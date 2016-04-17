package pl.tarsius.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.controlsfx.control.BreadCrumbBar;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.action.LinkAction;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FlowActionHandler;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.ResponsiveDesign;
import pl.tarsius.util.gui.StockButtons;

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

    @FXMLApplicationContext
    private ApplicationContext applicationContext;

    @ActionHandler
    private FlowActionHandler flowActionHandler;

    //SideBar
    @FXML
    @LinkAction(HomeController.class)
    private Hyperlink sideBarProject;

    @FXML
    private Hyperlink sideBarTaks;
    @FXML
    private Hyperlink sideBarProblems;
    @FXML
    private Hyperlink sideBarInv;

    public void setUserBar(User user) {
        userBarFullName.setText(user.getImie()+" "+user.getNazwisko());
        userBarAvatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
    }


    @PostConstruct
    public void start() {
        User user = (User) applicationContext.getRegisteredObject("userSession");
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
