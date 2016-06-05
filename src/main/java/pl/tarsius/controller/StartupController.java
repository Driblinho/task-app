package pl.tarsius.controller;


import impl.org.controlsfx.skin.DecorationPane;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.context.FXMLApplicationContext;
import io.datafx.controller.context.Metadata;
import io.datafx.controller.context.ViewMetadata;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.flow.context.ViewFlowContext;
import io.datafx.controller.util.VetoException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.UserAuth;
import pl.tarsius.util.gui.ResponsiveDesign;
import pl.tarsius.util.validator.PeselValidator;
import pl.tarsius.util.validator.form.UserFormValidator;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

/**
 * Created by Ireneusz Kuliga on 25.03.16.
 */
@FXMLController(value = "/view/startup/welcome.fxml", title = "Tarsius")
public class StartupController {

    private Logger loger;

    @FXMLApplicationContext
    private ApplicationContext appContext;

    @FXML
    private Label topMsg;

    @FXML
    @ActionTrigger("showForgetForm")
    private Hyperlink forgotPassword;


    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML private DecorationPane logInForm;
    @FXML private DecorationPane registrationForm;
    @FXML private AnchorPane forgotPasswordForm;

    //Fromularz logowania
    @FXML private TextField logInEmail;
    @FXML private PasswordField logInPassword;

    @FXML
    @ActionTrigger("validateLogin")
    private Button logIn;

    //Formularz rejestracji
    @FXML private TextField regEmail;
    @FXML private TextField regName;
    @FXML private TextField regSurname;
    @FXML private PasswordField regPassword;
    @FXML private PasswordField regPassword2;
    @FXML private TextField regTel;
    @FXML private TextField regPESEL;
    @FXML private TextField regCity;
    @FXML private TextField regStreet;
    @FXML private TextField regZip;

    @FXML
    @ActionTrigger("validateReg")
    private Button regSend;

    //Formularz odzyskiwania hasła
    @FXML private TextField forgEmail;
    @FXML private TextField forgToken;
    @FXML private PasswordField forgPassword;
    @FXML private PasswordField forgPassword2;

    @FXML
    @ActionTrigger("showLogInForm")
    private Button cancel;

    @FXML
    @ActionTrigger("saveNewPassword")
    private Button change;

    @FXML
    @ActionTrigger("sendTokenToEmail")
    private Button sendToken;


    //Nagłówek zakładek (logowanie)
    @FXML private Tab logInTab;

    @FXML private StackPane loading;

    @FXML private GridPane topMsgContent;
    @FXML
    @ActionTrigger("closeAlert")
    private Hyperlink topMsgClose;


    private ValidationSupport validationSupportLogin;
    private ValidationSupport validationSupportReg;
    private ValidationSupport validationSupportForg;

    //private Validator validatorsEmail;

    private ValidationSupport validationSupportTokenSend = new ValidationSupport();

    public StartupController() {
        loger = LoggerFactory.getLogger(getClass());
    }

    @PostConstruct
    public void init() {


        Platform.runLater(() -> {
            new ResponsiveDesign((Stage) forgotPassword.getParent().getScene().getWindow()).resizeBodyWidth(forgotPassword.getParent().getScene().getWindow().getWidth());
            //-3.48% HACK
            double h = forgotPassword.getParent().getScene().getWindow().getHeight();
            //h = h-h*0.0348;
            h = h-h*0.0248;
            new ResponsiveDesign((Stage) forgotPassword.getParent().getScene().getWindow()).resizeBodyHeight(h);
        });

        validationSupportLogin = new ValidationSupport();
        validationSupportLogin.registerValidator(logInEmail, true, UserFormValidator.getEmail());
        validationSupportLogin.registerValidator(logInPassword, true, UserFormValidator.getPassword());

        validationSupportReg = new ValidationSupport();
        validationSupportReg.registerValidator(regEmail, true, UserFormValidator.getEmail());
        validationSupportReg.registerValidator(regName, true, UserFormValidator.getName());
        validationSupportReg.registerValidator(regSurname, true, UserFormValidator.getSurname());
        validationSupportReg.registerValidator(regTel, true, UserFormValidator.getPhone());
        validationSupportReg.registerValidator(regPESEL, true, UserFormValidator.getPesel());
        validationSupportReg.registerValidator(regCity, true, UserFormValidator.getCity());
        validationSupportReg.registerValidator(regStreet, true, UserFormValidator.getStreet());
        validationSupportReg.registerValidator(regZip, true, UserFormValidator.getZip());


        // TODO: 03.04.16 Ulepszyć
        ArrayList<String> p1 = new ArrayList<>();
        p1.add(regPassword.textProperty().toString());

        regPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            p1.set(0,newValue);
            regPassword2.setText(null);
        });

        validationSupportReg.registerValidator(regPassword2, true, UserFormValidator.getEqPassword(p1));
        validationSupportReg.registerValidator(regPassword,true,UserFormValidator.getPassword());


        Validator validatorToken = Validator.combine(Validator.createEmptyValidator("Token jest wymagany"));

        // TODO: 03.04.16 Ulepszyć
        ArrayList<String> fp1 = new ArrayList<>();
        fp1.add(forgPassword.textProperty().toString());

        forgPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            fp1.set(0,newValue);
            forgPassword2.setText(null);
        });

        validationSupportForg = new ValidationSupport();
        validationSupportForg.registerValidator(forgEmail,true,UserFormValidator.getEmail());
        validationSupportForg.registerValidator(forgToken,true,validatorToken);
        validationSupportForg.registerValidator(forgPassword,true,UserFormValidator.getPassword());
        validationSupportForg.registerValidator(forgPassword2,true,UserFormValidator.getEqPassword(fp1));
        validationSupportTokenSend.registerValidator(forgEmail,UserFormValidator.getEmail());
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
    public void validateLogin() throws VetoException, FlowException {
        if(validationSupportLogin.isInvalid()) {
            validationSupportLogin.errorDecorationEnabledProperty();
            validationSupportLogin.initInitialDecoration();
        } else {


            Task<Object[]> task = new Task<Object[]>() {
                @Override
                protected Object[] call() {
                    Object[] authUser = UserAuth.authUser(logInPassword.getText(),logInEmail.getText());
                    loger.debug((String) authUser[1]);
                    return authUser;
                }
            };

            task.setOnFailed(event -> {event.getSource().getException().printStackTrace();loading.setVisible(false);showAlert("Problem z wykonaniem logowania",false);});
            task.setOnRunning(event -> loading.setVisible(true));
            task.setOnSucceeded(event -> {
                if((boolean)task.getValue()[0]) {
                    try {
                        flowActionHandler.navigate(HomeController.class);
                    } catch (VetoException e) {
                        loger.debug("LoginTask", e);
                    } catch (FlowException e) {
                        loger.debug("LoginTask", e);
                    }
                } else {
                    loading.setVisible(false);
                    showAlert((String) task.getValue()[1], false);
                }
            });
            new Thread(task).start();
        }
    }

    @ActionMethod("validateReg")
    public void validateReg() throws VetoException, FlowException {
        if(validationSupportReg.isInvalid()) {
            validationSupportReg.errorDecorationEnabledProperty();
            validationSupportReg.initInitialDecoration();
        } else {

            User user = new User();
            PeselValidator p = new PeselValidator(regPESEL.getText());
            user.setHaslo(regPassword.getText().trim());
            user.setImie(regName.getText().trim());
            user.setNazwisko(regSurname.getText().trim());
            user.setEmail(regEmail.getText().trim().toLowerCase());
            user.setUlica(regStreet.getText().trim());
            user.setMiasto(regCity.getText().trim());
            user.setKodPocztowy(regZip.getText().trim());
            user.setPesel(regPESEL.getText().trim());
            //user.setDataUrodzenia(Date.valueOf(LocalDate.of(p.getBirthYear(),p.getBirthMonth(),p.getBirthDay())));
            user.setTelefon(regTel.getText().trim());

            Object[] status = UserAuth.createUser(user);

            showAlert((String) status[1],(boolean) status[0]);

            if((boolean) status[0]) {
                regEmail.setText("");
                regName.setText("");
                regSurname.setText("");
                regTel.setText("");
                regCity.setText("");
                regStreet.setText("");
                regZip.setText("");
                regPESEL.setText("");
                regPassword.setText("");
                regPassword2.setText("");
                validationSupportReg.setErrorDecorationEnabled(false);
            }



        }




    }

    @ActionMethod("sendTokenToEmail")
    public void sendTokenToEmail() {
        if(validationSupportTokenSend.isInvalid()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("oops!");
            alert.setHeaderText("Nie mozna wysłać tokena");
            String s ="Aby wysłać token nalezy podać adres email zarejestrowanego użytkownika";
            alert.setContentText(s);
            alert.getDialogPane().setPrefSize(400,200);
            alert.show();
        } else {

           Object[] sendMsg = UserAuth.sendToken(forgEmail.getText());

            if ((boolean)sendMsg[0]) {
                topMsgContent.setVisible(true);
                topMsgContent.setStyle("-fx-background-color: rgba(1, 164, 164, 0.7)");
                topMsg.setText((String) sendMsg[1]);
            } else {
                topMsgContent.setVisible(true);
                topMsg.setText("Błąd: "+sendMsg[1]);
            }

        }
    }

    @ActionMethod("saveNewPassword")
    public void saveNewPassword() throws VetoException, FlowException {
        if(validationSupportForg.isInvalid()) {
            validationSupportForg.initInitialDecoration();
        } else {
           Object[] authUser = UserAuth.changePassword(forgEmail.getText(),forgToken.getText(),forgPassword.getText());
           showAlert((String)authUser[1],(boolean)authUser[0]);
            if(!(boolean)authUser[0]) {

            }
        }
    }

    @ActionMethod("closeAlert")
    public void closeAlert() {
        topMsgContent.setVisible(false);
    }


    private void showAlert(String msg, boolean info) {
        topMsgContent.setVisible(true);
        topMsg.setText(msg);
        if(info) {
            topMsgContent.setStyle("-fx-background-color: rgba(1, 164, 164, 0.7)");
        } else  {
            topMsgContent.setStyle("-fx-background-color: rgba(204, 10, 10, 0.7)");
        }
    }

    private void hideAlert() { topMsgContent.setVisible(false); }










}
