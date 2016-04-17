package pl.tarsius.controller;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FlowActionHandler;
import org.datafx.controller.util.VetoException;
import org.datafx.provider.DataProvider;
import org.datafx.provider.ObjectDataProvider;
import org.datafx.reader.DataReader;
import org.datafx.reader.converter.JdbcConverter;
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.util.gui.StockButtons;
import javax.annotation.PostConstruct;
import org.datafx.reader.JdbcSource;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/app/start.fxml", title = "TaskApp - Home")
public class HomeController extends BaseController{



    @FXMLApplicationContext private ApplicationContext applicationContext;
    @FXML private Circle profileAvatar;

    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML
    private Pane dataList;

    @PostConstruct
    public void init() throws VetoException, FlowException {
        new StockButtons(operationButtons,flowActionHandler).homeAction();

        InitializeConnection ic = new InitializeConnection();

        JdbcConverter<Project> jdbcConverter = new JdbcConverter<Project>() {
            private boolean next = true;

            @Override
            public boolean next() {
                return next;
            }

            @Override
            public Project convertOneRow(ResultSet resultSet) {
                Project p = new Project();
                try {
                    p = new Project(
                            resultSet.getLong("projekt_id"),
                            resultSet.getString("nazwa"),
                            resultSet.getString("opis"),
                            resultSet.getLong("lider"),
                            resultSet.getTimestamp("data_dodania"),
                            resultSet.getTimestamp("data_zakonczenia"));
                } catch (SQLException e) {
                    next = false;
                }
                return p;
            }
        };

        DataReader<Project> dataReader = new JdbcSource<Project>(ic.getJdbcUrl() + "&user=" + ic.getUsername() + "&password=" + ic.getPassword(), jdbcConverter, "Projekty", "*");


        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        do {
                            DataProvider<Project> dataProvider=new ObjectDataProvider<Project>(dataReader);
                            Worker<Project> w = dataProvider.retrieve();
                            Platform.runLater(() -> w.stateProperty().addListener((observable, oldValue, newValue) -> {
                                if(newValue.equals(State.SUCCEEDED)) {
                                    createLis(w.getValue());
                                }
                            }));
                            Thread.sleep(500);
                        } while (dataReader.next());
                        return null;
                    }
                };
            }
        };

        service.start();


        service.stateProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("STAN: "+newValue.toString());

        });

        //new Thread(projectWorker).start();

    }


    private void createLis(Project project) {
        Hyperlink a = new Hyperlink(project.getNazwa());
        a.setUserData(project.getProjekt_id());
        a.setOnAction(event -> {
            Hyperlink hl = (Hyperlink) event.getSource();
            applicationContext.register("projectId", hl.getUserData());
            try {
                flowActionHandler.navigate(ShowProject.class);
            } catch (VetoException e) {
                e.printStackTrace();
            } catch (FlowException e) {
                e.printStackTrace();
            }
        });
        dataList.getChildren().add(a);
    }




}
