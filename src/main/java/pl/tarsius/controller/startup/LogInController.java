package pl.tarsius.controller.startup;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.controller.Controller;
import pl.tarsius.util.validator.CustomValidator;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
public class LogInController extends Controller {
    @FXML private Hyperlink forgotPassword;
    @FXML private Button logIn;
    @FXML private TextField logInEmail;
    @FXML private TextField logInPassword;
    private ValidationSupport validationSupport;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validationSupport = new ValidationSupport();


        //Stowrzenie Validator dla pola logInEmail
        Validator validatorsEmail = Validator.combine(
            Validator.createEmptyValidator("Email jest wymagany"),
            CustomValidator.createMaxSizeValidator("Maksymalnie 10 znaków", 10),
            CustomValidator.createEmailValidator("Nieprawidłowy email")
        );

        //Stworzenie Validatora dla pola logInPassword
        Validator validatorsPass = Validator.combine(
                Validator.createEmptyValidator("Hasło jest wymagane"),
                CustomValidator.createMinSizeValidator("Minimalnie 6 znaków", 6)
        );

        validationSupport.registerValidator(logInEmail, true, validatorsEmail);
        validationSupport.registerValidator(logInPassword, true, validatorsPass);


        logIn.setOnAction(event -> {

            //((StackPane)ApplicationContext.getInstance().getRegisteredObject("loadingOverlay")).setVisible(true);
            setLoading(true);
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    setLoading(false);
                    t.cancel();
                }
            }, 2000);

            if(validationSupport.isInvalid()) {
                validationSupport.errorDecorationEnabledProperty();
                validationSupport.initInitialDecoration();

            }


        });


    }


    public Hyperlink getForgotPassword() {
        return forgotPassword;
    }

}
