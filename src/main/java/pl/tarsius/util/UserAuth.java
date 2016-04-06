package pl.tarsius.util;

import com.mysql.jdbc.PreparedStatement;
import org.apache.commons.dbutils.DbUtils;
import org.mindrot.jbcrypt.BCrypt;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.User;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Ireneusz Kuliga on 29.03.16.
 */
public class UserAuth {


    public static String genHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }


     /**
     *
     * @param password
     * @param email
     * @return
     */
    public static Object[] authUser(String password, String email) {
        Object [] value = new Object[2];
        value[0] = false;
        String sql = "SELECT * FROM `Uzytkownicy` WHERE `email` = ?";
        String dbHash="";

        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setString(1, email.trim());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getFetchSize()==0) {
                value[1] = "Brak Użytkownika w Javie";
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
        } catch (SQLException e) {
            value[1] = e.getMessage();
            return value;
        }
        if( BCrypt.checkpw(password, dbHash)) {
            return new Object[] {true,"Zalogowano"};
        }
        value[1] = "NIepoprawne hasło";
        return value;
    }

    public static Object[] createUser(User userData) {
        Object [] status = new Object[2];
        status[0] = false;
        List<String> errMsg = new ArrayList<>();
        String sql = "INSERT INTO `Uzytkownicy` (`email`, `imie`, `nazwisko`, `haslo`, `typ`, `data_urodzenia`, `telefon`, `kod_pocztowy`, `miasto`, `ulica`, `aktywny`, `PESEL`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        Connection connection = null;
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

}
