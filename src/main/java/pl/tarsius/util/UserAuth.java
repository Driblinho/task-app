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
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

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
            if(resultSet.getRow()==0) {
                value[1] = "Brak Użytkownika w Bazie";
                return value;
            }
            if(!resultSet.getBoolean("aktywny")) {
                value[1] = "Użytkowniej nie jest aktywny";
                return value;
            };
            Timestamp lock = resultSet.getTimestamp("blokada");
            if(lock!=null && lock.before(new Timestamp(new Date().getTime()))) {
                value[1] = "Użytkownik zablokowany do: "+lock.toString();
                return value;
            }
            dbHash = resultSet.getString("haslo");
            userModel = new User();
            userModel.setAvatarId(resultSet.getString("avatar_id"));
            userModel.setImie(resultSet.getString("imie"));
            userModel.setNazwisko(resultSet.getString("nazwisko"));
            userModel.setAvatarId(resultSet.getString("avatar_id"));
            userModel.setEmail(resultSet.getString("email"));

        } catch (SQLException e) {
            loger.debug(e.getMessage());
            value[1] = "Problem z bazą danych";
            return value;
        }
        if( BCrypt.checkpw(password, dbHash)) {
            ApplicationContext.getInstance().register("userSession", userModel);
            return new Object[] {true,"Zalogowano"};
        }
        value[1] = "Niepoprawne hasło";
        return value;
    }

    public static Object[] createUser(User userData) {
        Object [] status = new Object[2];
        status[0] = false;
        List<String> errMsg = new ArrayList<>();
        String sql = "INSERT INTO `Uzytkownicy` (`email`, `imie`, `nazwisko`, `haslo`, `typ`, `data_urodzenia`, `telefon`, `kod_pocztowy`, `miasto`, `ulica`, `aktywny`, `PESEL`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        //Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            int i = 1;
            preparedStatement.setString(i++, userData.getEmail());
            preparedStatement.setString(i++, userData.getImie());
            preparedStatement.setString(i++, userData.getNazwisko());
            preparedStatement.setString(i++, genHash(userData.getHaslo()));
            preparedStatement.setInt(i++, userData.getTyp());
            preparedStatement.setDate(i++, userData.getDataUrodzenia());
            preparedStatement.setString(i++, userData.getTelefon());
            preparedStatement.setString(i++, userData.getKodPocztowy());
            preparedStatement.setString(i++, userData.getMiasto());
            preparedStatement.setString(i++, userData.getUlica());
            preparedStatement.setBoolean(i++, userData.isAktywny());
            preparedStatement.setString(i++, userData.getPesel());
            int u = preparedStatement.executeUpdate();
            status[0] = u>0;
            
        } catch (SQLException e) {
            errMsg.add(e.getLocalizedMessage());

            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
            status[1] = errMsg;
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


}
