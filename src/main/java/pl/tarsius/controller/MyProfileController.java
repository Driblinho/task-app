package pl.tarsius.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.action.ActionMethod;
import org.datafx.controller.flow.action.ActionTrigger;
import org.datafx.controller.flow.action.LinkAction;
import pl.tarsius.database.Model.User;

import javax.annotation.PostConstruct;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/app/profile.fxml", title = "TaskApp - Moje Dane")
public class MyProfileController extends BaseController{

    @FXMLApplicationContext
    private ApplicationContext applicationContext;

    @FXML private AnchorPane profileCard;
    @FXML private AnchorPane profileEdit;

    @FXML
    @ActionTrigger("editProfile")
    private Button editProfile;

    @FXML private Circle profileAvatar;

    @FXML private Button changePassword;

    @PostConstruct
    public void init() {
        User user = (User) applicationContext.getRegisteredObject("userSession");
        setUserBar(user);
        profileAvatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
    }

    @ActionMethod("editProfile")
    public void editProfile() {
        profileCard.setVisible(false);
        profileEdit.setVisible(true);
        editProfile.setDisable(true);
    }

}
