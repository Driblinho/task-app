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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/app/start.fxml", title = "TaskApp - Home")
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

    @FXML
    private SegmentedButton filtrButtons;

    private boolean isBoss;
    private  boolean showMyproject;

    private int PER_PAGE = 6;

    @FXML private CheckBox showAllProject;
    @FXML private CheckBox showEndProject;
    @FXML private CheckBox showEndOnlyProject;


    @PostConstruct
    public void init() throws VetoException, FlowException {

        contentFlow.setPrefHeight(400.0);

        isBoss = false;
        showMyproject = false;

        ToggleButton asc = new ToggleButton("Rosnąco");
        asc.setSelected(true);
        asc.setUserData("ASC");
        ToggleButton desc = new ToggleButton("Malejąco");
        desc.setUserData("DESC");
        sortButtons.getButtons().addAll(asc,desc);

        ToggleButton boss = new ToggleButton("Kierownicze");
        ToggleButton my = new ToggleButton("Moje Projekty");
        ToggleButton collaborate = new ToggleButton("Uczestnicze");

        filtrButtons.getButtons().addAll(my,boss,collaborate);




        loger = LoggerFactory.getLogger(getClass());
        new StockButtons(operationButtons,flowActionHandler).homeAction();

        InitializeConnection initializeConnection = new InitializeConnection();
        try {
            Connection conn = initializeConnection.connect();
            new Thread(renderProject(null,0,conn)).start();

            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    search = userBarSearch.getText().trim();
                    new Thread(renderProject(search,0,conn)).start();
                }
            });

            asc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort=(String) asc.getUserData();
                    new Thread(renderProject(search,0,conn)).start();
                } else desc.setSelected(true);
            });

            desc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort = (String) desc.getUserData();
                    new Thread(renderProject(search,0,conn)).start();
                } else asc.setSelected(true);
            });



            boss.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    isBoss = true;
                    new Thread(renderProject(search, 0, conn)).start();
                } else
                    isBoss = false;

            });
            my.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    showMyproject=true;
                    new Thread(renderProject(search,0,conn)).start();
                } else {
                    showMyproject=false;
                }


            });
            collaborate.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    isBoss = false;
                    showMyproject = false;
                    new Thread(renderProject(search,0,conn)).start();
                }

            });


            showAllProject.setOnMouseClicked(event -> {
                new Thread(renderProject(search,0,conn)).start();
            });
            showEndProject.setOnMouseClicked(event -> {
                new Thread(renderProject(search,0,conn)).start();
            });
            showEndOnlyProject.setOnMouseClicked(event -> {
                new Thread(renderProject(search,0,conn)).start();
            });



            pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderProject(search,newValue.intValue(),conn)).start();
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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
            desc.setText(project.getOpis());
            title.setText(project.getNazwa());
            title.setUserData(project.getProjekt_id());

            if(project.getData_zakonczenia()==null) {
                end.setVisible(false);
                endL.setVisible(false);
            } else {


                end.setText(project.getData_zakonczenia().toString());
            }
            //status.setText("Dane na podstawie zadań");
            title.setOnAction(event -> {
                long id = (long) ((Hyperlink)event.getSource()).getUserData();
                ApplicationContext.getInstance().register("projectId",id);
                // TODO: 17.04.16 Obsłużyć
                try {
                    flowActionHandler.navigate(ShowProject.class);
                } catch (VetoException e) {
                    e.printStackTrace();
                } catch (FlowException e) {
                    e.printStackTrace();
                }
            });


            Task<Map<TaskDb.Status,Long>> countTask = new Task<Map<TaskDb.Status, Long>>() {
                @Override
                protected Map<TaskDb.Status, Long> call() throws Exception {
                    return Project.getStatistic(project.getProjekt_id());
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


    private Task<ObservableList<Project>> renderProject(String search, int page, Connection connection) {

        User u = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        contentFlow.getChildren().clear();
        int perPage = PER_PAGE;
        //int page = 1;

        String sql = "select * from ProjektyUzytkownicy pu,Projekty p,Uzytkownicy u,(select imie as l_imie,nazwisko as l_nazwisko,uzytkownik_id l_id from Uzytkownicy)au where pu.uzytkownik_id=u.uzytkownik_id \n" +
                "and pu.projekt_id=p.projekt_id and au.l_id=p.lider";

        if(!showAllProject.isSelected())
            sql+=" and pu.uzytkownik_id="+u.getUzytkownikId();

        if(showEndOnlyProject.isSelected())
            sql+=" and p.status=0";
        else if(!showEndProject.isSelected())
            sql+=" and p.status=1";




        if(search!=null && search.length()>0) sql+=" and (nazwa like '%"+search+"%' or opis like '%"+search+"%')";





        if(isBoss)
            sql+=" and p.lider in (select uzytkownik_id from Uzytkownicy WHERE typ in (2,3))";

        if(showMyproject)
            sql+=" and p.lider="+u.getUzytkownikId();


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
                do {
                    Project i = dataReader.get();
                    observableList.add(i);
                    Thread.sleep(500);
                } while (dataReader.next());
                return observableList;
            }
        };
        task.setOnSucceeded(event -> {
            // TODO: 20.04.16 To FIX
            try {
                contentFlow.getChildren().clear();
                task.getValue().forEach(this::createLis);
            } catch (NullPointerException e) {
                loger.debug("Bark danych", e);
            }

        });
        return task;
    }


}
