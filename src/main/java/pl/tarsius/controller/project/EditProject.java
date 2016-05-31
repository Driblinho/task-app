package pl.tarsius.controller.project;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
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
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.BlockDatePicker;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

/**
 * Created by Ireneusz Kuliga on 20.04.16.
 */
@FXMLController(value = "/view/app/editproject.fxml", title = "Edytuj projekt - Tarsius")
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

    private ValidationSupport validationSupport;

    @PostConstruct
    public void init() {
        new StockButtons(operationButtons,flowActionHandler).inProjectButton();
        Project project = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
        editProjectDatePicker.setDayCellFactory(new BlockDatePicker());
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
    }




    @ActionMethod("updateProject")
    public void updateProject() {
        if(validationSupport.isInvalid()) validationSupport.initInitialDecoration();
        else {
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
                    return Project.updateProject(p);
                }
            };
            task.setOnRunning(event -> loading.setVisible(true));
            task.setOnSucceeded(event -> {
                loading.setVisible(false);
                if((boolean)task.getValue()[0]) DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowProject.class);
                else  new Alert(Alert.AlertType.ERROR,""+task.getValue()[1]);
            });

            new Thread(task).start();

        }
    }

    @ActionMethod("cleanDate")
    public void cleanDate() {
        editProjectDatePicker.setValue(null);
    }

}
