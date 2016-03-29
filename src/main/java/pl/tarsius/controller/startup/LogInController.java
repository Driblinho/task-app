package pl.tarsius.controller.startup;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
public class LogInController implements Initializable {
    @FXML public Hyperlink forgotPassword;
    @FXML private Button logIn;
    @FXML private TextField logInEmail;
    @FXML private TextField logInPassword;
    @Override
    public void initialize(URL location, ResourceBundle resources) {


        //logInEmail.setStyle("-fx-border-color:red");
        logIn.setOnAction(event -> {
            System.out.println("KLIK");
        });


    }

}
