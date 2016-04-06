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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ireneusz Kuliga on 29.03.16.
 */
public class UserAuth {


    public static String genHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public static int authUser(String password, String email) {
        String sql = "SELECT * FROM `Uzytkownicy` WHERE `email` = ?";

        Connection connection = new InitializeConnection().connect();
        try {
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.getFetchSize();
            System.out.println();
            boolean status = resultSet.getBoolean("aktywny");
            if(!status) return -1;
            if (resultSet.getTimestamp("blokada").before(new Timestamp(new Date().getTime()))) return -2;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        String dbHash = "HH";
        return BCrypt.checkpw(password, dbHash)?1:-3;
    }

    public static Object[] createUser(User userData) {
        Object [] status = new Object[2];
        status[0] = false;
        List<String> errMsg = new ArrayList<>();
        String sql = "INSERT INTO `Uzytkownicy` (`email`, `imie`, `nazwisko`, `haslo`, `typ`, `data_urodzenia`, `telefon`, `kod_pocztowy`, `miasto`, `ulica`, `aktywny`, `PESEL`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        Connection connection = new InitializeConnection().connect();
        PreparedStatement preparedStatement = null;
        try {
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
