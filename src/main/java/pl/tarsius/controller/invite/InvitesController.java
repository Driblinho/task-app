package pl.tarsius.controller.invite;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.WeakEventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Invite;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by Ireneusz Kuliga on 19.04.16.
 */
@FXMLController(value = "/view/app/myInv.fxml", title = "TaskApp - Moje Zaproszenia")
public class InvitesController extends BaseController {

    @FXML
    private ChoiceBox invFiltrSort;

    @FXML
    private VBox invContent;

    @ActionHandler
    private FlowActionHandler flowActionHandler;
    private Logger loger;

    //private ObservableList<Invite> inviteObservableList;

    @PostConstruct
    public void init() {
        loger = LoggerFactory.getLogger(getClass());
        User user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        new StockButtons(operationButtons, flowActionHandler).homeAction();

        invFiltrSort.setItems(FXCollections.observableArrayList("Sortowanie", "Najnowsze", "Najstarsze"));
        invFiltrSort.getSelectionModel().selectFirst();





        InitializeConnection initializeConnection = new InitializeConnection();
        try {
            Task<ObservableList<Invite>> task = inviteTask(null,initializeConnection.connect(),user.getUzytkownikId());
            new Thread(task).start();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private GridPane invRow(Invite invite) {
        GridPane gridPane = new GridPane();
        try {
            gridPane = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/invitationUserTPL.fxml"));
            gridPane.setPrefWidth(invContent.getScene().widthProperty().getValue()-230.0);
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
                Node n = (Node) b.getParent();
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
                        try {
                            flowActionHandler.navigate(InvitesController.class);
                        } catch (VetoException e) {
                            loger.debug("Cancdel Inv Task", e);
                        } catch (FlowException e) {
                            loger.debug("Cancdel Inv Task", e);
                        }
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
                        try {
                            if((boolean)task.getValue()[0]) {
                                flowActionHandler.navigate(ShowProject.class);
                            }
                            loading.setVisible(false);
                            new Alert(Alert.AlertType.INFORMATION,(String) task.getValue()[1]).show();
                        } catch (VetoException e) {
                            e.printStackTrace();
                        } catch (FlowException e) {
                            e.printStackTrace();
                        }
                    });

                    new Thread(task).start();



            });

            projectName.setText(invite.getNazwaProjektu());
            author.setText(invite.getUzytkownikImieNazwisko());
            endD.setText(invite.getDataZakonczenia().toString());
            dateSend.setText(invite.getDataDodania().toString());


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return gridPane;
        }

    }

    public Task<ObservableList<Invite>> inviteTask(String search, Connection connection, long userId) {

        String sql = "select z.uzytkownik_id,z.zaproszenie_id,p.projekt_id,p.nazwa,p.data_zakonczenia,lider,u.imie,u.nazwisko,z.data_dodania " +
                "from Zaproszenia z,Projekty p,Uzytkownicy u " +
                "where p.projekt_id=z.projekt_id " +
                "and p.lider=u.uzytkownik_id " +
                "and z.uzytkownik_id="+userId+" and stan=1;";

        //if(search!=null) sql+= " and u.imie like '%"+search+"%'";

        DataReader<Invite> dataReader = new JdbcSource<>(connection, sql, Invite.jdbcConverter());

        Task<ObservableList<Invite>> task = new Task<ObservableList<Invite>>() {
            @Override
            protected ObservableList<Invite> call() throws InterruptedException, IOException {
                ObservableList<Invite> observableList = FXCollections.observableArrayList();

                do {
                    Invite i = dataReader.get();
                    observableList.add(i);
                    Thread.sleep(500);
                } while (dataReader.next());
                return observableList;
            }
        };
        task.setOnSucceeded(event -> {
            if(task.getValue().get(0)!=null)
                task.getValue().forEach(invite -> invContent.getChildren().add(invRow(invite)));
        });
        return task;
    }





}
