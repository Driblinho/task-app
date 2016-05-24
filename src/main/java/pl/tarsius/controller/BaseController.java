package pl.tarsius.controller;


import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.LinkAction;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.controlsfx.control.BreadCrumbBar;
import pl.tarsius.controller.invite.InvitesController;
import pl.tarsius.controller.project.NewProjectController;
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.controller.raport.ReportController;
import pl.tarsius.controller.task.*;
import pl.tarsius.controller.users.UsersListController;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.MyBread;
import pl.tarsius.util.gui.ResponsiveDesign;

import javax.annotation.PostConstruct;
import java.util.HashSet;

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
    //@LinkAction(MyProfileController.class)
    @ActionTrigger("OpenProfile")
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

    @FXML @LinkAction(MyTasksController.class)
    public Hyperlink sideBarTaks;
    @FXML
    @LinkAction(InvitesController.class)
    public Hyperlink sideBarInv;
    @FXML
    @LinkAction(UsersListController.class)
    public Hyperlink sideBarUsers;

    @FXML
    @LinkAction(ReportController.class)
    public Hyperlink sideBarRaports;

    public String search;
    public String sort="";


    public TreeItem<MyBread> root = new TreeItem<MyBread>(new MyBread("Home", HomeController.class));

    public TreeItem<MyBread> signalProject = new TreeItem<MyBread>(new MyBread("Projekt",ShowProject.class));
    public TreeItem<MyBread> task = new TreeItem<MyBread>(new MyBread("Zadanie", ShowTaskController.class));
    public TreeItem<MyBread> noweTask = new TreeItem<>(new MyBread("Dodaj task", NewTaskController.class));
    public TreeItem<MyBread> changeTaskStatus = new TreeItem<>(new MyBread("Zmień status", StatusController.class));
    public TreeItem<MyBread> editTask = new TreeItem<>(new MyBread("Edytuj zadanie", EditTaskController.class));
    public TreeItem<MyBread> newProject = new TreeItem<>(new MyBread("Nowy projekt", NewProjectController.class));
    public TreeItem<MyBread> myTaskList = new TreeItem<>(new MyBread("Zadania", MyTasksController.class));
    public TreeItem<MyBread> myInv = new TreeItem<>(new MyBread("Zaproszenia", InvitesController.class));
    public TreeItem<MyBread> useresManagment = new TreeItem<>(new MyBread("Zarządzanie użytkownikami", UsersListController.class));
    public TreeItem<MyBread> profilView = new TreeItem<>(new MyBread("Profil", MyProfileController.class));
    public TreeItem<MyBread> bucketReport = new TreeItem<>(new MyBread("Profil", MyProfileController.class));
    public TreeItem<MyBread> editTask = new TreeItem<>(new MyBread("Edytuj Zadanie", EditTaskController.class));
    public User user;


    public void setUserBar(User user) {
        userBarFullName.setText(user.getImie()+" "+user.getNazwisko());
        userBarAvatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
    }


    @PostConstruct
    public void start() {
        user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        if(user.isAdmin()) {
            sideBarUsers.setVisible(true);
        }
        setUserBar(user);



        signalProject.getChildren().addAll(task, noweTask);
        task.getChildren().addAll(changeTaskStatus, editTask);


        root.getChildren().addAll(signalProject, newProject, myTaskList, myInv, useresManagment, profilView, bucketReport);
        breadCrumb=new BreadCrumbBar(root);


        Platform.runLater(() -> {
            new ResponsiveDesign((Stage) operationButtons.getParent().getScene().getWindow()).resizeBodyWidth(operationButtons.getParent().getScene().getWindow().getWidth());
            //-3.48% HACK
            double h = operationButtons.getParent().getScene().getWindow().getHeight();
            //h = h-h*0.0348;
            h = h-h*0.0248;
            new ResponsiveDesign((Stage) operationButtons.getParent().getScene().getWindow()).resizeBodyHeight(h);
        });

    }

    @ActionMethod("OpenProfile")
    public void OpenProfile() throws VetoException, FlowException {
        Long l = null;
        ApplicationContext.getInstance().register("showUserID", l);
        flowActionHandler.navigate(MyProfileController.class);
    }

    @ActionMethod("AddToBucket")
    public void AddToBucket() throws VetoException, FlowException {
        long projectId = (long) ApplicationContext.getInstance().getRegisteredObject("projectId");
        HashSet<Long> bucket = (HashSet<Long>) ApplicationContext.getInstance().getRegisteredObject("reportBucket");
        bucket.add(projectId);
        ApplicationContext.getInstance().register("reportBucket", bucket);
        System.out.println("KLIKNOLEM DODALEM");
    }


    public EventHandler<BreadCrumbBar.BreadCrumbActionEvent> crumbActionEventEventHandler() {
        return event -> {
            System.out.println(event.getSelectedCrumb().toString());
            MyBread b = (MyBread) event.getSelectedCrumb().getValue();
            try {
                flowActionHandler.navigate(b.getLink());
            } catch (VetoException e) {
                e.printStackTrace();
            } catch (FlowException e) {
                e.printStackTrace();
            }
        };
    }


}
