package pl.tarsius.controller.task;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.database.Model.User;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ireq on 30.04.16.
 */
@FXMLController(value = "/view/app/mytasklist.fxml", title = "Moje zadania - Tarsius")
public class MyTasksController extends BaseController {

    @FXML private CheckBox filtrNew;
    @FXML private CheckBox filtrForTest;
    @FXML private CheckBox filtrEnd;
    @FXML private CheckBox filtrInProgres;
    @FXML private SegmentedButton sortButtons;
    @FXML private VBox taskList;
    @FXML private Pagination pagination;
    private static Logger loger = LoggerFactory.getLogger(MyTasksController.class);
    private static int PER_PAGE = 8;


    @PostConstruct
    public void init() {

        breadCrumb.setSelectedCrumb(myTaskList);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        try {
            Connection connection = new InitializeConnection().connect();
            new Thread(renderTasks(connection,0)).start();

            pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderTasks(connection,newValue.intValue())).start();
            });

            filtrNew.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());
            filtrForTest.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());
            filtrEnd.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());
            filtrInProgres.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());

        } catch (SQLException e) {
            e.printStackTrace();
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

            taskUser.setOnAction(event -> navigateToProfile(taskDb.getUserId()));

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

    private Task<ObservableList<TaskDb>> renderTasks(Connection connection, int page){
        String sql = "select {tpl} from Zadania z,Uzytkownicy u where z.uzytkownik_id=u.uzytkownik_id and z.uzytkownik_id="+user.getUzytkownikId();


        int perPage=PER_PAGE;



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
            sql+=" and stan in ("+stan+")";
        }

        String countSql = sql.replace("{tpl}", "count(*)");
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
                    int pageCount = (int) Math.ceil(count/(float)perPage);
                    if(pageCount==0) {
                        Platform.runLater(() -> pagination.setVisible(false));
                    } else {
                        Platform.runLater(() -> {
                            pagination.setVisible(true);
                            pagination.setPageCount(pageCount);
                            pagination.setCurrentPageIndex(page);
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
            taskList.getChildren().clear();
            task.getValue().forEach(taskDb -> taskList.getChildren().add(inProjectTask(taskDb)));
        });
        return task;
    }
}
