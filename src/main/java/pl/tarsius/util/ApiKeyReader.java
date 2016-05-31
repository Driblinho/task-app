package pl.tarsius.util;

import javafx.scene.control.Alert;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

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
    private String mailApiKey;
    private String cloudinaryApiKey;
    private String cloudinaryApiSecret;
    private String cloudinaryCloudNam;

    public ApiKeyReader(String mailApiKey, String cloudinaryCloudNam, String cloudinaryApiKey, String cloudinaryApiSecret) {
        this.cloudinaryCloudNam = cloudinaryCloudNam;
        this.mailApiKey = mailApiKey;
        this.cloudinaryApiKey = cloudinaryApiKey;
        this.cloudinaryApiSecret = cloudinaryApiSecret;
    }
    public ApiKeyReader() {}

    public ApiKeyReader load() {
        Connection connection = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            connection = new InitializeConnection().connect();
            st = connection.createStatement();
            rs = st.executeQuery("select * from apis");
            HashMap<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("nazwa_wartosci"),rs.getString("wartosc"));
            }
            return new ApiKeyReader(map.get("mailgun.apiKey"),map.get("cloudinary.cloudNam"),map.get("cloudinary.apiKey"),map.get("cloudinary.apiSecret"));
        } catch (SQLException e) {
            loger.debug("ApiKeyReader", e);
            new Alert(Alert.AlertType.ERROR, "Błąd podczas pobierania apiKey").show();
            return null;
        } finally {
            DbUtils.closeQuietly(connection,st,rs);
        }

    }

    /**
     * Getter for property 'mailApiKey'.
     *
     * @return Value for property 'mailApiKey'.
     */
    public String getMailApiKey() {
        return mailApiKey;
    }

    /**
     * Getter for property 'cloudinaryApiKey'.
     *
     * @return Value for property 'cloudinaryApiKey'.
     */
    public String getCloudinaryApiKey() {
        return cloudinaryApiKey;
    }

    /**
     * Getter for property 'cloudinaryApiSecret'.
     *
     * @return Value for property 'cloudinaryApiSecret'.
     */
    public String getCloudinaryApiSecret() {
        return cloudinaryApiSecret;
    }

    /**
     * Getter for property 'cloudinaryCloudNam'.
     *
     * @return Value for property 'cloudinaryCloudNam'.
     */
    public String getCloudinaryCloudNam() {
        return cloudinaryCloudNam;
    }
}
