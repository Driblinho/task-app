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
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @PostConstruct
    public void init() throws VetoException, FlowException {
        loger = LoggerFactory.getLogger(getClass());
        new StockButtons(operationButtons,flowActionHandler).homeAction();
        InitializeConnection initializeConnection = new InitializeConnection();
        try {
            new Thread(renderProject(null,initializeConnection.connect())).start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void createLis(Project project) {
        Parent parent = null;
        try {
            parent = FXMLLoader.load(getClass().getClassLoader().getResource("view/smallProjectTpl.fxml"));
            Hyperlink title = (Hyperlink) parent.lookup(".smallProjectTitle");
            Hyperlink author = (Hyperlink) parent.lookup(".smallProjectAuthor");
            Text end = (Text) parent.lookup(".smallProjectDateEnd");
            Label endL = (Label) parent.lookup(".smallProjectDateEndLabel");
            Text status = (Text) parent.lookup(".smallProjectStatus");
            Text desc = (Text) parent.lookup(".smallProjectDesc");


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
            status.setText("Dane na podstawie zadań");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        contentFlow.getChildren().add(parent);
    }


    private Task<ObservableList<Project>> renderProject(String search, Connection connection) {

        User u = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");

        String sql = "select * from ProjektyUzytkownicy pu,Projekty p,Uzytkownicy u,(select imie as l_imie,nazwisko as l_nazwisko,uzytkownik_id l_id from Uzytkownicy)au where pu.uzytkownik_id=u.uzytkownik_id \n" +
                "and pu.projekt_id=p.projekt_id and au.l_id=p.lider and pu.uzytkownik_id="+u.getUzytkownikId()+" order by pu.lider limit 6;\n";

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
                task.getValue().forEach(this::createLis);
            } catch (NullPointerException e) {
                loger.debug("Bark danych", e);
            }

        });
        return task;
    }



}
