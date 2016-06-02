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
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.Model.Project;
import pl.tarsius.util.gui.BlockDatePicker;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

/**
 * Kontroler odpowiadający za edycje projektu
 * Created by Ireneusz Kuliga on 20.04.16.
 */
@FXMLController(value = "/view/app/editproject.fxml", title = "Edytuj projekt - Tarsius")
public class EditProjectController extends BaseController {


    /**
     * Pole tekstowe na tytuł projektu (FXML)
     */
    @FXML
    private TextField editProjectTitleField;
    /**
     * Pole tekstowe na opis projektu (FXML)
     */
    @FXML
    private TextField editProjectDescField;
    /**
     * Pole na DatePicker z FXML
     */
    @FXML
    private DatePicker editProjectDatePicker;

    /**
     * Pole obsługujące usuwanie daty z DataPickera
     */
    @FXML
    @ActionTrigger("cleanDate")
    private Button editProjectDatePickerClean;

    /**
     * Pole obsługujące zapisywanie formularza
     */
    @FXML
    @ActionTrigger("updateProject")
    private Button editProjectSave;

    /**
     * Pole obsługujące akcje wstecz (Anuluj)
     */
    @FXML @BackAction
    private Button editProjectCancel;

    /**
     * Pole na validator formularza
     */
    private ValidationSupport validationSupport;

    /**
     * Metoda inicjalizująca Kontroler Edytujący projekt
     */
    @PostConstruct
    public void init() {
        new StockButtons(operationButtons,flowActionHandler).inProjectButton();

        breadCrumb.setSelectedCrumb(editProject);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

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


    /**
     * Metoda aktaulizuje projekt, wyświetla komunikaty i przenosi do edytowanego projektu po pomyślnej modyfikacji
     */
    @ActionMethod("updateProject")
    public void updateProject() {
        if(validationSupport.isInvalid()) validationSupport.initInitialDecoration();
        else {
            Timestamp dz = null;
            if(editProjectDatePicker.getValue()!=null) {
                dz = Timestamp.valueOf(editProjectDatePicker.getValue().atStartOfDay());
            }
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
                if((boolean)task.getValue()[0]) DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowProjectController.class);
                else  new Alert(Alert.AlertType.ERROR,""+task.getValue()[1]);
            });

            new Thread(task).start();

        }
    }

    /**
     * Metoda usuwa datę z DataPickera
     */
    @ActionMethod("cleanDate")
    public void cleanDate() {
        editProjectDatePicker.setValue(null);
    }

}
