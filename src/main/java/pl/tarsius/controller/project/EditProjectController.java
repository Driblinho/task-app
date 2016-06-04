package pl.tarsius.controller.project;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
import io.datafx.controller.flow.action.LinkAction;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.HomeController;
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
@FXMLController(value = "/view/app/newproject.fxml", title = "Edytuj projekt - Tarsius")
public class EditProjectController extends BaseController {


    /**
     * Pole mapujące TextField na tytuł projektu
     */
    @FXML private TextField newProjectTitleField;
    /**
     * Pole mapujące TextField na opis projektu
     */
    @FXML private TextArea newProjectDescField;
    /**
     * Pole mapujące DataPicker na date zakończenia projektu
     */
    @FXML
    private DatePicker newProjectDatePicker;

    /**
     * DataFX FlowActionHandler
     */
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    /**
     * Button odpowiadający za usuwanie daty z DataPickera
     */
    @FXML @ActionTrigger("cleanDate") private Button newProjectDatePickerClean;

    /**
     * Button obsługujący zapis projektu
     */
    @FXML
    @ActionTrigger("saveProject")
    private Button newProjectSave;
    /**
     * Buton obsługujący akcje cofania (Anuluj)
     */
    @FXML @BackAction private Button newProjectCancel;


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
        newProjectDatePicker.setDayCellFactory(new BlockDatePicker());
        newProjectTitleField.setText(project.getNazwa());
        newProjectDescField.setText(project.getOpis());
        if(project.getData_zakonczenia()!=null)
        newProjectDatePicker.setValue(project.getData_zakonczenia().toLocalDateTime().toLocalDate());
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(newProjectTitleField, Validator.combine(
                Validator.createEmptyValidator("Tytuł jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        ));
        validationSupport.registerValidator(newProjectDescField,false,CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków",200));
    }

    /**
     * Metoda aktaulizuje projekt, wyświetla komunikaty i przenosi do edytowanego projektu po pomyślnej modyfikacji
     */
    @ActionMethod("updateProject")
    public void updateProject() {
        if(validationSupport.isInvalid()) validationSupport.initInitialDecoration();
        else {
            Timestamp dz = null;
            if(newProjectDatePicker.getValue()!=null) {
                dz = Timestamp.valueOf(newProjectDatePicker.getValue().atStartOfDay());
            }
            Project p = new Project();
            Project projectM = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
            p.setProjekt_id(projectM.getProjekt_id());
            p.setNazwa(newProjectTitleField.getText());
            p.setOpis(newProjectDescField.getText());
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
        newProjectDatePicker.setValue(null);
    }

}
