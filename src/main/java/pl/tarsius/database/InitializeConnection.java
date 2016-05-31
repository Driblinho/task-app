package pl.tarsius.database;


import com.mysql.jdbc.Connection;
import io.datafx.controller.context.ApplicationContext;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Created by Ireneusz Kuliga on 23.03.16.
 * Klasa inicjalizująca połączenie z bazą danych
 */
public class InitializeConnection {

    private static Logger loger = LoggerFactory.getLogger(InitializeConnection.class);;
    private Connection connection;
    private String jdbcUrl;
    private String username;
    private String password;

    /**
     * Konstruktor ładujący ustawienia z pliku
     */
    public InitializeConnection() {
            SqlConnection cfg = ApplicationContext.getInstance().getRegisteredObject(SqlConnection.class);
            jdbcUrl = cfg.getJdbcUrl();
            username = cfg.getUsername();
            password = cfg.getPassword();
    }

    public static void configLoader() {
        URL url = InitializeConnection.class.getProtectionDomain().getCodeSource().getLocation();
        Properties databseConfig = new Properties();
        try {
            FileInputStream cfgFile=null;
            try {
                String path = URLDecoder.decode(url.getFile(), "UTF-8");
                cfgFile = new FileInputStream(new File(path+"database.properties"));
            } catch (UnsupportedEncodingException e) {
                loger.debug("Kodowanie pliku CFG DB", e);
                new Alert(Alert.AlertType.ERROR,"Problem z kodowaniem pliku konfiguracyjnego bazy danych").show();
            }
            databseConfig.load(cfgFile);
            ApplicationContext.getInstance().register(new SqlConnection(
                    databseConfig.getProperty("database.jdbcUrl"),
                    databseConfig.getProperty("database.user"),
                    databseConfig.getProperty("database.password")
            ));
        } catch (IOException e) {
            loger.debug("CFG DB", e);
            new Alert(Alert.AlertType.ERROR,"Błąd podczas ładowania \npliku konfiguracyjnego połączenie z bazą danych").show();
        }
    }

    /**
     * Metoda zwracająca połączenie do bazy danych
     * @return Connection
     * @throws SQLException
     */
    public Connection connect() throws SQLException {
        loger.info("Connect to Database");
        return connection = (Connection) DriverManager.getConnection(jdbcUrl, username, password);

    }

    /**
     * Getter zwracający url jdbc wraz z nazwą bazy danych
     * @return String
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Getter zwracający hasło do bazy danych
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Getter zwracający nazwę użytkownika bazy
     * @return
     */
    public String getUsername() {
        return username;
    }

    private static class SqlConnection {
        private String jdbcUrl;
        private String username;
        private String password;

        public SqlConnection(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }

        /**
         * Getter for property 'jdbcUrl'.
         *
         * @return Value for property 'jdbcUrl'.
         */
        public String getJdbcUrl() {
            return jdbcUrl;
        }

        /**
         * Getter for property 'username'.
         *
         * @return Value for property 'username'.
         */
        public String getUsername() {
            return username;
        }

        /**
         * Getter for property 'password'.
         *
         * @return Value for property 'password'.
         */
        public String getPassword() {
            return password;
        }
    }
}
