package pl.tarsius.controller.startup;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import pl.tarsius.controller.startup.ForgotPasswordController;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
public class StartupController implements Initializable {
    @FXML private AnchorPane logIn;
    @FXML private LogInController logInController;
    @FXML private ForgotPasswordController forgotPasswordController;
    @FXML private AnchorPane forgotPassword;
    @FXML private Tab logInTab;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logInController.forgotPassword.setOnAction(event -> {
            logInTab.setText("Odzyskiwanie hasła");
            VBox vb = (VBox) logIn.getParent();
            vb.getChildren().clear();
            vb.getChildren().addAll(forgotPassword);
        });


        forgotPasswordController.cancel.setOnAction(event -> {
            logInTab.setText("Logowanie");
            VBox vb = (VBox) forgotPassword.getParent();
            vb.getChildren().clear();
            vb.getChildren().addAll(logIn);
        });

    }

}
