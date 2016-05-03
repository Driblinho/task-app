package pl.tarsius.controller.task;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.LinkAction;
import io.datafx.controller.util.VetoException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.Model.TaskDb;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;

/**
 * Created by ireq on 03.05.16.
 */
@FXMLController(value = "/view/app/taskStatus.fxml", title = "TaskApp")
public class StatusController extends BaseController{
    private TaskDb taskDb;


    @FXML @ActionTrigger("saveStatus") private Button taskSave;


    @FXML
    @LinkAction(ShowTaskController.class)
    private Button taskCancel;

    @FXML private TextField taskComment;
    private ValidationSupport validationSupport;
    private boolean statusTyp;
    @PostConstruct
    public void init() {
        taskDb = (TaskDb) ApplicationContext.getInstance().getRegisteredObject("taskModel");
        if(taskDb.getStatus()==TaskDb.Status.END.getValue())
            new StockButtons(operationButtons,flowActionHandler).inCloseTask();
        else
            new StockButtons(operationButtons,flowActionHandler).inTask();
        statusTyp = (taskDb.getStatus()==TaskDb.Status.FORTEST.getValue() || taskDb.getStatus()==TaskDb.Status.END.getValue());
        Validator validator = CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100);

        if(statusTyp) {
            taskSave.setText("Zmień na : W Trakcie");
            validator = Validator.combine(Validator.createEmptyValidator("Pole jest wymagane"),validator);

        }

        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(taskComment, validator);


    }

    @ActionMethod("saveStatus")
    public void  saveStatus() throws VetoException, FlowException {
        if(validationSupport.isInvalid()) {
            validationSupport.initInitialDecoration();
        } else {
            Object[] msg;
            if(statusTyp) {
                msg = TaskDb.updateStatus(taskDb.getId(), TaskDb.Status.INPROGRES, taskComment.getText());
            } else {
                if(taskComment.getText().isEmpty()) msg = TaskDb.updateStatus(taskDb.getId(), TaskDb.Status.FORTEST);
                else msg = TaskDb.updateStatus(taskDb.getId(), TaskDb.Status.FORTEST, taskComment.getText());
            }
            if((boolean)msg[0]) {
                flowActionHandler.navigate(ShowTaskController.class);
            } else new Alert(Alert.AlertType.ERROR,""+msg[1]).show();


        }
    }
}
