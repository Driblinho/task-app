package pl.tarsius.controller.project;


import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.context.FXMLApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.ProfileController;
import pl.tarsius.database.Model.Project;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;

/**
 * Created by Ireneusz Kuliga on 15.04.16.
 */
@FXMLController(value = "/view/app/widokprojektu.fxml", title = "TaskApp")
public class ShowProject extends BaseController{

    @FXML private Label inprojectTitle;
    @FXML private Text inprojectDesc;
    @FXML private Text inprojectDataStart;
    @FXML private Text inprojectDataStop;
    @FXML private Label inprojectDataStopLabel;
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    @FXML
    @ActionTrigger("showAuthorProfile")
    private Hyperlink inprojectAuthor;

    private Project project;

    @PostConstruct
    public void init(){
        project = Project.getProject((long)ApplicationContext.getInstance().getRegisteredObject("projectId"));
        ApplicationContext.getInstance().register("projectModel", project);
        new StockButtons(operationButtons, flowActionHandler).inProjectButton();
       ApplicationContext.getInstance().register("projectLider", project.getLider());
            inprojectTitle.setText(project.getNazwa());
            inprojectDesc.setText(project.getOpis());
            inprojectDataStart.setText(project.getData_dodania().toString());
            inprojectAuthor.setText(project.getLiderImieNazwisko());
            Timestamp dz = project.getData_zakonczenia();
            if(dz!=null) {
                inprojectDataStop.setText(dz.toString());
            } else {
                inprojectDataStop.setVisible(false);
                inprojectDataStopLabel.setVisible(false);
            }
    }

    @ActionMethod("showAuthorProfile")
    public void showAuthorProfile() throws VetoException, FlowException {
        System.out.println("OOOPEN"+project.getLider());

        flowActionHandler.navigate(ProfileController.class);

    }

}
