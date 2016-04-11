package pl.tarsius.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.controlsfx.control.BreadCrumbBar;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.action.LinkAction;
import pl.tarsius.database.Model.User;

/**
 * Created by Jarek on 2016-04-09.
 */
public class BaseController {

    //UserBar
    @FXML
    @LinkAction(StartupController.class)
    protected Hyperlink logOut;

    @FXML protected Circle userBarAvatar;

    @FXML
    @LinkAction(MyProfileController.class)
    protected Hyperlink userBarFullName;

    @FXML protected Label userBarNotification;
    @FXML protected TextField userBarSearch;
    @FXML protected BreadCrumbBar breadCrumb;

    //SideBar
    @FXML
    @LinkAction(HomeController.class)
    protected Hyperlink sideBarProject;

    @FXML protected Label sideBarProjectCount;
    @FXML protected StackPane loading;


    public void setUserBar(User user) {
        userBarFullName.setText(user.getImie()+" "+user.getNazwisko());
        userBarAvatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
    }



}
