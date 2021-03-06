package pl.tarsius.controller.invite;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.project.ShowProjectController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Invite;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Kontroler odpowiadający z a wyświetlanie i zarządzanie użytkownikami
 * Created by Ireneusz Kuliga on 19.04.16.
 */
@FXMLController(value = "/view/app/myInv.fxml", title = "Moje Zaproszenia - Tarsius" )
public class InvitesController extends BaseController {


    /**
     * {@link SegmentedButton} na filtrowanie zaproszeń
     */
    @FXML private SegmentedButton invFiltr;
    /**
     * {@link SegmentedButton} na sortowanie zaproszeń
     */
    @FXML private SegmentedButton invSort;

    /**
     * Pole mapujące element FXML odpowiadający za wyświetlanie listy zaproszeń
     */
    @FXML private VBox invContent;

    /**
     * Pole mapujące element FXML odpowiadający za stronicowanie zaproszeń
     */
    @FXML private Pagination invPagination;

    /**
     * DataFX {@link FlowActionHandler} obsługuje nawigacje między kontrolerami
     */
    @ActionHandler
    private FlowActionHandler flowActionHandler;


    /**
     * {@link Logger}
     */
    private static Logger loger = LoggerFactory.getLogger(InvitesController.class);

    /**
     * Pole pomagające określić czy filtrować po zaproszeniach od kierownika
     */
    private boolean isBoss;

    private static final int PER_PAGE = 10;


    /**
     * Metoda inicjalizująca kontroler Zaproszeń
     */
    @PostConstruct
    public void init() {

        breadCrumb.setSelectedCrumb(myInv);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());
        Platform.runLater(() -> userBarSearch.setDisable(true));

        invPagination.setPageCount(1);
        User user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        new StockButtons(operationButtons, flowActionHandler).homeAction();

        ToggleButton boss = new ToggleButton("Kierownicze");
        ToggleButton all = new ToggleButton("Wszystkie");
        invFiltr.getButtons().addAll(boss,all);

        ToggleButton asc = new ToggleButton("Rosnąco");
        asc.setSelected(true);
        asc.setUserData("ASC");
        ToggleButton desc = new ToggleButton("Malejąco");
        desc.setUserData("DESC");
        invSort.getButtons().addAll(asc,desc);


        InitializeConnection initializeConnection = new InitializeConnection();
        try {
            Connection conn = initializeConnection.connect();
            Task<ObservableList<Invite>> task = inviteTask(conn,user.getUzytkownikId(),0);
            new Thread(task).start();

            invPagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                new Thread(inviteTask(conn, user.getUzytkownikId(), newValue.intValue())).start();
            });


            asc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort=(String) asc.getUserData();
                    new Thread(inviteTask(conn, user.getUzytkownikId(), 0)).start();
                } else desc.setSelected(true);
            });

            desc.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    sort = (String) desc.getUserData();
                    new Thread(inviteTask(conn, user.getUzytkownikId(), 0)).start();
                } else asc.setSelected(true);
            });

            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    search = userBarSearch.getText().trim();
                    new Thread(inviteTask(conn, user.getUzytkownikId(), 0)).start();
                }
            });

            boss.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    isBoss = true;
                    new Thread(inviteTask(conn, user.getUzytkownikId(), 0)).start();
                } else
                    isBoss = false;

            });

            all.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue) {
                    isBoss=false;
                    new Thread(inviteTask(conn, user.getUzytkownikId(), 0)).start();
                } else {
                    isBoss = true;
                }


            });

        } catch (SQLException e) {
            e.printStackTrace();
        }




    }


    /** Meada zwraca <code>GridPane</code> z zaproszeniem użytkownika
     * @param invite Obiekt reprezentujący zaproszenie
     * @return GridPane
     */
    private GridPane invRow(Invite invite) {
        GridPane gridPane = new GridPane();
        try {
            gridPane = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/invitationUserTPL.fxml"));
            Label projectName = (Label) gridPane.lookup(".invProjectName");
            Hyperlink author = (Hyperlink) gridPane.lookup(".invAuthor");
            Label endDL = (Label) gridPane.lookup(".invDescDateEnd");
            Label endD = (Label) gridPane.lookup(".invDateEnd");
            Button cancel = (Button) gridPane.lookup(".invCancel");

            Button acept = (Button) gridPane.lookup(".invAccept");
            Label dateSendL = (Label) gridPane.lookup(".invDescDateSendLabel");
            Label dateSend = (Label) gridPane.lookup(".invDateSend");


            cancel.setOnAction(event -> {
                Button b = (Button) event.getSource();
                Task<Object[]> task = new Task<Object[]>() {
                    @Override
                    protected Object[] call() throws Exception {
                        return Invite.remove(invite.getZaproszeniId());
                    }
                };
                task.setOnRunning(event1 -> loading.setVisible(true));
                task.setOnSucceeded(event1 -> {
                    loading.setVisible(false);
                    if((boolean)task.getValue()[0]) {
                        DataFxEXceptionHandler.navigateQuietly(flowActionHandler, InvitesController.class);
                    } else {
                        new Alert(Alert.AlertType.INFORMATION,""+task.getValue()[1]).show();
                    }
                });


                new Thread(task).start();
            });

            acept.setOnAction(event -> {
                loading.setVisible(true);
                    ApplicationContext.getInstance().register("projectId", invite.getProjektId());
                    Task<Object[]> task = new Task<Object[]>() {
                        @Override
                        protected Object[] call() throws Exception {
                            Object[] resp = Project.addUserToProject(invite.getUzytkownikId(),invite.getProjektId());
                            if((boolean)resp[0]) {
                                Object[] ir = Invite.remove(invite.getZaproszeniId());
                                if (!(boolean)ir[0]) resp = ir;
                            }
                            return resp;
                        }
                    };
                    task.setOnSucceeded(event1 -> {
                        loading.setVisible(false);
                        if((boolean)task.getValue()[0]) {
                            DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowProjectController.class);
                        }
                        new Alert(Alert.AlertType.INFORMATION,(String) task.getValue()[1]).show();
                    });
                    new Thread(task).start();
            });

            projectName.setText(invite.getNazwaProjektu());
            author.setText(invite.getUzytkownikImieNazwisko());
            endD.setText(invite.getDataZakonczenia().toString());
            dateSend.setText(invite.getDataDodania().toString());


        } catch (IOException e) {
            loger.debug("Szablon zaproszeń", e);
        } finally {
            return gridPane;
        }

    }

    /**
     * Metoda generująca task który wyświetla zaproszenia użytkownika
     * @param connection Połączenie bazy danych
     * @param userId Identyfikator użytkownika dla którego mają zastać wyświetlone zaproszenia
     * @param page Index strony dla stronicowania
     * @return Task wyświetlający zaproszenia
     */
    public Task<ObservableList<Invite>> inviteTask(Connection connection, long userId,int page) {

        String sql = "select {tpl} " +
                "from Zaproszenia z,Projekty p,Uzytkownicy u " +
                "where p.projekt_id=z.projekt_id " +
                "and p.lider=u.uzytkownik_id " +
                "and z.uzytkownik_id="+userId+" and stan=1";
        if(search!=null && search.length()>0) sql+=" and p.nazwa like '%"+search+"%'";
        if(isBoss)
            sql+=" and p.lider in (select uzytkownik_id from Uzytkownicy WHERE typ in (2,3))";
        String sqlCount = sql.replace("{tpl}", "count(*)");
        loger.debug("SQL count:"+sqlCount);
        sql=sql.replace("{tpl}", "z.uzytkownik_id,z.zaproszenie_id,p.projekt_id,p.nazwa,p.data_zakonczenia,lider,u.imie,u.nazwisko,z.data_dodania");
        if (sort.length()>0) sql+= " order by z.data_dodania "+sort;
        //if(search!=null) sql+= " and u.imie like '%"+search+"%'";
        int perPage = PER_PAGE;
        sql+= " limit "+page*perPage+","+perPage+"";

        DataReader<Invite> dataReader = new JdbcSource<>(connection, sql, Invite.jdbcConverter());

        Task<ObservableList<Invite>> task = new Task<ObservableList<Invite>>() {
            @Override
            protected ObservableList<Invite> call() throws InterruptedException, IOException {
                ObservableList<Invite> observableList = FXCollections.observableArrayList();

                try {
                    ResultSet rs = connection.prepareStatement(sqlCount).executeQuery();
                    long count;
                    if(rs.next()) count = rs.getLong(1);
                    else return observableList;
                    int pageCount = (int) Math.ceil((float)count/perPage);
                    Platform.runLater(() -> {
                        if(pageCount>0) {
                            invPagination.setVisible(true);
                            invPagination.setPageCount(pageCount);
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                dataReader.forEach(invite -> observableList.add(invite));
                return observableList;
            }
        };
        task.setOnSucceeded(event -> {
            invContent.getChildren().clear();
            if(task.getValue().size()>0)
                task.getValue().forEach(invite -> invContent.getChildren().add(invRow(invite)));
        });
        return task;
    }





}
