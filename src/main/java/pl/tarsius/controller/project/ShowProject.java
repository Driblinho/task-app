package pl.tarsius.controller.project;


import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.context.FXMLApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.controlsfx.control.SegmentedButton;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.ProfileController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Invite;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.Executor;

/**
 * Created by Ireneusz Kuliga on 15.04.16.
 */
@FXMLController(value = "/view/app/widokprojektu.fxml", title = "TaskApp")
public class ShowProject extends BaseController{

    @FXML private Label inprojectTitle;
    @FXML private Text inprojectDesc;
    @FXML private Text inprojectDataStart;
    @FXML private Text inprojectDataStop;
    @FXML private Label inprojectDataStopLabel;
    @FXML private Pagination InProjectMemberPagination;
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML
    @ActionTrigger("showAuthorProfile")
    private Hyperlink inprojectAuthor;

    @FXML
    private VBox userInProject;

    private Project project;

    @FXML private SegmentedButton sortProjectUsers;

    @PostConstruct
    public void init(){
        sort="DESC";
        ToggleButton asc = new ToggleButton("Rosnąco");
        asc.setSelected(true);
        asc.setUserData("ASC");
        ToggleButton desc = new ToggleButton("Malejąco");
        desc.setUserData("DESC");
        sortProjectUsers.getButtons().addAll(asc,desc);



        project = Project.getProject((long)ApplicationContext.getInstance().getRegisteredObject("projectId"));
        ApplicationContext.getInstance().register("projectModel", project);
        new StockButtons(operationButtons, flowActionHandler).inProjectButton();
       ApplicationContext.getInstance().register("projectLider", project.getLider());
            inprojectTitle.setText(project.getNazwa());
            inprojectDesc.setText(project.getOpis());
            inprojectDataStart.setText(project.getData_dodania().toString());
            inprojectAuthor.setText(project.getLiderImieNazwisko());
            Timestamp dz = project.getData_zakonczenia();
            if(dz!=null) {
                inprojectDataStop.setText(dz.toString());
            } else {
                inprojectDataStop.setVisible(false);
                inprojectDataStopLabel.setVisible(false);
            }

        InProjectMemberPagination.setPageCount(1);
        try {
            Connection connection = new InitializeConnection().connect();
            Task<ObservableList<User>> task = renderUser(connection,0);
            new Thread(task).start();


            asc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort=(String) asc.getUserData();

                    Task<ObservableList<User>> t = renderUser(connection,0);
                    new Thread(t).start();
                } else desc.setSelected(true);
            });

            desc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort = (String) desc.getUserData();

                    new Thread(renderUser(connection,0)).start();
                } else asc.setSelected(true);
            });

            InProjectMemberPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderUser(connection,newValue.intValue())).start();
            });



        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

    @ActionMethod("showAuthorProfile")
    public void showAuthorProfile() throws VetoException, FlowException {
        System.out.println("OOOPEN"+project.getLider());

        flowActionHandler.navigate(ProfileController.class);

    }


    private AnchorPane inProjectUser(User user){
        try {
            AnchorPane anchorPane  = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/userInProjectTPL.fxml"));
            Circle circle = (Circle) anchorPane.lookup(".userInProjectAvatar");
            Hyperlink name = (Hyperlink) anchorPane.lookup(".userInProjectName");
            Text task = (Text) anchorPane.lookup(".userInProjectTaskCount");
            Text endTask = (Text) anchorPane.lookup(".userInProjectTaskEndCount");

            name.setText(user.getImieNazwisko());
            task.setText(""+0);
            endTask.setText("0");

            return anchorPane;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    private Task<ObservableList<User>> renderUser(Connection connection,int page) {
        String sql = "SELECT {tpl} FROM ProjektyUzytkownicy pu,Uzytkownicy u WHERE projekt_id="+project.getProjekt_id()+" and u.uzytkownik_id=pu.uzytkownik_id";

        if (sort.length()>0) sql+= " order by pu.projekt_uzytkownik "+sort;
        int perPage=1;
        String countSql = sql.replace("{tpl}", "count(*)");
        sql+= " limit "+page*perPage+","+perPage+"";
        String exc = sql.replace("{tpl}", "u.uzytkownik_id,u.imie,u.nazwisko,u.avatar_id");
        DataReader<User> dataReader = new JdbcSource<>(connection, exc, User.jdbcConverter());
        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() throws IOException {
                //dataReader.forEach(user -> userInProject.getChildren().add(inProjectUser(user)));
                ObservableList<User> osb = FXCollections.observableArrayList();
                do {
                    osb.add(dataReader.get());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }while (dataReader.next());
                return osb;
            }
        };
        task.setOnRunning(event -> {
            try {

                ResultSet rs = connection.prepareStatement(countSql).executeQuery();
                System.out.println("COUNT"+countSql);
                System.out.println(exc);
                rs.next();
                long count = rs.getLong(1);
                int pageCount = (int) Math.ceil(count/perPage);
                InProjectMemberPagination.setPageCount(pageCount);
                InProjectMemberPagination.setCurrentPageIndex(page);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });

        task.setOnSucceeded(event -> {
            userInProject.getChildren().clear();
            task.getValue().forEach(user -> userInProject.getChildren().add(inProjectUser(user)));
        });
        return task;
    }

}
