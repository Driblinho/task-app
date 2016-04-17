package pl.tarsius.controller.project;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.datafx.controller.FXMLController;
import org.datafx.controller.FxmlLoadException;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.action.ActionMethod;
import org.datafx.controller.flow.action.ActionTrigger;
import org.datafx.controller.flow.action.LinkAction;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FXMLViewFlowContext;
import org.datafx.controller.flow.context.FlowActionHandler;
import org.datafx.controller.flow.context.ViewFlowContext;
import org.datafx.controller.util.VetoException;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.HomeController;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.ResponsiveDesign;
import pl.tarsius.util.gui.StockButtons;
import pl.tarsius.util.validator.CustomValidator;

import javax.annotation.PostConstruct;
import java.sql.*;

/**
 * Created by Jarosław Kuliga on 14.04.16.
 */
@FXMLController(value = "/view/app/newproject.fxml", title = "TaskApp")
public class NewProjectController extends BaseController{
    @FXML private TextField newProjectTitleField;
    @FXML private TextField newProjectDescField;
    @FXML private DatePicker newProjectDatePicker;

    @ActionHandler private FlowActionHandler flowActionHandler;

    @FXML
    @ActionTrigger("cleanDate") private Button newProjectDatePickerClean;

    @FXML
    @ActionTrigger("saveProject")
    private Button newProjectSave;
    @FXML @LinkAction(HomeController.class) private Button newProjectCancel;
    @FXMLApplicationContext private ApplicationContext applicationContext;

    @FXMLViewFlowContext private ViewFlowContext viewContext;
    ValidationSupport validationSupport;


    @PostConstruct
    public void init() throws FxmlLoadException {

        new StockButtons(operationButtons, flowActionHandler).homeAction();

        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(newProjectTitleField, Validator.combine(
                Validator.createEmptyValidator("Tytuł jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        ));
        validationSupport.registerValidator(newProjectDescField,false,CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków",200));



    }

   @ActionMethod("saveProject")
   public void saveProject() throws VetoException, FlowException {

       if(validationSupport.isInvalid()) {
        validationSupport.initInitialDecoration();
       } else {
           Timestamp dz = null;
           if(newProjectDatePicker.getValue()!=null) {
               dz = Timestamp.valueOf(newProjectDatePicker.getValue().atStartOfDay());
           }
           User user = (User) applicationContext.getRegisteredObject("userSession");
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
                   try {
                       new Alert(Alert.AlertType.INFORMATION, (String) task.getValue()[2]).show();
                       applicationContext.register("projectId", task.getValue()[1]);
                       flowActionHandler.navigate(ShowProject.class);
                   } catch (VetoException e) {
                       e.printStackTrace();
                   } catch (FlowException e) {
                       e.printStackTrace();
                   }
               } else {
                   new Alert(Alert.AlertType.ERROR, (String) task.getValue()[2]).show();
               }
           });

           new Thread(task).start();
       }

   }

    @ActionMethod("cleanDate")
    public void cleanDate() {
        newProjectDatePicker.setValue(null);
    }


}
