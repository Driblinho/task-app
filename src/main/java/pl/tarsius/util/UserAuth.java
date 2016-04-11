package pl.tarsius.util;

import com.mysql.jdbc.PreparedStatement;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.dbutils.DbUtils;
import org.datafx.controller.context.ApplicationContext;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.User;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

/**
 * Created by Ireneusz Kuliga on 29.03.16.
 */
public class UserAuth {

    private static Logger loger = LoggerFactory.getLogger(UserAuth.class);;
    private static Connection connection;

    public static String genHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }


    public static Object[] authUser(String password, String email) {
        Object [] value = new Object[2];
        value[1] = "Nieprawidłowy email lub hasło";
        value[0] = false;
        String sql = "SELECT * FROM `Uzytkownicy` WHERE `email` = ?";
        String dbHash;
        User userModel = null;
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setString(1, email.trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getRow()==0) return value;
            userModel = setUserModel(resultSet);
            if(!BCrypt.checkpw(password, userModel.getHaslo())) return value;

            if(!resultSet.getBoolean("aktywny")) {
                value[1] = "Użytkownik nie jest aktywny";
                return value;
            };
            Timestamp lock = resultSet.getTimestamp("blokada");
            if(lock!=null && lock.before(new Timestamp(new Date().getTime()))) {
                value[1] = "Użytkownik zablokowany do: "+lock.toString();
                return value;
            }



        } catch (SQLException e) {
            loger.debug(e.getSQLState());
            value[1] = "Problem z bazą danych";
            return value;
        }

        ApplicationContext.getInstance().register("userSession", userModel);
        return new Object[] {true,"Zalogowano"};
    }

    public static Object[] createUser(User userData) {
        Object [] status = new Object[2];
        status[0] = false;
        status[1] = "Użytkownik został zarejestrowany poprawnie";

        String countSql = "select count(*) as count from Uzytkownicy";
        String sql = "INSERT INTO `Uzytkownicy` (`email`, `imie`, `nazwisko`, `haslo`, `typ`, `data_urodzenia`, `telefon`, `kod_pocztowy`, `miasto`, `ulica`, `aktywny`, `PESEL`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();

            preparedStatement = (PreparedStatement) connection.prepareStatement(countSql);
            ResultSet count = preparedStatement.executeQuery();
            count.next();

            int typ;
            boolean aktywny;

            //Jeżeli w bazie niema innych użytkowników rejestruj administratora
            if(count.getLong(1)==0) {
                typ = 3;
                aktywny = true;
            } else {
                typ = 1;
                aktywny = false;
            }

            preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            int i = 1;
            preparedStatement.setString(i++, userData.getEmail());
            preparedStatement.setString(i++, userData.getImie());
            preparedStatement.setString(i++, userData.getNazwisko());
            preparedStatement.setString(i++, genHash(userData.getHaslo()));
            preparedStatement.setInt(i++, typ);
            preparedStatement.setDate(i++, userData.getDataUrodzenia());
            preparedStatement.setString(i++, userData.getTelefon());
            preparedStatement.setString(i++, userData.getKodPocztowy());
            preparedStatement.setString(i++, userData.getMiasto());
            preparedStatement.setString(i++, userData.getUlica());
            preparedStatement.setBoolean(i++, aktywny);
            preparedStatement.setString(i++, userData.getPesel());
            int u = preparedStatement.executeUpdate();
            status[0] = u>0;
            
        } catch (SQLIntegrityConstraintViolationException e) {
            status[1] = "Email i PESEL muszą być unikalne";
            loger.debug(e.getMessage());
        } catch (SQLException e) {
            status[1] = "Błąd bazy danych";
            loger.debug(e.getMessage());
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
            return status;
        }
    }

    private static String genRecoveryToken() {
        SecureRandom random = new SecureRandom();
        String s = new BigInteger(130, random).toString(32).toUpperCase();
        return s.substring(0,10);
    }

    public static Object[] sendToken(String email) {
        Object[] values = new Object[2];
        values[0]=false;
        LocalDateTime tokenTime = LocalDateTime.now().plusDays(7);
        String sql = "select count(*),uzytkownik_id,imie,nazwisko from Uzytkownicy WHERE email=?";
        try {
            Connection connection= new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setString(1, email.toLowerCase());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            Long count = resultSet.getLong(1);
            Long userId = resultSet.getLong(2);
            String imie = resultSet.getString(3);
            String nazwisko = resultSet.getString(4);

            if(count>0) {
                //Usuwanie wszystkich tokenow uzykownikow
                sql = "DELETE FROM HasloResetToken WHERE uzytkownik_id="+userId;
                DbUtils.closeQuietly(preparedStatement);
                preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
                int del = preparedStatement.executeUpdate();
                DbUtils.closeQuietly(preparedStatement);
                //Generowanie nowego tokenu
                sql = "INSERT INTO `HasloResetToken` (`token`, `waznosc`, `uzytkownik_id`) VALUES (?, ?, ?);";
                preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
                int i = 1;
                String token = genRecoveryToken();
                preparedStatement.setString(i++, token);
                preparedStatement.setTimestamp(i++, Timestamp.valueOf(tokenTime));
                preparedStatement.setLong(i++, userId);
                int up = preparedStatement.executeUpdate();

                Properties properties = new Properties();
                try {
                    InputStream cfgFile = new FileInputStream(UserAuth.class.getClassLoader().getResource("properties/mail.properties").getFile());
                    properties.load(cfgFile);
                    Mail mail = new Mail(properties.getProperty("apiKey"),UserAuth.class.getClassLoader().getResource("assets/emailtempleate.html").getPath());
                    mail.setToken(token);
                    mail.setApDomain("mail@taskapp.com");
                    mail.setSubject("Zmiana hasła");
                    mail.setDesc("Treść wiadomości dla użytkownika");
                    mail.setEmailUser(email.trim().toLowerCase());
                    mail.setMessage("Wiadomość");
                    mail.setNameApp("TaskApp");
                    mail.setNameUser(imie);
                    mail.setNameUser(imie+" "+nazwisko);
                    ClientResponse clientResponse = mail.send();
                    if(clientResponse.getStatus()==200) {
                        values[0] = true;
                        values[1] = "Wiadomość z Tokenem została wysłana";
                    } else values[1] = "Nie można wysłać maila Status:"+clientResponse.getStatus();

                } catch (java.io.IOException e) {
                    return new Object[]{false,e.getMessage()};
                }

            } else values[1] = "W bazie nie ma użytkownika o takim adresei email";

        } catch (SQLException e) {
            values = new Object[] {false, e.getMessage()};
        } finally {
            return values;
        }

    }

    public static Object[] changePassword(String email, String token, String password) {
        Object[] value = new Object[2];
        value[0]=false;
        String sql = "select count(*),u.uzytkownik_id FROM Uzytkownicy u,HasloResetToken ht where u.uzytkownik_id=ht.uzytkownik_id and token=? and email=?";

        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setString(1,token);
            preparedStatement.setString(2,email.trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            long count = resultSet.getLong(1);
            long userId = resultSet.getLong(2);
            if(count>0) {
                //Zmien haslo
                DbUtils.closeQuietly(null,preparedStatement,resultSet);
                sql = "UPDATE Uzytkownicy SET haslo=? WHERE `Uzytkownicy`.`uzytkownik_id`=?";
                preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
                preparedStatement.setString(1,genHash(password.trim()));
                preparedStatement.setLong(2,userId);
                preparedStatement.executeUpdate();
                DbUtils.closeQuietly(preparedStatement);
                sql = "DELETE FROM HasloResetToken WHERE uzytkownik_id="+userId;
                preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
                preparedStatement.executeUpdate();

                value[0] = true;
                value[1] = "Hasło zostało zmienione";
            } else {
                value[1] = "Brak użytkownika powiązanego z tokenem";
            }

        } catch (SQLException e) {
            value[1] = e.getMessage();
        }
        finally {
            return value;
        }


    }

    public static Object[] updatePassword(String password, long userId) {
        String sql = "UPDATE Uzytkownicy SET haslo = ? WHERE Uzytkownicy.uzytkownik_id = ?;";
        Object[] value = new Object[3];
        value[0] = false;
        value[1] = "Hasło nie zostało zmienione";
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            String hash = genHash(password);
            preparedStatement.setString(1, hash);
            preparedStatement.setLong(2, userId);
            int up = preparedStatement.executeUpdate();
            if(up>0) {
                value[0] = true;
                value[1] = "Hasło zostało zmienione";
                value[2] = hash;
            }

        } catch (SQLException e) {
            loger.debug(e.getMessage());
            value[1] = "Błąd bazy danych";
        } finally {
            return value;
        }
    }

    public static Object[] updateUser(User userModel) {
        String sql = "UPDATE Uzytkownicy SET email = ?, imie = ?, nazwisko = ?, data_urodzenia =  ?, telefon = ?, kod_pocztowy = ?, miasto = ?, ulica = ?, PESEL = ?  WHERE Uzytkownicy.uzytkownik_id = ?";
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            int i=1;
            preparedStatement.setString(i++, userModel.getEmail());
            preparedStatement.setString(i++, userModel.getImie());
            preparedStatement.setString(i++, userModel.getNazwisko());
            preparedStatement.setDate(i++, userModel.getDataUrodzenia());
            preparedStatement.setString(i++, userModel.getTelefon());
            preparedStatement.setString(i++, userModel.getKodPocztowy());
            preparedStatement.setString(i++, userModel.getMiasto());
            preparedStatement.setString(i++, userModel.getUlica());
            preparedStatement.setString(i++, userModel.getPesel());
            preparedStatement.setLong(i++, userModel.getUzytkownikId());

            int up = preparedStatement.executeUpdate();
            if(up>0) {
                return new Object[]{true,"Dane zostały zaktualizowane"};
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            return new Object[] {false, "Email i PESEL muszą być unikalne"};
        } catch (SQLException e) {
            //e.printStackTrace();
            loger.debug(e.getMessage());
            return new Object[] {false,"Błąd bazy danych"};
        }
        return new Object[]{false, "Błąd "};
    }


    public static User setUserModel(ResultSet resultSet) throws SQLException {
        User userModel = new User();
        userModel.setUzytkownikId(resultSet.getLong("uzytkownik_id"));
        userModel.setAvatarId(resultSet.getString("avatar_id"));
        userModel.setImie(resultSet.getString("imie"));
        userModel.setNazwisko(resultSet.getString("nazwisko"));
        userModel.setAvatarId(resultSet.getString("avatar_id"));
        userModel.setEmail(resultSet.getString("email"));
        userModel.setPesel(resultSet.getString("PESEL"));
        userModel.setDataUrodzenia(resultSet.getDate("data_urodzenia"));
        userModel.setTelefon(resultSet.getString("telefon"));
        userModel.setMiasto(resultSet.getString("miasto"));
        userModel.setUlica(resultSet.getString("ulica"));
        userModel.setKodPocztowy(resultSet.getString("kod_pocztowy"));
        userModel.setTyp(resultSet.getInt("typ"));
        userModel.setHaslo(resultSet.getString("haslo"));
        return userModel;
    }

    public static Object[] setAvatar(String imagePath,long userId) {
        String sql = "UPDATE Uzytkownicy SET avatar_id = ? WHERE Uzytkownicy.uzytkownik_id = ?";

        try {
            ImageCloudinaryUpload imageCloudinaryUpload = new ImageCloudinaryUpload();

            Map<String,Object> image = imageCloudinaryUpload.send(imagePath);
            String imgId = (String) image.get("public_id");
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setString(1,imgId);
            preparedStatement.setLong(2,userId);
            preparedStatement.executeUpdate();
            User user =(User) ApplicationContext.getInstance().getRegisteredObject("userSession");
            user.setAvatarId(imageCloudinaryUpload.getUrl(imgId));
            ApplicationContext.getInstance().register("userSession",user);
            return new Object[]{true,"Avatar został zmieniony"};
        } catch (IOException e) {
            loger.debug(e.getMessage());
            return new Object[]{false,"Błąd podczas odczytu grafiki"};
        } catch (SQLException e) {
            loger.debug(e.getMessage());
            return new Object[] {false, "Błąd bazy danych"};
        }
    }


}
