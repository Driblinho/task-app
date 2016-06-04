package pl.tarsius.controller.project;



import io.datafx.controller.FXMLController;
import io.datafx.controller.FxmlLoadException;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.LinkAction;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.HomeController;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.BlockDatePicker;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

/**
 * Kontroler odpowiadający za dodawanie nowego projektu
 * Created by Jarosław Kuliga on 14.04.16.
 */
@FXMLController(value = "/view/app/newproject.fxml", title = "Dodaj projekt - Tarsius")
public class NewProjectController extends BaseController {

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
    @FXML @LinkAction(HomeController.class) private Button newProjectCancel;

    /**
     * Pole na validator formularza
     */
    private ValidationSupport validationSupport;


    /**
     * Metoda inicjalizująca kontroler
     */
    @PostConstruct
    public void init() {

        new StockButtons(operationButtons, flowActionHandler).homeAction();

        breadCrumb.setSelectedCrumb(newProject);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        newProjectDatePicker.setDayCellFactory(new BlockDatePicker());
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(newProjectTitleField, Validator.combine(
                Validator.createEmptyValidator("Tytuł jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        ));
        validationSupport.registerValidator(newProjectDescField,false,CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków",200));



    }

    /**
     * Metoda zapisująca nowy  projekt do bazy
     */
   @ActionMethod("saveProject")
   public void saveProject() throws VetoException, FlowException {

       if(validationSupport.isInvalid()) {
        validationSupport.initInitialDecoration();
       } else {
           Timestamp dz = null;
           if(newProjectDatePicker.getValue()!=null) {
               dz = Timestamp.valueOf(newProjectDatePicker.getValue().atStartOfDay());
           }
           User user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
           Project project = new Project(
                   newProjectTitleField.getText(),
                   newProjectDescField.getText(),user.getUzytkownikId(),
                   dz
           );




           Task<Object[]> task = new Task<Object[]>() {
               @Override
               protected Object[] call() throws Exception {
                   return project.save();
               }
           };
           task.setOnRunning(event -> loading.setVisible(true));
           task.setOnFailed(event -> {loading.setVisible(false);
               new Alert(Alert.AlertType.ERROR, "Problem z wykonaniem dodawania").show();
           });
           task.setOnSucceeded(event -> {
               if((boolean)task.getValue()[0]) {
                   new Alert(Alert.AlertType.INFORMATION, (String) task.getValue()[2]).show();
                   ApplicationContext.getInstance().register("projectId", task.getValue()[1]);
                   DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowProjectController.class);
               } else {
                   new Alert(Alert.AlertType.ERROR, (String) task.getValue()[2]).show();
               }
           });

           new Thread(task).start();
       }

   }

    /**
     * Metoda usuwająca datę z DataPickera
     */
    @ActionMethod("cleanDate")
    public void cleanDate() {
        newProjectDatePicker.setValue(null);
    }


}
