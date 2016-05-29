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
import org.controlsfx.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.BlockDatePicker;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;
import pl.tarsius.util.validator.form.TaskFormValidator;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ireq on 01.05.16.
 */
@FXMLController(value = "/view/app/newtask.fxml", title = "TaskApp")
public class EditTaskController extends BaseController {
    @FXML private TextField taskName;
    @FXML private TextField taskDesc;
    @FXML private ListSelectionView<User> taskUser;

    @FXML private DatePicker taskEndDatePicker;

    @FXML
    @ActionTrigger("cleanDate")
    private Button taskEndDatePickerClean;

    @FXML
    @ActionTrigger("saveTask")
    private Button taskSave;

    @FXML
    @LinkAction(ShowProject.class)
    private Button taskCancel;

    @FXML private FlowPane taskUserList;
    private ToggleGroup toggleGroup;

    @FXML
    @ActionTrigger("clearSelectedUser")
    private Button taskClearUser;

    @FXML private Pagination taskUserListpPag;

    @FXML private CheckBox taskRemoveUser;
    @FXML private Text taskCurentUser;
    @FXML private Group taskCurUserGroup;

    private ValidationSupport validationSupport;

    private static Logger loger = LoggerFactory.getLogger(EditTaskController.class);
    private TaskDb taskDbModel;

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

    public AnchorPane userItem(User user) {
        try {
            AnchorPane root = FXMLLoader.load(getClass().getClassLoader().getResource("view/app/userListItemTPL.fxml"));
            Circle avatar = (Circle) root.lookup(".userAv");
            Label username = (Label) root.lookup(".userName");
            RadioButton radio = (RadioButton) root.lookup(".userRadio");
            radio.setUserData(user);
            radio.setToggleGroup(toggleGroup);
            avatar.setFill(new ImagePattern(new Image(user.getAvatarUrl())));
            username.setText(user.getImieNazwisko());
            return root;
        } catch (IOException e) {
            loger.error("userItem",e);
            return null;
        }
    }


    private Task<ObservableList<User>> renderUsers(long projectId, int page){

        try {
            Connection connection = new InitializeConnection().connect();
            String sql = "select {tpl} from ProjektyUzytkownicy pu,Uzytkownicy u where projekt_id="+projectId+" and u.uzytkownik_id=pu.uzytkownik_id ";
            if(search!=null && search.length()>0) sql+=" and (u.imie like '%"+search+"%' or u.nazwisko like '%"+search+"%')";
            String sqlCount = sql.replace("{tpl}", "count(*)");
            loger.debug("SQL (count):"+sqlCount);
            sql = sql.replace("{tpl}", "u.*");

            loger.debug("SQL :"+sql);
            int perPage = 1;
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
                    if(toggleGroup.getSelectedToggle()!=null) {
                        User user = (User) toggleGroup.getSelectedToggle().getUserData();
                        // TODO: 03.05.16 FIX SETTER
                        taskDb.setStatus(TaskDb.Status.INPROGRES.getValue());
                        return TaskDb.updateTask(taskDb,user.getUzytkownikId(),taskDbModel.getId());
                    }

                    if(taskRemoveUser.isSelected()) taskDb.setStatus(TaskDb.Status.NEW.getValue());
                    return TaskDb.updateTask(taskDb,null,taskDbModel.getId());
                }
            };
            task.setOnRunning(event -> {
                loading.setVisible(true);
            });
            task.setOnSucceeded(event -> {
                loading.setVisible(false);
                if((boolean)task.getValue()[0]) {
                    new Alert(Alert.AlertType.INFORMATION,(String)task.getValue()[1]).show();
                    try {
                        flowActionHandler.navigate(ShowTaskController.class);
                    } catch (VetoException e) {
                        e.printStackTrace();
                    } catch (FlowException e) {
                        e.printStackTrace();
                    }
                } else new Alert(Alert.AlertType.ERROR,(String)task.getValue()[1]).show();
            });
            new Thread(task).start();
        }
    }


    @ActionMethod("cleanDate")
    public void cleanDate(){ taskEndDatePicker.setValue(null);
    }

    @ActionMethod("clearSelectedUser")
    public void clearSelectedUser() {
        if(toggleGroup.getSelectedToggle()!=null)
            toggleGroup.getSelectedToggle().setSelected(false);
    }
}
