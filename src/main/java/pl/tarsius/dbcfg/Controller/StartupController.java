package pl.tarsius.dbcfg.Controller;

import com.mysql.jdbc.Driver;
import io.datafx.controller.FXMLController;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.dbcfg.DB.ScriptRunner;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.*;
import java.util.Properties;

/**
 * Created by ireq on 08.06.16.
 */
@FXMLController(value = "/view/cfgWindow.fxml", title = "Konfigurator połączenia z bazą - Tarsius")
public class StartupController {

    @FXML @ActionTrigger("save")
    private Button saveCfg;
    @FXML
    private TextField dbUser;
    @FXML
    private TextField dbName;
    @FXML
    private TextField dbHost;
    @FXML
    private PasswordField dbPass;
    @FXML
    private TextField cjName;
    @FXML
    private TextField cjSecret;
    @FXML
    private TextField cjApi;
    @FXML
    private TextField mailApi;
    @FXML
    private CheckBox dbCreateNew;

    private ValidationSupport validationSupport;
    private ValidationSupport apiValidation;

    @PostConstruct
    public void start() {



    }

    private Object[] invalidDB() {
        boolean api = cjApi.getText().isEmpty() || cjName.getText().isEmpty() || cjSecret.getText().isEmpty() || mailApi.getText().isEmpty();
        boolean sql = dbHost.getText().isEmpty() || dbPass.getText().isEmpty() || dbUser.getText().isEmpty() || dbName.getText().isEmpty();
        if (dbCreateNew.isSelected())
            return new Object[] {api || sql,"Wystkie pola wymagane"} ;
        return new Object[] {sql,"Konfiguracja mysql wymagana"} ;
    }

    @ActionMethod("save")
    public void save() {
        Object[] v = invalidDB();
        if((boolean)v[0]) {
            new Alert(Alert.AlertType.WARNING,""+v[1]).show();
        } else {
            String DB_URL = "jdbc:mysql://"+dbHost.getText().trim()+"/{DB}?characterEncoding=UTF-8";
            try {
                Connection conn = null;

                try {

                    if(dbCreateNew.isSelected()) {

                        conn = DriverManager.getConnection(DB_URL.replace("{DB}",""), dbUser.getText().trim(),dbPass.getText().trim());
                        conn.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS "+dbName.getText().trim());
                        conn = DriverManager.getConnection(DB_URL.replace("{DB}", dbName.getText().trim()), dbUser.getText().trim(),dbPass.getText().trim());

                        InputStreamReader is = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("sqlscript/taskappEmpty.sql"));
                        new ScriptRunner(conn,false,true).runScript(is);

                        conn.createStatement().executeUpdate("TRUNCATE TABLE ApiKeys;");
                        conn.setAutoCommit(false);

                        PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO ApiKeys (nazwa_wartosci,wartosc) VALUES (?,?);");

                        preparedStatement.setString(1, "cloudinary.apiKey");
                        preparedStatement.setString(2, cjApi.getText());
                        preparedStatement.addBatch();

                        preparedStatement.setString(1, "cloudinary.apiSecret");
                        preparedStatement.setString(2, cjSecret.getText());
                        preparedStatement.addBatch();

                        preparedStatement.setString(1, "cloudinary.cloudNam");
                        preparedStatement.setString(2, cjName.getText());
                        preparedStatement.addBatch();

                        preparedStatement.setString(1, "mailgun.apiKey");
                        preparedStatement.setString(2, mailApi.getText());
                        preparedStatement.addBatch();

                        preparedStatement.executeBatch();
                        conn.commit();

                    } else {
                        DriverManager.getConnection(DB_URL.replace("{DB}", dbName.getText().trim()), dbUser.getText().trim(),dbPass.getText().trim());
                    }



                    Properties properties = new Properties();

                    properties.setProperty("database.password", dbPass.getText());
                    properties.setProperty("database.user", dbUser.getText());
                    properties.setProperty("database.jdbcUrl", DB_URL.replace("{DB}", dbName.getText().trim()));

                    URL url = StartupController.class.getProtectionDomain().getCodeSource().getLocation();
                    String path = URLDecoder.decode(url.getFile(), "UTF-8");
                    File cfg = new File(new File("").getAbsolutePath()+File.separator+"database.properties");
                    OutputStream outputStream = new FileOutputStream(cfg);
                    properties.store(outputStream,"DB CFG");

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Plik konfiguracyjny został wygenerowany");
                    alert.setHeaderText("Wygenerowano plik konfiguracyjny");
                    alert.show();


                } catch (IOException e) {
                    e.printStackTrace();
                    Alert ale = new Alert(Alert.AlertType.ERROR,"Błąd podczas dostępu do plików");
                    ale.setHeaderText("Błąd");
                    ale.show();
                }

            } catch (SQLException e) {
                String msg = e.getMessage();
                if(e.getMessage().startsWith("Access denied")) msg="Bark uprawnień do bazy lub niepoprawne dane";
                if(e.getMessage().startsWith("Unknown database")) msg = "Nieprawidłowa nazwa bazy danych";
                if(e.getMessage().startsWith("Communications")) msg = "Nieprawidłowy host";
                Alert alert = new Alert(Alert.AlertType.WARNING, msg);
                alert.setHeaderText("Błąd");
                alert.show();
                e.printStackTrace();
            }
        }
    }
}
