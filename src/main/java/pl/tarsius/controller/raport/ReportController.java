package pl.tarsius.controller.raport;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
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
import pl.tarsius.database.Model.ReportItem;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.pdf.GenReportService;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Kontroler odpowiadający za generowanie raportów
 * Created by ireq on 05.05.16.
 */
@FXMLController(value = "/view/app/generatingReports.fxml", title = "Generowanie Raportów - Tarsius")
public class ReportController extends BaseController {
    /**
     * {@link Logger}
     */
    private static Logger loger = LoggerFactory.getLogger(ReportController.class);
    /**
     * {@link VBox} na listę raportów
     */
    @FXML private VBox reportList;
    @FXML private RadioButton rTaskRadio;
    @FXML private RadioButton rProjectRadio;
    @FXML private Pagination pagination;

    /**
     * Button obsługujący akcje generowania raportów z wszystkich projektów
     */
    @FXML @ActionTrigger("genFullReport") private Button genAllReport;
    /**
     * Button obsługujący akcje generowania raportów z wybranych raportów
     */
    @FXML @ActionTrigger("genSelectedReport") private Button genSelectReport;
    /**
     * Button obsługujący akcje generowania raportów z tylko moich zadań projektów
     */
    @FXML @ActionTrigger("genAllMyReport") private Button genAllMyReport;
    /**
     * Generowanie raportów z moich zadań
     */
    @FXML @ActionTrigger("genTaskReport") private Button genMyTaskReport;
    /**
     *  Czyszczenie listy raportów
     */
    @FXML @ActionTrigger("clearList") private Button clearReportList;
    /**
     * Ilość raportów na stronie
     */
    private int PER_PAGE = 8;
    /**
     * Koszyk raportów
     */
    private HashSet<Long> projectBucket;
    /**
     * {@link Service} generujący raporty
     */
    private Service<Void> service;

    /**
     * Inicjalizacja kontrolera
     */
    @PostConstruct
    public void  init() {
        new StockButtons(operationButtons, flowActionHandler).homeAction();
        breadCrumb.setSelectedCrumb(bucketReport);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());
        projectBucket = (HashSet<Long>) ApplicationContext.getInstance().getRegisteredObject("reportBucket");
        Platform.runLater(() -> {
            clearReportList.setVisible(false);
            genSelectReport.setVisible(false);
            pagination.setVisible(false);
        });

        try {

            Connection connection = new InitializeConnection().connect();
            if(projectBucket.size()>0) {
                Platform.runLater(() ->{
                    pagination.setVisible(true);
                    clearReportList.setVisible(true);
                    genSelectReport.setVisible(true);
                });
                new Thread(renderReports(connection,0)).start();
                pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                    new Thread(renderReports(connection, newValue.intValue())).start();
                });
            }
        } catch (SQLException e) {
            loger.debug("ReportController sqlErr", e);
            new Alert(Alert.AlertType.ERROR,"Problem z bazą danych").show();
        }


    }

    /**
     *
     * @param row
     * @return
     */
    private GridPane reportRow(ReportItem row) {
        GridPane node = null;
        try {
            node = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/reportRowTPL.fxml"));
            Hyperlink title = (Hyperlink) node.lookup(".reportName");
            Hyperlink projectAutor = (Hyperlink) node.lookup(".reportProjectUser");
            Hyperlink tasksCount = (Hyperlink) node.lookup(".reportNumberTask");
            Hyperlink usersCount = (Hyperlink) node.lookup(".reportNumberUser");
            Button remove = (Button) node.lookup(".delOnReportList");

            remove.setOnAction(event -> {
                projectBucket.remove(row.getProjectId());
                ApplicationContext.getInstance().register("reportBucket", projectBucket);
                DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ReportController.class);
            });

            projectAutor.setOnAction(event -> navigateToProfile(row.getAuthorId()));

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

    private Task<ObservableList<ReportItem>> renderReports(Connection connection, int page) {
        String sql = "select p.projekt_id,p.nazwa,p.lider,p.opis,p.data_dodania,p.data_zakonczenia,u.imie,u.nazwisko,count(*) as u_count,(select count(*) from Zadania z where p.projekt_id=z.projekt_id) as t_count from Projekty p,ProjektyUzytkownicy pu,Uzytkownicy u where p.projekt_id=pu.projekt_id and u.uzytkownik_id=pu.uzytkownik_id";
        sql+=" and p.projekt_id in ("+projectBucket.toString().replace("[","").replace("]","")+") ";
        sql+=" group by p.projekt_id";
        sql+=" limit "+page* PER_PAGE +","+ PER_PAGE +"";

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
                Platform.runLater(() -> pagination.setPageCount((int) Math.ceil(count/ PER_PAGE)));
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

    @ActionMethod("genFullReport")
    public void genFullReport() {
        cancelService();
        service = new GenReportService();
        if(!service.isRunning()) {
            service.reset();
            service.start();
        }
    }

    @ActionMethod("genSelectedReport")
    public void genSelectedReport() {
            if(projectBucket.size()>0) {
                cancelService();
                service = new GenReportService(projectBucket);
                if(!service.isRunning()) {
                    service.reset();
                    service.start();
                }
            }
    }

    @ActionMethod("genAllMyReport")
    public void genAllMyReport() {
        cancelService();
        service = new GenReportService(user.getUzytkownikId());
        if(!service.isRunning()) {
            service.reset();
            service.start();
        }
    }

    @ActionMethod("genTaskReport")
    public void genTaskReport() {
        cancelService();
        service = new GenReportService(user.getUzytkownikId());
        ((GenReportService)service).setTaskReport(true);
        if(!service.isRunning()) {
            service.reset();
            service.start();
        }
    }

    @ActionMethod("clearList")
    public void clearList() throws VetoException, FlowException {
        projectBucket.clear();
        ApplicationContext.getInstance().register("reportBucket", projectBucket);
        flowActionHandler.navigate(ReportController.class);
    }

    private void cancelService() {
        if(service!=null && service.isRunning()) service.cancel();
    }

}
