package pl.tarsius.controller.project;

import com.sun.istack.internal.Nullable;
import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import io.datafx.io.converter.JdbcConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.ListSelectionView;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Invite;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ireneusz Kuliga on 18.04.16.
 */
@FXMLController(value = "/view/app/addUserToProject.fxml", title = "TaskApp - Dodaj uczestników do projektu")
public class AddToProjectController extends BaseController {
    @FXML
    @BackAction
    private Button cancelUser;

    @FXML
    @ActionTrigger("addToProject")
    private Button saveUser;

    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML private ListSelectionView selectUser;

    @PostConstruct
    public void init() {
        new StockButtons(operationButtons,flowActionHandler).inProjectButton();
        InitializeConnection initializeConnection = new InitializeConnection();
        saveUser.setDisable(true);


        try {
            Connection connection = initializeConnection.connect();
            final Task<ObservableList<User>>[] task = new Task[]{userListTask(null, connection)};
            new Thread(task[0]).start();
            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    System.out.println("ENTER");
                    int size = selectUser.getSourceItems().size();
                    if(size>0)
                        selectUser.getSourceItems().remove(0, size-1);
                    task[0].cancel();
                    try {
                        task[0] = userListTask(userBarSearch.getText().trim(),initializeConnection.connect());
                        new Thread(task[0]).start();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void updateList(User user) {
        selectUser.getSourceItems().add(user);

    }


    private Task userListTask(@Nullable String search, Connection connection) {

        Project projectM = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
        String sql = "select u.uzytkownik_id,u.imie,u.nazwisko from Uzytkownicy u \n" +
                "where uzytkownik_id not in (select uzytkownik_id from ProjektyUzytkownicy where projekt_id="+projectM.getProjekt_id()+");";

        if(search!=null) sql+= " and u.imie like '%"+search+"%'";



        JdbcConverter<User> jdbcConverter = User.jdbcConverter();
        DataReader<User> dataReader = new JdbcSource<>(connection, sql, jdbcConverter);
        Task<ObservableList<User>> t = new Task<ObservableList<User>>() {
            @Override
            protected ObservableList<User> call() throws Exception {
                ObservableList<User> observableList = FXCollections.observableArrayList();
                do {
                    User u = dataReader.get();
                    observableList.add(u);
                    Thread.sleep(500);
                } while (dataReader.next());
                return observableList;
            }
        };

        // TODO: 18.04.16 Implemen fail
        t.setOnSucceeded(event -> {
            if(t.getValue().get(0).getImie()!=null)
                t.getValue().forEach(user -> updateList(user));
            saveUser.setDisable(false);
        });
        return t;

    }

    @ActionMethod("addToProject")
    public void addToProject() throws VetoException, FlowException {
        ObservableList<User> toAdd = selectUser.getTargetItems();
        List<Invite> i = new ArrayList<>();
        long pId = (long) ApplicationContext.getInstance().getRegisteredObject("projectId");
        toAdd.forEach(user -> i.add(new Invite(pId, user.getUzytkownikId(), 1, user.getImieNazwisko())));
        Object[] b = Invite.saveList(i);
        Alert.AlertType a = ((boolean)b[0])?Alert.AlertType.INFORMATION: Alert.AlertType.ERROR;
        new Alert(a,""+b[1]).show();
        if((boolean)b[0]) flowActionHandler.navigate(ShowProject.class);
    }

}
