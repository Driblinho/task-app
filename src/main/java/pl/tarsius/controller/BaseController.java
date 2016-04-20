package pl.tarsius.controller;


import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.LinkAction;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.controlsfx.control.BreadCrumbBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.invite.InvitesController;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.ResponsiveDesign;

import javax.annotation.PostConstruct;

/**
 * Created by Jarek on 2016-04-09.
 */
public abstract class BaseController {

    //UserBar
    @FXML
    @LinkAction(StartupController.class)
    public Hyperlink logOut;

    @FXML public Circle userBarAvatar;

    @FXML
    @LinkAction(MyProfileController.class)
    public Hyperlink userBarFullName;

    @FXML public Label userBarNotification;
    @FXML public TextField userBarSearch;
    @FXML public BreadCrumbBar breadCrumb;

    @FXML public Label sideBarProjectCount;
    @FXML public StackPane loading;
    @FXML public VBox operationButtons;

    @ActionHandler
    public FlowActionHandler flowActionHandler;

    //SideBar
    @FXML
    @LinkAction(HomeController.class)
    public Hyperlink sideBarProject;

    @FXML
    public Hyperlink sideBarTaks;
    @FXML
    public Hyperlink sideBarProblems;
    @FXML
    @LinkAction(InvitesController.class)
    public Hyperlink sideBarInv;



    public void setUserBar(User user) {
        userBarFullName.setText(user.getImie()+" "+user.getNazwisko());
        userBarAvatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
    }


    @PostConstruct
    public void start() {

        User user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        setUserBar(user);
        Platform.runLater(() -> {
            new ResponsiveDesign((Stage) operationButtons.getParent().getScene().getWindow()).resizeBodyWidth(operationButtons.getParent().getScene().getWindow().getWidth());
            //-3.48% HACK
            double h = operationButtons.getParent().getScene().getWindow().getHeight();
            h = h-h*0.0348;
            new ResponsiveDesign((Stage) operationButtons.getParent().getScene().getWindow()).resizeBodyHeight(h);
        });

    }



}
