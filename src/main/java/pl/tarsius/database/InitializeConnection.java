package pl.tarsius.database;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


/**
 * Created by Ireneusz Kuliga on 23.03.16.
 */
public class InitializeConnection {

    private Logger loger;
    private Connection connection;
    private Properties databseConfig;
    private String jdbcUrl;
    private String username;
    private String password;

    public InitializeConnection() {
        loger = LoggerFactory.getLogger(InitializeConnection.class);
        databseConfig = new Properties();
        try {
            InputStream cfgFile = new FileInputStream(getClass().getClassLoader().getResource("properties/database.properties").getFile());
            try {
                databseConfig.load(cfgFile);
                jdbcUrl  = databseConfig.getProperty("database.jdbcUrl");
                username = databseConfig.getProperty("database.user");
                password = databseConfig.getProperty("database.password");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection connect() throws SQLException {
        loger.info("Connect to Database");
        return connection = DriverManager.getConnection(jdbcUrl, username, password);

    }

}
