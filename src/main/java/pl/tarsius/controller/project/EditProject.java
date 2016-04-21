package pl.tarsius.controller.project;

import com.sun.istack.internal.Nullable;
import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Created by Ireneusz Kuliga on 20.04.16.
 */
@FXMLController(value = "/view/app/editproject.fxml", title = "TaskApp")
public class EditProject extends BaseController {


    @FXML
    private TextField editProjectTitleField;
    @FXML
    private TextField editProjectDescField;
    @FXML
    private ListSelectionView<User> editProjectSelectUser;
    @FXML
    private DatePicker editProjectDatePicker;

    @FXML
    @ActionTrigger("cleanDate")
    private Button editProjectDatePickerClean;

    @FXML
    @ActionTrigger("updateProject")
    private Button editProjectSave;

    @FXML
    @BackAction
    private Button editProjectCancel;

    private ObservableList<User> obl;
    private ObservableList<User> obl2;
    private ValidationSupport validationSupport;

    @PostConstruct
    public void init() {
        new StockButtons(operationButtons,flowActionHandler).inProjectButton();
        Project project = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
        editProjectTitleField.setText(project.getNazwa());
        editProjectDescField.setText(project.getOpis());
        if(project.getData_zakonczenia()!=null)
        editProjectDatePicker.setValue(project.getData_zakonczenia().toLocalDateTime().toLocalDate());
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(editProjectTitleField, Validator.combine(
                Validator.createEmptyValidator("Tytuł jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        ));
        validationSupport.registerValidator(editProjectDescField,false,CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków",200));

        obl = FXCollections.observableArrayList();
        obl2 = FXCollections.observableArrayList();
        editProjectSelectUser.setSourceItems(obl);
        editProjectSelectUser.setTargetItems(obl2);
        Connection conn = null;
        try {
            conn = new InitializeConnection().connect();
            new Thread(userListTask(null,conn)).start();
            new Thread(userInProjectListTask(conn)).start();
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
                obl.addAll(t.getValue());
            editProjectSave.setDisable(false);
        });
        return t;
    }

    private Task userInProjectListTask(Connection connection) {

        Project projectM = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
        String sql = "select u.uzytkownik_id,u.imie,u.nazwisko from ProjektyUzytkownicy pu,Uzytkownicy u where pu.uzytkownik_id=u.uzytkownik_id and lider!=1 and projekt_id="+projectM.getProjekt_id()+"";


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
        t.setOnRunning(event -> editProjectSave.setDisable(true));
        t.setOnSucceeded(event -> {
            if(t.getValue().get(0).getImie()!=null)
                obl2.addAll(t.getValue());
            editProjectSave.setDisable(false);
        });
        return t;
    }

    @ActionMethod("updateProject")
    public void updateProject() {
        if(validationSupport.isInvalid()) {
            validationSupport.initInitialDecoration();
        } else {
            Timestamp dz = null;
            if(editProjectDatePicker.getValue()!=null) {
                dz = Timestamp.valueOf(editProjectDatePicker.getValue().atStartOfDay());
            }
            editProjectSelectUser.getTargetItems();
            Project p = new Project();
            Project projectM = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
            p.setProjekt_id(projectM.getProjekt_id());
            p.setNazwa(editProjectTitleField.getText());
            p.setOpis(editProjectDescField.getText());
            p.setData_zakonczenia(dz);
            Task<Object[]> task = new Task<Object[]>() {
                @Override
                protected Object[] call() throws Exception {
                    return Project.updateProject(p,obl2);
                }
            };
            task.setOnRunning(event -> loading.setVisible(true));
            task.setOnSucceeded(event -> {
                loading.setVisible(false);
                if((boolean)task.getValue()[0]) {
                    try {
                        flowActionHandler.navigate(ShowProject.class);
                    } catch (VetoException e) {
                        e.printStackTrace();
                    } catch (FlowException e) {
                        e.printStackTrace();
                    }
                } else {
                    new Alert(Alert.AlertType.ERROR,""+task.getValue()[1]);
                }
            });

            new Thread(task).start();

        }
    }

    @ActionMethod("cleanDate")
    public void cleanDate() {
        editProjectDatePicker.setValue(null);
    }

}
