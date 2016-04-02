package pl.tarsius.controller.startup;

import impl.org.controlsfx.skin.DecorationPane;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import pl.tarsius.controller.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
@FXMLController("view/startup/welcome.fxml")
public class StartupController extends Controller {
    @FXML private BorderPane body;
    @FXML private DecorationPane logIn;
    @FXML private LogInController logInController;
    @FXML private ForgotPasswordController forgotPasswordController;
    @FXML private AnchorPane forgotPassword;
    @FXML private Tab logInTab;
    @FXML private ImageView logo;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        logInController.getForgotPassword().setOnAction(event -> {
            logInTab.setText("Odzyskiwanie hasła");
            System.out.println(ApplicationContext.getInstance().getRegisteredObject("User"));
            VBox vb = (VBox) logIn.getParent();
            vb.getChildren().clear();
            vb.getChildren().addAll(forgotPassword);
        });


        forgotPasswordController.getCancel().setOnAction(event -> {
            logInTab.setText("Logowanie");
            VBox vb = (VBox) forgotPassword.getParent();
            vb.getChildren().clear();
            vb.getChildren().addAll(logIn);
        });




    }
    @Override
    public void start() {

        //Skalowanie baneru startowego
        logo.setFitWidth(logo.getScene().getWidth());
        logo.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
            logo.setFitWidth(newValue.doubleValue());
        });

    }

}
