package pl.tarsius.controller;


import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.context.FXMLApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.project.ShowProjectController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Klasa startowa po zalogowaniu
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/app/start.fxml", title = "Projekty - Tarsius")
public class HomeController extends BaseController{



    @FXMLApplicationContext
    private ApplicationContext applicationContext;
    @FXML private Circle profileAvatar;

    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML
    private Pane contentFlow;
    private Logger loger;

    @FXML
    private Pagination pagination;

    @FXML
    private SegmentedButton sortButtons;


    private boolean isBoss;
    private  boolean showMyproject;

    private static final int PER_PAGE = 10;


    private Service<ObservableList<Project>> renderService;

    @FXML private CheckBox endProject;
    @FXML private CheckBox myProjects;
    @FXML private CheckBox collProjects;
    @FXML private CheckBox managerProjects;

    @FXML private GridPane projectListLoading;

    /**
     * Inicjalizacja kontroler
     */
    @PostConstruct
    public void init() {

        isBoss = false;
        showMyproject = false;

        ToggleButton asc = new ToggleButton("Rosnąco");
        asc.setSelected(true);
        asc.setUserData("ASC");
        ToggleButton desc = new ToggleButton("Malejąco");
        desc.setUserData("DESC");
        sortButtons.getButtons().addAll(asc,desc);




        loger = LoggerFactory.getLogger(getClass());
        new StockButtons(operationButtons,flowActionHandler).homeAction();

        InitializeConnection initializeConnection = new InitializeConnection();
        try {
            Connection conn = initializeConnection.connect();
            renderService = renderProject(null,0,conn);
            renderService.start();

            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    search = userBarSearch.getText().trim();
                    reloadProject(conn);
                }
            });

            asc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort=(String) asc.getUserData();
                    reloadProject(conn);
                } else desc.setSelected(true);
            });

            desc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort = (String) desc.getUserData();
                    reloadProject(conn);

                } else asc.setSelected(true);
            });

            endProject.setOnMouseClicked(event -> reloadProject(conn));
            myProjects.setOnMouseClicked(event -> reloadProject(conn));
            collProjects.setOnMouseClicked(event -> reloadProject(conn));
            managerProjects.setOnMouseClicked(event -> reloadProject(conn));



            pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                if(renderService.isRunning())
                    renderService.cancel();
                renderService = renderProject(search,newValue.intValue(),conn);
                renderService.start();

            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Metoda tworząca listę projektów
     * @param project Obiekt {@link Project}
     */
    private void createLis(Project project) {
        if(project==null) return;
        try {
            Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource("view/smallProjectTpl.fxml"));
            Hyperlink title = (Hyperlink) parent.lookup(".smallProjectTitle");
            Hyperlink author = (Hyperlink) parent.lookup(".smallProjectAuthor");
            Text end = (Text) parent.lookup(".smallProjectDateEnd");
            Label endL = (Label) parent.lookup(".smallProjectDateEndLabel");
            //Text status = (Text) parent.lookup(".smallProjectStatus");
            Text desc = (Text) parent.lookup(".smallProjectDesc");

            Text countNew = (Text) parent.lookup(".smallProjectNewCount");
            Text countForTest = (Text) parent.lookup(".smallProjectForTestCount");
            Text countInProgres = (Text) parent.lookup(".smallProjectInProgresCount");
            Text countEnd = (Text) parent.lookup(".smallProjectEndCount");


            author.setText(project.getLiderImieNazwisko());

            author.setOnAction(event -> navigateToProfile(project.getLider()));

            String s = project.getOpis().replaceAll("[\\t\\n\\r]"," ");
            try {s=s.substring(0,150);} catch (IndexOutOfBoundsException e) {}
            desc.setText(s);
            title.setText(project.getNazwa());
            title.setUserData(project.getProjektId());

            if(project.getDataZakonczenia()==null) {
                end.setVisible(false);
                endL.setVisible(false);
            } else end.setText(project.getDataZakonczenia().toString());


            title.setOnAction(event -> {
                long id = (long) ((Hyperlink)event.getSource()).getUserData();
                ApplicationContext.getInstance().register("projectId",id);
                DataFxEXceptionHandler.navigateQuietly(flowActionHandler, ShowProjectController.class);
            });


            Task<Map<TaskDb.Status,Long>> countTask = new Task<Map<TaskDb.Status, Long>>() {
                @Override
                protected Map<TaskDb.Status, Long> call() throws Exception {
                    return Project.getStatistic(project.getProjektId());
                }
            };
            countTask.setOnSucceeded(event -> {
                if(countTask.getValue().containsKey(TaskDb.Status.NEW)) countNew.setText(""+countTask.getValue().get(TaskDb.Status.NEW));
                if(countTask.getValue().containsKey(TaskDb.Status.INPROGRES)) countInProgres.setText(""+countTask.getValue().get(TaskDb.Status.INPROGRES));
                if(countTask.getValue().containsKey(TaskDb.Status.FORTEST)) countForTest.setText(""+countTask.getValue().get(TaskDb.Status.FORTEST));
                if(countTask.getValue().containsKey(TaskDb.Status.END)) countEnd.setText(""+countTask.getValue().get(TaskDb.Status.END));
            });

            new Thread(countTask).start();



            contentFlow.getChildren().add(parent);

        } catch (IOException e) {
            loger.debug("createListElem", e);
        }


    }


    /**
     * Metoda dostarczająca {@link Service}
     * @param search parametr wyszukiwania
     * @param page obecna strona projektów
     * @param connection połączenie z bazą
     * @return Service wyświetlający projekty
     */
    private Service<ObservableList<Project>> renderProject(String search, int page, Connection connection) {



        Service<ObservableList<Project>> service = new Service<ObservableList<Project>>() {

            @Override
            protected Task<ObservableList<Project>> createTask() {
                User u = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
                contentFlow.getChildren().clear();
                int perPage = PER_PAGE;

                String sql = "select * from Projekty p,(select imie as l_imie,nazwisko as l_nazwisko,uzytkownik_id l_id from Uzytkownicy)au where p.lider=au.l_id";

                if(endProject.isSelected())
                    sql+=" and p.status=0";
                else
                    sql+=" and p.status=1";

                if(myProjects.isSelected() && !collProjects.isSelected())
                    sql+=" and l_id="+u.getUzytkownikId();

                if(collProjects.isSelected()) {
                    String lider = myProjects.isSelected()?"":"and lider=0";
                    sql+=" and p.projekt_id in (select distinct projekt_id from ProjektyUzytkownicy where uzytkownik_id="+user.getUzytkownikId()+" "+lider+")";
                }

                if(managerProjects.isSelected())
                    sql+=" and p.lider in (select uzytkownik_id from Uzytkownicy WHERE typ in (2,3))";

                if(search!=null && search.length()>0) sql+=" and (nazwa like '%"+search+"%' or opis like '%"+search+"%')";





                sql+= " order by p.data_dodania "+sort;

                String countSql = sql.replace("*","count(*)");

                sql+= " limit "+page*perPage+","+perPage+"";

                loger.debug("SQL (count): "+countSql);
                loger.debug("SQL: "+sql);
                try {
                    PreparedStatement ps = connection.prepareStatement(countSql);
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    long count = rs.getLong(1);
                    pagination.setCurrentPageIndex(page);
                    int pageCount = (int) Math.ceil((float)count/perPage);
                    if(pageCount>0)
                        pagination.setPageCount(pageCount);
                    else pagination.setPageCount(1);

                } catch (SQLException e) {
                    loger.debug("RENDER projects ",e);
                }

                DataReader<Project> dataReader = new JdbcSource<>(connection, sql, Project.jdbcConverter());

                Task<ObservableList<Project>> task = new Task<ObservableList<Project>>() {
                    @Override
                    protected ObservableList<Project> call() throws InterruptedException, IOException {
                        ObservableList<Project> observableList = FXCollections.observableArrayList();
                        dataReader.forEach(project -> observableList.add(project));
                        return observableList;
                    }
                };
                task.setOnRunning(event -> projectListLoading.setVisible(true));
                task.setOnSucceeded(event -> {
                    contentFlow.getChildren().clear();
                    task.getValue().forEach(project -> createLis(project));
                    projectListLoading.setVisible(false);
                });
                return task;
            }
        };

        return service;
    }

    /**
     * Metoda przeładowująca listę projektów
     * @param connection połączenia z bazą
     */
    private void reloadProject(Connection connection) {
        if(renderService.isRunning()) renderService.cancel();
        renderService = renderProject(search,0,connection);
        renderService.start();
    }


}
