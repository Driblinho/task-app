package pl.tarsius.controller.startup;

import impl.org.controlsfx.skin.DecorationPane;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.action.ActionMethod;
import org.datafx.controller.flow.action.ActionTrigger;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
@FXMLController("/view/startup/welcome.fxml")
public class StartupController {
//
//    @FXML private BorderPane body;
//    @FXML private DecorationPane logIn;
//    @FXML private AnchorPane forgotPassword;
//    @FXML private Tab logInTab;
//    @FXML private ImageView logo;




    @FXML
    @ActionTrigger("showForgetForm")
    private Hyperlink forgotPassword;
    @FXML
    @ActionTrigger("showLogInForm")
    private Button cancel;

    @FXML
    @ActionTrigger("validateLogin")
    private Button logIn;

    @FXML private DecorationPane logInForm;
    @FXML private AnchorPane forgotPasswordForm;

    @FXML private TextField logInEmail;
    @FXML private PasswordField logInPassword;

    @FXML private Tab logInTab;

    private ValidationSupport validationSupport;


    @PostConstruct
    public void init() {

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



    }

    @ActionMethod("showForgetForm")
    public void showForgetForm() {
        logInForm.setVisible(false);
        forgotPasswordForm.setVisible(true);
        logInTab.setText("Odzyskiwanie hasła");
    }

    @ActionMethod("showLogInForm")
    public void showLogInForm() {
        logInForm.setVisible(true);
        forgotPasswordForm.setVisible(false);
        logInTab.setText("Logowanie");
    }

    @ActionMethod("validateLogin")
    public void validateLogin() {
        if(validationSupport.isInvalid()) {
            validationSupport.errorDecorationEnabledProperty();
            validationSupport.initInitialDecoration();

        }
    }





}
