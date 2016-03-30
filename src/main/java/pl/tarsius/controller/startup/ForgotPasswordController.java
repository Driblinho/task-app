package pl.tarsius.controller.startup;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
public class ForgotPasswordController implements Initializable {
    @FXML
    public Button cancel;
    @FXML
    private AnchorPane content;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /*
        cancel.setOnAction(event -> {
            content.getChildren().clear();
            try {
                content.getChildren().setAll((Node) FXMLLoader.load(getClass().getClassLoader().getResource("view/startup/form/login.fxml")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });*/

    }


}
