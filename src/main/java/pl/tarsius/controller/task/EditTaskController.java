package pl.tarsius.controller.task;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.LinkAction;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.validation.ValidationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.project.ShowProjectController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.BlockDatePicker;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.form.TaskFormValidator;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Kontroler odpowiadający za edycje zadania
 * Created by ireq on 01.05.16.
 */
@FXMLController(value = "/view/app/newtask.fxml", title = "Edytuj zadanie - Tarsius")
public class EditTaskController extends BaseController {
    /**
     * Nazwa zadania
     */
    @FXML private TextField taskName;
    /**
     * Opis zadania
     */
    @FXML private TextField taskDesc;
    @FXML private ListSelectionView<User> taskUser;

    /**
     * Data zakończenia zadania
     */
    @FXML private DatePicker taskEndDatePicker;

    /**
     * {@link Button} usuwający date zakończenia
     */
    @FXML
    @ActionTrigger("cleanDate")
    private Button taskEndDatePickerClean;

    /**
     * {@link Button} obsługujący akcje zapisywania do bazy
     */
    @FXML
    @ActionTrigger("saveTask")
    private Button taskSave;

    /**
     * {@link Button} na akcje wstecz (anuluj)
     */
    @FXML
    @LinkAction(ShowProjectController.class)
    private Button taskCancel;

    @FXML private FlowPane taskUserList;
    /**
     * Grupa na sortowanie użytkowników
     */
    private ToggleGroup toggleGroup;

    /**
     * Odznaczanie zaznaczonych użytkowników
     */
    @FXML
    @ActionTrigger("clearSelectedUser")
    private Button taskClearUser;

    /**
     * Stronicowanie użytkowników
     */
    @FXML private Pagination taskUserListpPag;

    /**
     * Pole na {@link CheckBox} określający czy usunąć kogoś z zadania
     */
    @FXML private CheckBox taskRemoveUser;
    @FXML private Text taskCurentUser;
    /**
     * {@link Group}
     */
    @FXML private Group taskCurUserGroup;

    /**
     * Pole na validatro formularza
     */
    private ValidationSupport validationSupport;

    private static Logger loger = LoggerFactory.getLogger(EditTaskController.class);
    /**
     * Reprezentacja edytowanego zadania
     */
    private TaskDb taskDbModel;

    private Long selectedUser = 0L;

    /**
     * Metoda inicjalizująca Kontroler
     */
    @PostConstruct
    public void init() {

        breadCrumb.setSelectedCrumb(editTask);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        taskEndDatePicker.setDayCellFactory(new BlockDatePicker());

        taskDbModel = (TaskDb) ApplicationContext.getInstance().getRegisteredObject("taskModel");
        if(taskDbModel.getStatus()==TaskDb.Status.END.getValue())
            new StockButtons(operationButtons,flowActionHandler).inCloseTask();
        else
            new StockButtons(operationButtons,flowActionHandler).inTask();


        taskRemoveUser.setVisible(true);
        taskCurUserGroup.setVisible(true);
        taskCurentUser.setText(taskDbModel.getUserName());

        taskDesc.setText(taskDbModel.getDesc());
        taskName.setText(taskDbModel.getName());
        if(taskDbModel.getEndDate()!=null)
        taskEndDatePicker.setValue(taskDbModel.getEndDate().toLocalDate());

        toggleGroup = new ToggleGroup();
        long projectId = (long) ApplicationContext.getInstance().getRegisteredObject("projectId");
        new Thread(renderUsers(projectId, 0)).start();

        taskUserListpPag.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            new Thread(renderUsers(projectId, newValue.intValue())).start();
        });

        userBarSearch.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)) {
                search = userBarSearch.getText().trim();
                new Thread(renderUsers(projectId, 0)).start();
            }
        });

        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(taskName, TaskFormValidator.getName());
        validationSupport.registerValidator(taskDesc, TaskFormValidator.getDescription());

    }

    /**Metoda zwraca wypełniony szablon pojedynczego użytkownika
     * @param user Obiekt reprezentujący użytkownika
     * @return AnchorPane z wypełnionym szablonem
     */
    public AnchorPane userItem(User user) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/userListItemTPL.fxml"));
            Circle avatar = (Circle) root.lookup(".userAv");
            Label username = (Label) root.lookup(".userName");
            RadioButton radio = (RadioButton) root.lookup(".userRadio");
            radio.setUserData(user);
            if(user.getUzytkownikId()==selectedUser) radio.setSelected(true);
            radio.setOnAction(event -> selectedUser=user.getUzytkownikId());
            radio.setToggleGroup(toggleGroup);
            avatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
            username.setText(user.getImieNazwisko());
            return root;
        } catch (IOException e) {
            loger.error("userItem",e);
            return null;
        }
    }


    /**
     * Metoda wyświetlająca użytkowników możliwych do wybrania
     * @param projectId ID projektu
     * @param page Strona na jaką ma zostać ustawione stronicowanie
     * @return Task wyświetlający użytkowników w taskUserList.
     */
    private Task<ObservableList<User>> renderUsers(long projectId, int page){

        try {
            Connection connection = new InitializeConnection().connect();
            String sql = "select {tpl} from ProjektyUzytkownicy pu,Uzytkownicy u where projekt_id="+projectId+" and u.uzytkownik_id=pu.uzytkownik_id ";
            if(search!=null && search.length()>0) sql+=" and (u.imie like '%"+search+"%' or u.nazwisko like '%"+search+"%')";
            String sqlCount = sql.replace("{tpl}", "count(*)");
            loger.debug("SQL (count):"+sqlCount);
            sql = sql.replace("{tpl}", "u.*");

            loger.debug("SQL :"+sql);
            int perPage = 4;
            sql+= " limit "+page*perPage+","+perPage+"";
            DataReader<User> dr = new JdbcSource<>(connection, sql, User.jdbcConverter());

            Task<ObservableList<User>> task = new Task<ObservableList<User>>() {
                @Override
                protected ObservableList<User> call() throws Exception {
                    ObservableList<User> users = FXCollections.observableArrayList();
                    try {
                        ResultSet rs = connection.prepareStatement(sqlCount).executeQuery();
                        rs.next();
                        long count = rs.getLong(1);
                        int newPageCount = (int) Math.ceil((float)count/perPage);
                        if(newPageCount==0) Platform.runLater(() -> taskUserListpPag.setVisible(false));
                        else {
                            Platform.runLater(() -> {taskUserListpPag.setVisible(true);taskUserListpPag.setPageCount(newPageCount);});
                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    dr.forEach(user -> users.add(user));
                    return users;
                }
            };
            task.setOnSucceeded(event -> {
                taskUserList.getChildren().clear();
                if(task.getValue().size()>0)
                    task.getValue().forEach(user -> taskUserList.getChildren().add(userItem(user)));
            });
            return task;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Metoda zapisująca zadanie do i wyświetlająca komunikat.
     * Po pomyślnym dodaniu do bazy przenosi do now edytowanego zadnia
     */
    @ActionMethod("saveTask")
    public void saveTask() {
        if(validationSupport.isInvalid()) {
            validationSupport.initInitialDecoration();
        } else {

            Date end = null;
            if(taskEndDatePicker.getValue()!=null) end = Date.valueOf(taskEndDatePicker.getValue());
            long projectId = (long)ApplicationContext.getInstance().getRegisteredObject("projectId");
            TaskDb taskDb = new TaskDb(taskDbModel.getId(), taskName.getText(), taskDesc.getText(), TaskDb.Status.valueOf(taskDbModel.getStatus()), projectId, end);
            Task<Object[]> task = new Task<Object[]>() {
                @Override
                protected Object[] call() throws Exception {
                    taskDb.setStatus(taskDbModel.getStatus());
                    if(toggleGroup.getSelectedToggle()!=null && toggleGroup.getSelectedToggle().isSelected()) {
                        User user = (User) toggleGroup.getSelectedToggle().getUserData();
                        // TODO: 03.05.16 FIX SETTER
                        taskDb.setStatus(TaskDb.Status.INPROGRES.getValue());
                        return TaskDb.updateTask(taskDb,user.getUzytkownikId(),taskDbModel.getId());
                    }

                    Long uid = taskDbModel.getUserId();
                    if(taskRemoveUser.isSelected()) {
                        taskDb.setStatus(TaskDb.Status.NEW.getValue());
                        uid=null;
                    }

                    return TaskDb.updateTask(taskDb,uid,taskDbModel.getId());
                }
            };
            task.setOnRunning(event -> {
                loading.setVisible(true);
            });
            task.setOnFailed(event -> {
                task.getException().printStackTrace();
                loading.setVisible(false);
                new Alert(Alert.AlertType.ERROR, "Błąd podczas wykonywania zadania").show();
            });
            task.setOnSucceeded(event -> {
                loading.setVisible(false);
                if((boolean)task.getValue()[0]) {
                    new Alert(Alert.AlertType.INFORMATION,(String)task.getValue()[1]).show();
                    DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowTaskController.class);
                } else new Alert(Alert.AlertType.ERROR,(String)task.getValue()[1]).show();
            });
            new Thread(task).start();
        }
    }


    /**
     * Usuwa datę z {@link DatePicker}
     */
    @ActionMethod("cleanDate")
    public void cleanDate(){ taskEndDatePicker.setValue(null);
    }

    /**
     * Usuwa zaznaczonego użytkownika z listy dostawnych użytkowników
     */
    @ActionMethod("clearSelectedUser")
    public void clearSelectedUser() {
        if(toggleGroup.getSelectedToggle()!=null)
            toggleGroup.getSelectedToggle().setSelected(false);
    }
}
