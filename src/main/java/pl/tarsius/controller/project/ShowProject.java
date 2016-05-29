package pl.tarsius.controller.project;


import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.task.ShowTaskController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

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
    @FXML private SegmentedButton sortProjectTask;
    @FXML private VBox taskInProject;
    @FXML private Pagination inProjectTaskPg;

    @FXML private Tab userTab;
    @FXML private Tab taskTab;

    @FXML private CheckBox filtrNew;
    @FXML private CheckBox filtrForTest;
    @FXML private CheckBox filtrEnd;
    @FXML private CheckBox filtrInProgres;

    @FXML Text taskCountNew;
    @FXML Text taskCountInProgress;
    @FXML Text taskCountForTest;
    @FXML Text taskCountEnd;

    @FXML @ActionTrigger("AddToBucket") private Button addToReportBucket;

    private static Logger loger = LoggerFactory.getLogger(ShowProject.class);

    @PostConstruct
    public void init(){

        breadCrumb.setSelectedCrumb(signalProject);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        sort="DESC";
        ToggleButton asc = new ToggleButton("Rosnąco");
        asc.setUserData("ASC");
        ToggleButton desc = new ToggleButton("Malejąco");
        desc.setUserData("DESC");
        desc.setSelected(true);

        ToggleButton ascTask = new ToggleButton("Rosnąco");
        ascTask.setUserData("ASC");
        ToggleButton descTask = new ToggleButton("Malejąco");
        descTask.setUserData("DESC");
        descTask.setSelected(true);

        sortProjectUsers.getButtons().addAll(asc,desc);
        sortProjectTask.getButtons().addAll(ascTask,descTask);

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

            new Thread(renderTasks(connection,0)).start();


            asc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort=(String) asc.getUserData();
                    new Thread(renderUser(connection,0)).start();
                } else desc.setSelected(true);
            });

            desc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort = (String) desc.getUserData();
                    new Thread(renderUser(connection,0)).start();
                } else asc.setSelected(true);
            });


            ascTask.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort=(String) ascTask.getUserData();
                    new Thread(renderTasks(connection,0)).start();
                } else descTask.setSelected(true);
            });

            descTask.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort = (String) descTask.getUserData();
                    new Thread(renderTasks(connection,0)).start();
                } else ascTask.setSelected(true);
            });


            InProjectMemberPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderUser(connection,newValue.intValue())).start();
            });

            inProjectTaskPg.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderTasks(connection,newValue.intValue())).start();
            });

            filtrNew.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());
            filtrForTest.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());
            filtrEnd.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());
            filtrInProgres.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());


            Task<Map<TaskDb.Status,Long>> countTask = new Task<Map<TaskDb.Status, Long>>() {
                @Override
                protected Map<TaskDb.Status, Long> call() throws Exception {
                    return Project.getStatistic(project.getProjekt_id());
                }
            };
            countTask.setOnSucceeded(event -> {
                if(countTask.getValue().containsKey(TaskDb.Status.NEW)) taskCountNew.setText(""+countTask.getValue().get(TaskDb.Status.NEW));
                if(countTask.getValue().containsKey(TaskDb.Status.INPROGRES)) taskCountInProgress.setText(""+countTask.getValue().get(TaskDb.Status.INPROGRES));
                if(countTask.getValue().containsKey(TaskDb.Status.FORTEST)) taskCountForTest.setText(""+countTask.getValue().get(TaskDb.Status.FORTEST));
                if(countTask.getValue().containsKey(TaskDb.Status.END)) taskCountEnd.setText(""+countTask.getValue().get(TaskDb.Status.END));
            });

            new Thread(countTask).start();

        } catch (SQLException e) {
            loger.debug("init()", e);
        }



    }

    @ActionMethod("showAuthorProfile")
    public void showAuthorProfile() throws VetoException, FlowException {
        System.out.println("OOOPEN"+project.getLider());
        // TODO: 29.05.16 Otwieranie profilu
    }


    private AnchorPane inProjectUser(User user){
        try {
            AnchorPane anchorPane  = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/userInProjectTPL.fxml"));
            Circle avatar = (Circle) anchorPane.lookup(".userInProjectAvatar");
            Hyperlink name = (Hyperlink) anchorPane.lookup(".userInProjectName");
            Text task = (Text) anchorPane.lookup(".userInProjectTaskCount");
            Text endTask = (Text) anchorPane.lookup(".userInProjectTaskEndCount");
            avatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
            name.setText(user.getImieNazwisko());
            task.setText(""+0);
            endTask.setText("0");
            return anchorPane;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private AnchorPane inProjectTask(TaskDb taskDb) {
        try {
            AnchorPane anchorPane  = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/taskInProjectTPL.fxml"));
            Hyperlink taskName = (Hyperlink) anchorPane.lookup(".taskName");
            Hyperlink taskUser = (Hyperlink) anchorPane.lookup(".taskUser");
            Label taskUserL = (Label) anchorPane.lookup(".taskUserL");
            Label taskdateLable = (Label) anchorPane.lookup(".taskdateLable");
            Text taskDate = (Text) anchorPane.lookup(".taskDate");
            Label taskStatus = (Label) anchorPane.lookup(".taskStatus");

            String status = "Zakończone";
            switch (taskDb.getStatus()) {
                case 1: status="nowe";break;
                case 2: status = "W trakcie";break;
                case 3: status="Do sprawdzenia";break;
            }
            taskStatus.setText(status);

            taskName.setText(taskDb.getName());
            taskName.setOnAction(event -> {
                try {
                    ApplicationContext.getInstance().register("taskId", taskDb.getId());
                    flowActionHandler.navigate(ShowTaskController.class);
                } catch (VetoException e) {
                    e.printStackTrace();
                } catch (FlowException e) {
                    e.printStackTrace();
                }
            });


            if(taskDb.getUserName().isEmpty()) {
                taskUser.setVisible(false);
                taskUserL.setVisible(false);
            } else taskUser.setText(taskDb.getUserName());



            if(taskDb.getEndDate()==null) {
                taskdateLable.setVisible(false);
                taskDate.setVisible(false);
            } else taskDate.setText(taskDb.getEndDate().toString());
            return anchorPane;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Task<ObservableList<TaskDb>> renderTasks(Connection connection,int page){
        String sql = "select {tpl} from (select z.*,u.imie,u.nazwisko from Zadania z,Uzytkownicy u where z.uzytkownik_id=u.uzytkownik_id and z.projekt_id="+project.getProjekt_id()+"\n" +
                "union \n" +
                "select *,null,null from Zadania where uzytkownik_id is null and projekt_id="+project.getProjekt_id()+") Z";


        int perPage=1;



        String stan="";
        if(filtrEnd.isSelected())
            stan+=","+TaskDb.Status.END.getValue();

        if(filtrNew.isSelected())
            stan+=","+TaskDb.Status.NEW.getValue();

        if(filtrForTest.isSelected())
            stan+=","+TaskDb.Status.FORTEST.getValue();

        if(filtrInProgres.isSelected())
            stan+=","+TaskDb.Status.INPROGRES.getValue();

        stan = stan.replaceFirst(",","");

        if(!stan.isEmpty()) {
            sql+=" where stan in ("+stan+")";
        }

        String countSql = sql.replace("{tpl}", "count(*)");

        loger.debug("IN TEST: "+ stan);
        if (!sort.isEmpty()) sql+= " order by data_dodania "+sort;
        sql+= " limit "+page*perPage+","+perPage+"";
        sql=sql.replace("{tpl}", "*");
        loger.debug("SQL (RenderTask): "+sql);
        loger.debug("SQL (RenderTask) count: "+countSql);
        DataReader<TaskDb> dr = new JdbcSource<>(connection, sql, TaskDb.jdbcConverter());
        Task<ObservableList<TaskDb>> task = new Task<ObservableList<TaskDb>>() {
            @Override
            protected ObservableList<TaskDb> call() throws Exception {
                ObservableList<TaskDb> observableList = FXCollections.observableArrayList();
                try {
                    ResultSet rs = connection.prepareStatement(countSql).executeQuery();
                    rs.next();
                    long count = rs.getLong(1);
                    int pageCount = (int) Math.ceil(count/perPage);
                    if(pageCount==0) {
                        Platform.runLater(() -> inProjectTaskPg.setVisible(false));
                        return observableList;
                    }
                    else {
                        Platform.runLater(() -> {
                            inProjectTaskPg.setVisible(true);
                            inProjectTaskPg.setPageCount(pageCount);
                            inProjectTaskPg.setCurrentPageIndex(page);
                        });
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dr.forEach(taskDb -> observableList.add(taskDb));
                return observableList;
            }
        };

        task.setOnSucceeded(event -> {
            taskInProject.getChildren().clear();
            task.getValue().forEach(taskDb -> taskInProject.getChildren().add(inProjectTask(taskDb)));
        });
        return task;
    }

    private Task<ObservableList<User>> renderUser(Connection connection,int page) {
        String sql = "SELECT {tpl} FROM ProjektyUzytkownicy pu,Uzytkownicy u WHERE projekt_id="+project.getProjekt_id()+" and u.uzytkownik_id=pu.uzytkownik_id";

        if (sort.length()>0) sql+= " order by pu.projekt_uzytkownik "+sort;
        int perPage=1;
        String countSql = sql.replace("{tpl}", "count(*)");
        sql+= " limit "+page*perPage+","+perPage+"";
        String exc = sql.replace("{tpl}", "u.uzytkownik_id,u.imie,u.nazwisko,u.avatar_id,u.email");
        DataReader<User> dataReader = new JdbcSource<>(connection, exc, User.jdbcConverter());
        Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() throws IOException {

                try {
                    ResultSet rs = connection.prepareStatement(countSql).executeQuery();
                    rs.next();
                    long count = rs.getLong(1);
                    int pageCount = (int) Math.ceil(count/perPage);
                    Platform.runLater(() -> {
                        InProjectMemberPagination.setPageCount(pageCount);
                        InProjectMemberPagination.setCurrentPageIndex(page);
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                ObservableList<User> osb = FXCollections.observableArrayList();
                dataReader.forEach(user -> osb.add(user));
                return osb;
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> userInProject.getChildren().clear());
            task.getValue().forEach(user -> Platform.runLater(() -> userInProject.getChildren().add(inProjectUser(user))));
        });
        return task;
    }

}
