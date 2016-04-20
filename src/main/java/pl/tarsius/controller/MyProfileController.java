package pl.tarsius.controller;


import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.context.FXMLApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.controlsfx.validation.ValidationSupport;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.UserAuth;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.form.UserFormValidator;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/app/profile.fxml", title = "TaskApp - Moje Dane")
public class MyProfileController extends BaseController {

    private Logger loger;


    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML private Pane profileCard;
    @FXML private Pane profileEdit;
    @FXML private Pane newPasswordForm;

    @FXML
    @ActionTrigger("editProfile")
    private Button editProfile;



    @FXML
    @ActionTrigger("newPassword")
    private Button changePassword;


    //Dane profilowe
    @FXML private Text profileDataName;
    @FXML private Text profileDataSurname;
    @FXML private Text profileDataPesel;
    @FXML private Text profileDataBirthday;
    @FXML private Text profileDataTel;
    @FXML private Text profileDataCity;
    @FXML private Text profileDataStreet;
    @FXML private Text profileDataZip;
    //Karta profilowa
    @FXML private Circle profileDataAvatar;
    @FXML
    @ActionTrigger("changeAvatar")
    private Hyperlink pickAvatar;
    @FXML private Label profileDataFullName;
    @FXML private Label profileDataEmail;
    @FXML private Label profileDataRanga;

    @FXML @ActionTrigger("formCancel")
    private Button profilecardCancel;

    @FXML @ActionTrigger("formCancel")
    private Button newPasswordCancel;

    //Formularz zmiany hasła
    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField newPassword2;

    //Formularz zmiany danych
    @FXML private TextField regEmail;
    @FXML private TextField regName;
    @FXML private TextField regSurname;
    @FXML private TextField regTel;
    @FXML private TextField regPESEL;
    @FXML private TextField regCity;
    @FXML private TextField regStreet;
    @FXML private TextField regZip;

    @FXML
    @ActionTrigger("updatePassword")
    private Button newPasswordSend;
    @FXML
    @ActionTrigger("updateUserData")
    private Button userDataSend;

    private ValidationSupport validationSupportNewData;
    private ValidationSupport validationSupportNewPassword;
    private Long showId;
    private User user;

    @PostConstruct
    public void init() {
        loger = LoggerFactory.getLogger(getClass());


        new StockButtons(operationButtons,flowActionHandler).homeAction();
        //User user;
        showId = (Long) ApplicationContext.getInstance().getRegisteredObject("showUserID");
        if(showId!=null) {
            user = UserAuth.userByID(showId);
        } else user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        setProfileCard(user);

        validationSupportNewPassword = new ValidationSupport();
        if(showId!=null) oldPassword.setVisible(false); else
            validationSupportNewPassword.registerValidator(oldPassword,true, UserFormValidator.getPassword());
        validationSupportNewPassword.registerValidator(newPassword,true, UserFormValidator.getPassword());


        // TODO: Ulepszyć
        ArrayList<String> fp1 = new ArrayList<>();
        fp1.add(newPassword.textProperty().toString());
        newPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            fp1.set(0,newValue);
            newPassword2.setText(null);
        });
        validationSupportNewPassword.registerValidator(newPassword2,true,UserFormValidator.getEqPassword(fp1));

        validationSupportNewData = new ValidationSupport();
        validationSupportNewData.registerValidator(regEmail, true, UserFormValidator.getEmail());
        validationSupportNewData.registerValidator(regName, true, UserFormValidator.getName());
        validationSupportNewData.registerValidator(regSurname, true, UserFormValidator.getSurname());
        validationSupportNewData.registerValidator(regTel, true, UserFormValidator.getPhone());
        validationSupportNewData.registerValidator(regPESEL, true, UserFormValidator.getPesel());
        validationSupportNewData.registerValidator(regCity, true, UserFormValidator.getCity());
        validationSupportNewData.registerValidator(regStreet, true, UserFormValidator.getStreet());
        validationSupportNewData.registerValidator(regZip, true, UserFormValidator.getZip());

        //User form
        regCity.setText(user.getMiasto());
        regZip.setText(user.getKodPocztowy());
        regEmail.setText(user.getEmail());
        regName.setText(user.getImie());
        regSurname.setText(user.getNazwisko());
        regPESEL.setText(user.getPesel());
        regStreet.setText(user.getUlica());
        regTel.setText(user.getTelefon());



    }

    private void setProfileCard(User user) {

        profileDataAvatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
        profileDataFullName.setText(user.getImieNazwisko());
        profileDataEmail.setText(user.getEmail());
        String ranga = "Pracownik";
        if(user.getTyp() == 3) {
            ranga = "Admin";
        } else if (user.getTyp() == 2) {
            ranga = "Kierownik";
        }
        profileDataRanga.setText(ranga);

        profileDataName.setText(user.getImie());
        profileDataSurname.setText(user.getNazwisko());
        profileDataPesel.setText(user.getPesel());
        profileDataBirthday.setText(user.getDataUrodzenia().toString());
        profileDataTel.setText(user.getTelefon());
        profileDataCity.setText(user.getMiasto());
        profileDataStreet.setText(user.getUlica());
        profileDataZip.setText(user.getKodPocztowy());
    }

    @ActionMethod("editProfile")
    public void editProfile() {
        profileCard.setVisible(false);
        newPasswordForm.setVisible(false);
        profileEdit.setVisible(true);
        editProfile.setDisable(true);
        changePassword.setDisable(false);
    }

    @ActionMethod("newPassword")
    public void newPassword() {
        profileCard.setVisible(false);
        profileEdit.setVisible(false);
        newPasswordForm.setVisible(true);
        editProfile.setDisable(false);
        changePassword.setDisable(true);

    }

    @ActionMethod("changeAvatar")
    public void changeAvatar() throws VetoException, FlowException {


        new StockButtons(operationButtons, flowActionHandler).homeAction();
        User user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        File file = fileChooser.showOpenDialog(profileDataAvatar.getScene().getWindow());
        if(file != null) {

            final String[] msg = new String[1];
            Task<Boolean> task = new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    Object[] userAuth = UserAuth.setAvatar(file.getAbsolutePath(), user.getUzytkownikId());
                    msg[0] = (String) userAuth[1];
                    return (boolean) userAuth[0];
                }
            };
            task.setOnRunning(event -> loading.setVisible(true));
            task.setOnFailed(event -> new Alert(Alert.AlertType.ERROR,msg[0]));
            task.setOnSucceeded(event -> loading.setVisible(false));
            new Thread(task).start();
        }
    }

    @ActionMethod("formCancel")
    public void formCancel() {
        editProfile.setDisable(false);
        changePassword.setDisable(false);
        profileCard.setVisible(true);
        profileEdit.setVisible(false);
        newPasswordForm.setVisible(false);
    }

    @ActionMethod("updatePassword")
    public void updatePassword() throws VetoException, FlowException {

        if(validationSupportNewPassword.isInvalid()) {
            validationSupportNewPassword.initInitialDecoration();
        } else {
            if(showId==null)
                user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
            if(showId!=null || BCrypt.checkpw(oldPassword.getText().trim(), user.getHaslo())) {
                Object[] userAuth= UserAuth.updatePassword(newPassword.getText().trim(),user.getUzytkownikId());

                if((boolean)userAuth[0]) {
                    new Alert(Alert.AlertType.INFORMATION,(String) userAuth[1],ButtonType.OK).show();
                    user.setHaslo((String) userAuth[2]);
                    if(showId==null)
                        ApplicationContext.getInstance().register("userSession", user);
                    flowActionHandler.navigate(MyProfileController.class);
                } else {
                    new Alert(Alert.AlertType.ERROR,(String) userAuth[1],ButtonType.OK).show();
                }
            } else {
                oldPassword.setText("");
                new Alert(Alert.AlertType.WARNING,"Hasło nie poprawne",ButtonType.OK).show();
            }
        }
    }
    @ActionMethod("updateUserData")
    public void updateUserData() throws VetoException, FlowException {
        if(validationSupportNewData.isInvalid()) {
            validationSupportNewData.initInitialDecoration();
        } else {
            if(showId==null)
            user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
            user.setImie(regName.getText().trim());
            user.setNazwisko(regSurname.getText().trim());
            user.setEmail(regEmail.getText().trim().toLowerCase());
            user.setUlica(regStreet.getText().trim());
            user.setMiasto(regCity.getText().trim());
            user.setKodPocztowy(regZip.getText().trim());
            user.setPesel(regPESEL.getText().trim());
            user.setTelefon(regTel.getText().trim());
            Object[] userAuth = UserAuth.updateUser(user);
            if((boolean) userAuth[0]) {
                if(showId==null)
                    ApplicationContext.getInstance().register("userSession", user);
                new Alert(Alert.AlertType.INFORMATION, (String) userAuth[1]);
                flowActionHandler.navigate(MyProfileController.class);
            } else {
                new Alert(Alert.AlertType.WARNING,(String) userAuth[1]);
            }
        }
    }

}
