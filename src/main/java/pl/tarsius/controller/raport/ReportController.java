package pl.tarsius.controller.raport;

import io.datafx.controller.FXMLController;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.ReportItem;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by ireq on 05.05.16.
 */
@FXMLController(value = "/view/app/generatingReports.fxml", title = "Generowanie Raportów - TaskApp")
public class ReportController extends BaseController {
    private static Logger loger = LoggerFactory.getLogger(ReportController.class);
    @FXML private VBox reportList;
    @FXML private RadioButton rTaskRadio;
    @FXML private RadioButton rProjectRadio;
    @FXML private Pagination pagination;
    private int perPage;
    ToggleGroup group;
    HashSet<Long> projectBucket;
    @PostConstruct
    public void  init() {
        new StockButtons(operationButtons, flowActionHandler).homeAction();
        group = new ToggleGroup();
        rProjectRadio.setToggleGroup(group);
        rTaskRadio.setToggleGroup(group);

        perPage = 2;

        projectBucket = new HashSet<>();
        projectBucket.add(15l);
        projectBucket.add(18l);
        projectBucket.add(26l);
        projectBucket.add(25l);
        projectBucket.add(24l);
        pagination.setPageCount((int) Math.ceil(((double) projectBucket.size())/perPage));




        System.out.println(projectBucket.toString().replace("[","").replace("]",""));


        try {
            Connection connection = new InitializeConnection().connect();
            new Thread(renderRaports(connection,0)).start();

            pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(renderRaports(connection,newValue.intValue())).start();
            });

        } catch (SQLException e) {
            loger.debug("ReportController sqlErr", e);
            new Alert(Alert.AlertType.ERROR,"Problem z bazą danych").show();
        }


    }

    private GridPane reportRow(ReportItem row) {
        GridPane node = null;
        try {
            node = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/reportRowTPL.fxml"));
            Hyperlink title = (Hyperlink) node.lookup(".reportName");
            Hyperlink projectAutor = (Hyperlink) node.lookup(".reportProjectUser");
            Hyperlink tasksCount = (Hyperlink) node.lookup(".reportNumberTask");
            Hyperlink usersCount = (Hyperlink) node.lookup(".reportNumberUser");

            title.setText(row.getName());
            projectAutor.setText(row.getAuthor());
            tasksCount.setText(""+row.getTasks());
            usersCount.setText(""+row.getUsers());

        } catch (IOException e) {
           loger.debug("Problem z ładowaniem szablonu", e);
        } finally {
            return node;
        }
    }

    private Task<ObservableList<ReportItem>> renderRaports(Connection connection, int page) {
        String sql = "select p.projekt_id,p.nazwa,p.opis,p.data_dodania,p.data_zakonczenia,u.imie,u.nazwisko,count(*) as u_count,(select count(*) from Zadania z where p.projekt_id=z.projekt_id) as t_count from Projekty p,ProjektyUzytkownicy pu,Uzytkownicy u where p.projekt_id=pu.projekt_id and u.uzytkownik_id=pu.uzytkownik_id";
        sql+=" and p.projekt_id in ("+projectBucket.toString().replace("[","").replace("]","")+") ";
        sql+=" group by p.projekt_id";
        sql+=" limit "+page*perPage+","+perPage+"";

        String countSql = "select count(*) from Projekty where projekt_id in ("+projectBucket.toString().replace("[","").replace("]","")+")";
        loger.debug("SQL: "+sql);
        DataReader<ReportItem> dataReader = new JdbcSource<>(connection, sql, ReportItem.jdbcConverter());
        Task<ObservableList<ReportItem>> task = new Task<ObservableList<ReportItem>>() {
            @Override
            protected ObservableList<ReportItem> call() throws Exception {
                ObservableList<ReportItem> observableList = FXCollections.observableArrayList();
                PreparedStatement ps = connection.prepareStatement(countSql);
                ResultSet rs = ps.executeQuery();
                rs.next();
                double count = rs.getLong(1);
                Platform.runLater(() -> pagination.setPageCount((int) Math.ceil(count/perPage)));
                dataReader.forEach(reportItem -> observableList.add(reportItem));
                return observableList;
            }
        };
        task.setOnSucceeded(event -> {
            Platform.runLater(() -> reportList.getChildren().clear());
            task.getValue().forEach(reportItem -> Platform.runLater(() -> reportList.getChildren().add(reportRow(reportItem))));
        });
        return task;
    }

}
