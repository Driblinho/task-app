package pl.tarsius.controller.project;


import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
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
import javafx.scene.layout.*;
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
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

/** Kontroler odpowiadający za Wyświetlanie pojedynczego projektu
 * Created by Ireneusz Kuliga on 15.04.16.
 */
@FXMLController(value = "/view/app/widokprojektu.fxml", title = "Projekt - Tarsius")
public class ShowProjectController extends BaseController{

    /**
     * Mapowanie {@link Label} z FXML (Tytuł projektu)
     */
    @FXML private Label inprojectTitle;
    /**
     * Mapowanie {@link Text} z FXML (Opis projektu)
     */
    @FXML private Text inprojectDesc;
    /**
     * Mapowanie {@link Text} z FXML (Data rozpoczęcia projektu)
     */
    @FXML private Text inprojectDataStart;
    /**
     * Mapowanie {@link Text} z FXML (Data zakończenia projektu)
     */
    @FXML private Text inprojectDataStop;
    /**
     * Mapowanie {@link Label} z FXML (Nagłówek zakończenia projektu)
     */
    @FXML private Label inprojectDataStopLabel;
    /**
     * Mapowanie {@link Pagination} z FXML (Stronicowanie uczestników projektu)
     */
    @FXML private Pagination InProjectMemberPagination;
    /**
     * DataFX {@link FlowActionHandler}
     */
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    /**
     * {@link Hyperlink} obsługujący wyświetlanie profilu użytkownika projektu
     */
    @FXML
    @ActionTrigger("showAuthorProfile")
    private Hyperlink inprojectAuthor;

    /**
     * {@link VBox} na uczestników projektu
     */
    @FXML
    private VBox userInProject;

    /**
     * Pole na dane otwartego projektu
     */
    private Project project;

    /**
     * Sortowanie użytkowników
     */
    @FXML private SegmentedButton sortProjectUsers;

    /**
     * Sortowanie Zadań
     */
    @FXML private SegmentedButton sortProjectTask;

    /**
     * {@link VBox} na zadania w projekcie
     */
    @FXML private VBox taskInProject;
    /**
     * Stronicowanie zadań
     */
    @FXML private Pagination inProjectTaskPg;

    /**
     * Zakładka użytkownicy
     */
    @FXML private Tab userTab;
    /**
     * Zakładka zadania
     */
    @FXML private Tab taskTab;

    /**
     * {@link CheckBox} na filtr (Nowe zadania)
     */
    @FXML private CheckBox filtrNew;
    /**
     * {@link CheckBox} na filtr (Do sprawdzenia)
     */
    @FXML private CheckBox filtrForTest;
    /**
     * {@link CheckBox} na filtr (Zakończone)
     */
    @FXML private CheckBox filtrEnd;
    /**
     * {@link CheckBox} na filtr (W trakcie)
     */
    @FXML private CheckBox filtrInProgres;
    /**
     * {@link CheckBox} na filtr (Moje zadania)
     */
    @FXML private CheckBox filtrOnlyMy;

    /**
     * {@link Text} na ilość zadań nowych
     */
    @FXML Text taskCountNew;
    /**
     * {@link Text} na ilość zadań w trakcie
     */
    @FXML Text taskCountInProgress;
    /**
     * {@link Text} na ilość zadań do sprawdzenia
     */
    @FXML Text taskCountForTest;
    /**
     * {@link Text} na ilość zadań zakończonych
     */
    @FXML Text taskCountEnd;


    /**
     * {@link Button} obsługujący dodawanie projektu do koszyka
     */
    @FXML
    @ActionTrigger("AddToBucket") private Button addToReportBucket;

    /**
     * {@link Logger}
     */
    private static Logger loger = LoggerFactory.getLogger(ShowProjectController.class);

    /**
     * Ilość użytkowników i zadań na stronie
     */
    private static final int USER_AND_TASK_PER_PAGE = 6;

    /**
     * Metoda inicjalizująca kontroler
     */
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
        user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        ApplicationContext.getInstance().register("projectModel", project);
        new StockButtons(operationButtons, flowActionHandler).inProjectButton();
        ApplicationContext.getInstance().register("projectLider", project.getLider());
            inprojectTitle.setText(project.getNazwa());
            inprojectDesc.setText(project.getOpis());
            inprojectDataStart.setText(project.getDataDodania().toString());
            inprojectAuthor.setText(project.getLiderImieNazwisko());
            Timestamp dz = project.getDataZakonczenia();
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
            filtrOnlyMy.setOnMouseClicked(event -> new Thread(renderTasks(connection,0)).start());


            Task<Map<TaskDb.Status,Long>> countTask = new Task<Map<TaskDb.Status, Long>>() {
                @Override
                protected Map<TaskDb.Status, Long> call() throws Exception {
                    return Project.getStatistic(project.getProjektId());
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

    /**
     * Metoda obsługująca przechodzenie do profilu lider projektu
     */
    @ActionMethod("showAuthorProfile")
    public void showAuthorProfile() {
        navigateToProfile(project.getLider());
    }


    /**
     * Metoda generująca pojedynczy wiersz z użytkownikami uczestniczącymi w projekcie
     * @param userData Obiekt reprezentujący dane użytkownika
     * @return AnchorPane - wiersz z użytkownikiem
     */
    private AnchorPane inProjectUser(User userData){
        try {
            AnchorPane anchorPane  = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/userInProjectTPL.fxml"));
            Circle avatar = (Circle) anchorPane.lookup(".userInProjectAvatar");
            Hyperlink name = (Hyperlink) anchorPane.lookup(".userInProjectName");
            Text task = (Text) anchorPane.lookup(".userInProjectTaskCount");
            Text endTask = (Text) anchorPane.lookup(".userInProjectTaskEndCount");
            Button removeBtn = (Button) anchorPane.lookup(".removeUserFormProject");

            if((project.getLider()==user.getUzytkownikId() || user.isAdmin()) && userData.getUzytkownikId()!=project.getLider() )
                removeBtn.setVisible(true);

            removeBtn.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Usuwanie użytkownika z projektu");
                alert.setHeaderText("LUsuwasz użytkownika");
                alert.setContentText("Na pewno usunąć użytkownika?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    Object[] msg = Project.removeUserFormProject(userData.getUzytkownikId(), project.getProjektId());
                    Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, (String) msg[1]);
                    if((boolean)msg[0]) {
                        infoAlert.show();
                        DataFxEXceptionHandler.navigateQuietly(flowActionHandler,getClass());
                    } else {
                        infoAlert.setAlertType(Alert.AlertType.ERROR);
                        infoAlert.show();
                    }
                }
            });

            avatar.setFill(new ImagePattern(new Image(userData.getAvatarUrl())));
            name.setOnAction(event -> navigateToProfile(userData.getUzytkownikId()));
            name.setText(userData.getImieNazwisko());
            task.setText(""+0);
            endTask.setText("0");
            return anchorPane;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Nakładka na metodę statyczną generującą szablon zadania
     * @param taskDb Obiekt reprezentujący dane zadania
     * @return Wiersz z zadaniem
     */
    private GridPane inProjectTask(TaskDb taskDb) {
        return inProjectTaskTpl(taskDb,flowActionHandler);
    }

    /**
     * Metoda generująca pojedynczy wiersz z zadaniem dostępne statycznie dla innych kontrolerów
     * @param taskDb Obiekt reprezentujący dane zadania
     * @param flowActionHandler {@link FlowActionHandler} DataFX
     * @return wiersz z zadaniem
     */
    public static GridPane inProjectTaskTpl(TaskDb taskDb, FlowActionHandler flowActionHandler) {
        try {
            GridPane anchorPane  = FXMLLoader.load(ShowProjectController.class.getClassLoader().getResource("view/app/taskInProjectTPL.fxml"));
            Hyperlink taskName = (Hyperlink) anchorPane.lookup(".taskName");
            Hyperlink taskUser = (Hyperlink) anchorPane.lookup(".taskUser");
            Label taskUserL = (Label) anchorPane.lookup(".taskUserL");
            Label taskdateLable = (Label) anchorPane.lookup(".taskdateLable");
            Text taskDate = (Text) anchorPane.lookup(".taskDate");
            Label taskStatus = (Label) anchorPane.lookup(".taskStatus");

            taskUser.setOnAction(event -> new ShowProjectController().navigateToProfile(taskDb.getUserId()));

            String status = "Zakończone";
            switch (taskDb.getStatus()) {
                case 1: status="nowe";break;
                case 2: status = "W trakcie";break;
                case 3: status="Do sprawdzenia";break;
            }
            taskStatus.setText(status);

            taskName.setText(taskDb.getName());
            taskName.setOnAction(event -> {
                ApplicationContext.getInstance().register("projectId", taskDb.getProjectId());
                ApplicationContext.getInstance().register("taskId", taskDb.getId());
                DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowTaskController.class);
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
            loger.debug("Brak szablonu projektu", e);
            return null;
        }
    }


    /** Metoda generująca task wyświetlający zadania
     * @param connection Połączeni z bazą danych
     * @param page Strona na jaką ma zostać ustawione stronicowanie
     * @return Task wyświetlający listę zadań
     */
    private Task<ObservableList<TaskDb>> renderTasks(Connection connection,int page){
        String sql = "select {tpl} from (select z.*,u.imie,u.nazwisko from Zadania z,Uzytkownicy u where z.uzytkownik_id=u.uzytkownik_id and z.projekt_id="+project.getProjektId()+"\n" +
                "union \n" +
                "select *,null,null from Zadania where uzytkownik_id is null and projekt_id="+project.getProjektId()+") Z";


        int perPage=USER_AND_TASK_PER_PAGE;



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

        if(filtrOnlyMy.isSelected()) {
            if(stan.isEmpty())
                sql+=" where";
            else sql+=" and";
            sql+=" uzytkownik_id="+user.getUzytkownikId();
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
                    Platform.runLater(() -> {
                            inProjectTaskPg.setPageCount(pageCount);
                            inProjectTaskPg.setCurrentPageIndex(page);
                        });
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
    /** Metoda generująca task wyświetlający użytkowników
     * @param connection Połączeni z bazą danych
     * @param page Strona na jaką ma zostać ustawione stronicowanie
     * @return Task wyświetlający listę użytkowników
     */
    private Task<ObservableList<User>> renderUser(Connection connection,int page) {
        String sql = "SELECT {tpl} FROM ProjektyUzytkownicy pu,Uzytkownicy u WHERE projekt_id="+project.getProjektId()+" and u.uzytkownik_id=pu.uzytkownik_id";

        if (sort.length()>0) sql+= " order by pu.projekt_uzytkownik "+sort;
        int perPage=USER_AND_TASK_PER_PAGE;
        String countSql = sql.replace("{tpl}", "count(*)");
        sql+= " limit "+page*perPage+","+perPage+"";
        String exc = sql.replace("{tpl}", "u.*");
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
