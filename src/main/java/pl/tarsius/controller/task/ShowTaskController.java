package pl.tarsius.controller.task;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.TaskComment;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Kontroler wyświetlający pojedyncze zadanie
 * Created by ireq on 30.04.16.
 */
@FXMLController(value = "/view/app/showTask.fxml", title = "Zadanie - Tarsius")
public class ShowTaskController extends BaseController {
    @FXML private Text taskDesc;
    @FXML private Label taskTitle;
    @FXML private Label taskEndL;
    @FXML private Text taskEnd;
    @FXML private Text taskStart;
    @FXML
    @ActionTrigger("showTaskOwnerProfile")
    private Hyperlink taskProjectAuthor;
    @FXML private Text taskStatus;
    private TaskDb taskDb;

    @FXML private FlowPane taskCommentList;
    @FXML private Pagination taskCommentPg;

    private static Logger loger = LoggerFactory.getLogger(ShowTaskController.class);

    /**
     *  Metoda inicjalizująca kontroler
     */
    @PostConstruct
    public void init() {
        Platform.runLater(() -> userBarSearch.setDisable(true));
        breadCrumb.setSelectedCrumb(task);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        sort="DESC";
        long taskId = (long) ApplicationContext.getInstance().getRegisteredObject("taskId");
        taskDb = TaskDb.getById(taskId);
        ApplicationContext.getInstance().register("taskModel", taskDb);

        if(taskDb.getStatus()==TaskDb.Status.END.getValue())
            new StockButtons(operationButtons,flowActionHandler).inCloseTask();
        else
            new StockButtons(operationButtons,flowActionHandler).inTask();


        taskTitle.setText(taskDb.getName());
        taskDesc.setText(taskDb.getDesc());
        taskProjectAuthor.setText(taskDb.getUserName());
        if(taskDb.getEndDate()!=null)
        taskEnd.setText(taskDb.getEndDate().toString());

        String status = "Zakończone";
        switch (taskDb.getStatus()) {
            case 1: status="Nowe";break;
            case 2: status = "W trakcie";break;
            case 3: status= "Do sprawdzenia";break;
        }
        taskStatus.setText(status);
        try {
            Connection connection = new InitializeConnection().connect();
            new Thread(renderComment(connection,0)).start();

            taskCommentPg.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderComment(connection,newValue.intValue())).start();
            });

        } catch (SQLException e) {
            loger.debug("Komentarze Zadanie", e);
            Alert al = new Alert(Alert.AlertType.ERROR, "Błąd podczas wykonywania zapytania");
            al.show();
        }




    }

    /**
     * Metoda przekierowuje do profilu właściciela projektu
     */
    @ActionMethod("showTaskOwnerProfile")
    public void showTaskOwnerProfile() {
        navigateToProfile(taskDb.getUserId());
    }


    /**
     * Generuje szablon komentarzy
     * @param taskComment  Reprezentacja pojedynczego komentarza
     * @return
     */
    private AnchorPane inTaskComment(TaskComment taskComment) {
        try {
            AnchorPane anchorPane  = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/taskCommentTpl.fxml"));
            Platform.runLater(() -> {
                Text taskCommentDate = (Text) anchorPane.lookup(".taskCommentDate");
                Text taskCommentDesc = (Text) anchorPane.lookup(".taskCommentDesc");
                taskCommentDate.setText(taskComment.getAddDate().toString());
                taskCommentDesc.setText(taskComment.getDesc());
            });

            return anchorPane;
        } catch (IOException e) {
            loger.debug("Ładowanie szablonu komentarzy zadań", e);
            return null;
        }
    }

    /**
     * Metoda zwraca task wyświetlający listę komentarzy
     * @param connection Połączenie do bazy
     * @param page strona na którą ma zostać ustawione stronicowanie
     * @return Task
     */
    private Task<ObservableList<TaskComment>> renderComment(Connection connection,int page) {
        String sql = "select {tpl} from ZadaniaKomentarze where zadanie_id="+taskDb.getId();
        if (sort.length()>0) sql+= " order by data_dodania "+sort;
        int perPage=6;
        String countSql = sql.replace("{tpl}", "count(*)");
        sql=sql.replace("{tpl}", "*");
        sql+= " limit "+page*perPage+","+perPage+"";
        loger.debug("SQL :"+sql);
        loger.debug("SQL count "+countSql);
        DataReader<TaskComment> dr = new JdbcSource<>(connection, sql, TaskComment.jdbcConverter());
        Task<ObservableList<TaskComment>> task = new Task<ObservableList<TaskComment>>() {
            @Override
            protected ObservableList<TaskComment> call() throws Exception {
                ObservableList<TaskComment> observableList = FXCollections.observableArrayList();

                try {
                    ResultSet rs = connection.prepareStatement(countSql).executeQuery();
                    rs.next();
                    long count = rs.getLong(1);
                    int pageCount = (int) Math.ceil(count/perPage);
                    Platform.runLater(() -> {
                        if(pageCount>0) {
                            taskCommentPg.setVisible(true);
                            taskCommentPg.setPageCount(pageCount);
                            taskCommentPg.setCurrentPageIndex(page);
                        }

                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                dr.forEach(taskComment -> observableList.add(taskComment));
                return observableList;
            }
        };
        task.setOnSucceeded(event -> {
            taskCommentList.getChildren().clear();
            task.getValue().forEach(taskComment -> taskCommentList.getChildren().add(inTaskComment(taskComment)));
        });
        return task;
    }


}
