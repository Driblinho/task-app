package pl.tarsius.util;

import javafx.scene.control.Alert;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.raport.ReportController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.ApiKeys;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Created by ireq on 30.05.16.
 */
public class ApiKeyReader {
    private static Logger loger = LoggerFactory.getLogger(ApiKeyReader.class);
    public ApiKeys getKeys() {
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            connection = new InitializeConnection().connect();
            st = connection.createStatement();
            rs = st.executeQuery("select * form apis");
            HashMap<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("nazwa_wartosci"),rs.getString("wartosc"));
            }
            return new ApiKeys(map.get("mailgun.apiKey"),map.get("cloudinary.cloudNam"),map.get("cloudinary.apiKey"),map.get("cloudinary.apiSecret"));
        } catch (SQLException e) {
            loger.debug("ApiKeyReader", e);
            new Alert(Alert.AlertType.ERROR, "Błąd podczas pobierania apiKey");
            return null;
        } finally {
            DbUtils.closeQuietly(connection,st,rs);
        }

    }
}
