package pl.tarsius.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbutils.DbUtils;

/**
 * Created by Ireneusz Kuliga on 24.03.16.
 */
public class SqlPrev extends InitializeConnection {
    private Statement statement;
    private ResultSet resultSet;
    public void getUsers() {
        try {
            statement = connect().createStatement();
            statement.executeQuery("select * from Uzytkownicy");
            resultSet = statement.getResultSet();
            while (resultSet.next()) {
                System.out.println(resultSet.getString("imie"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(getConnection(),statement,resultSet);
        }


    }
}
