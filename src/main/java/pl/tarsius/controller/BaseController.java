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
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.controlsfx.control.BreadCrumbBar;
import pl.tarsius.controller.invite.InvitesController;
import pl.tarsius.controller.project.NewProjectController;
import pl.tarsius.controller.project.ShowProjectController;
import pl.tarsius.controller.raport.ReportController;
import pl.tarsius.controller.task.*;
import pl.tarsius.controller.users.UsersListController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.MyBread;
import pl.tarsius.util.gui.ResponsiveDesign;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jarek on 2016-04-09.
 */
public abstract class BaseController {
    
    
    @FXML
    @LinkAction(AboutController.class)
    private Hyperlink aboutUs;
    //UserBar
    @FXML
    @LinkAction(StartupController.class)
    public Hyperlink logOut;

    @FXML public Circle userBarAvatar;

    @FXML
    @ActionTrigger("OpenProfile")
    public Hyperlink userBarFullName;

    @FXML public TextField userBarSearch;
    @FXML public BreadCrumbBar breadCrumb;

    @FXML public Label sideBarProjectCount;
    @FXML public Label sideBarTaskCount;
    @FXML public Label sideBarInvCount;
    @FXML public Label sideBarRaportsCount;
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

    public TreeItem<MyBread> signalProject = new TreeItem<MyBread>(new MyBread("Projekt",ShowProjectController.class));
    public TreeItem<MyBread> task = new TreeItem<MyBread>(new MyBread("Zadanie", ShowTaskController.class));
    public TreeItem<MyBread> noweTask = new TreeItem<>(new MyBread("Dodaj task", NewTaskController.class));
    public TreeItem<MyBread> changeTaskStatus = new TreeItem<>(new MyBread("Zmień status", StatusController.class));
    public TreeItem<MyBread> editTask = new TreeItem<>(new MyBread("Edytuj zadanie", EditTaskController.class));
    public TreeItem<MyBread> newProject = new TreeItem<>(new MyBread("Nowy projekt", NewProjectController.class));
    public TreeItem<MyBread> myTaskList = new TreeItem<>(new MyBread("Zadania", MyTasksController.class));
    public TreeItem<MyBread> myInv = new TreeItem<>(new MyBread("Zaproszenia", InvitesController.class));
    public TreeItem<MyBread> useresManagment = new TreeItem<>(new MyBread("Zarządzanie użytkownikami", UsersListController.class));
    public TreeItem<MyBread> profilView = new TreeItem<>(new MyBread("Profil", MyProfileController.class));
    public TreeItem<MyBread> bucketReport = new TreeItem<>(new MyBread("Raporty", ReportController.class));
    public User user;


    /**
     * Setter for property 'userBar'.
     *
     * @param user Value to set for property 'userBar'.
     */
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
            new ResponsiveDesign((Stage) sideBarProject.getParent().getScene().getWindow()).resizeBodyHeight(h);
        });

        new Thread(countSidebar()).start();


    }

    public Task<HashMap<String, Long>> countSidebar(){

        Task<HashMap<String, Long>> localTask = new Task<HashMap<String, Long>>() {
            @Override
            protected HashMap<String, Long> call() throws Exception {
                HashMap<String, Long> data = new HashMap<>();
                data.put("projekty", 0L);
                data.put("zadania", 0L);
                data.put("zaproszenia", 0L);
                data.put("raporty", 0L);

                HashSet<Long> bucket = (HashSet<Long>) ApplicationContext.getInstance().getRegisteredObject("reportBucket");
                if(bucket!=null && bucket.size()>0)
                    data.put("raporty", (long) bucket.size());

                try {
                    Connection connection = new InitializeConnection().connect();
                    Statement st = connection.createStatement();
                    ResultSet rs = st.executeQuery("select COUNT(projekt_id) from Projekty;");
                    rs.next();

                    long sbp = rs.getLong(1);
                    data.put("projekty", sbp);

                    rs = st.executeQuery("SELECT COUNT(uzytkownik_id) FROM Zadania where uzytkownik_id=" + user.getUzytkownikId());
                    rs.next();
                    long sbtc = rs.getLong(1);
                    data.put("zadania", sbtc);

                    rs = st.executeQuery("SELECT COUNT(uzytkownik_id) from Zaproszenia WHERE uzytkownik_id=" + user.getUzytkownikId());
                    rs.next();
                    long sbic = rs.getLong(1);
                    data.put("zaproszenia", sbic);


                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    return data;
                }
            }
        };

        localTask.setOnSucceeded(event -> {
            HashMap<String, Long> loc = localTask.getValue();
            Platform.runLater(() -> {
                sideBarProjectCount.setText(""+loc.get("projekty"));
                sideBarTaskCount.setText(loc.get("zadania") + "");
                sideBarInvCount.setText(loc.get("zaproszenia") + "");
                sideBarRaportsCount.setText(loc.get("raporty") + "");
            });

        });




        return localTask;

    }


    /**
     * Metoda otwierająca profil obecnie zalogowanego użytkownika
     * @throws VetoException
     * @throws FlowException
     */
    @ActionMethod("OpenProfile")
    public void OpenProfile() throws VetoException, FlowException {
        navigateToProfile(null);
    }

    @ActionMethod("AddToBucket")
    public void AddToBucket() throws VetoException, FlowException {
        long projectId = (long) ApplicationContext.getInstance().getRegisteredObject("projectId");
        HashSet<Long> bucket = (HashSet<Long>) ApplicationContext.getInstance().getRegisteredObject("reportBucket");
        bucket.add(projectId);
        ApplicationContext.getInstance().register("reportBucket", bucket);
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

    /**
     * Metoda odpowiada za nawigację do profilu użytkownika
     * @param profileID ID użytkownika którego profil ma zostać wyświetlony
     * @throws VetoException
     * @throws FlowException
     */
    public void navigateToProfile(Long profileID) {
        user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        if(profileID!=null && user.getUzytkownikId()==profileID)
            profileID=null;

        ApplicationContext.getInstance().register("showUserID", profileID); //Usunięcie wartość odpowiadającej za otwieranie profilu niezalogowanego użytkownika
        DataFxEXceptionHandler.navigateQuietly(flowActionHandler,MyProfileController.class);
    }


}
