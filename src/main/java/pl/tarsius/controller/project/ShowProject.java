package pl.tarsius.controller.project;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.ResultSetRow;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.FlowException;
import org.datafx.controller.flow.action.ActionMethod;
import org.datafx.controller.flow.action.ActionTrigger;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FlowActionHandler;
import org.datafx.controller.util.VetoException;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.ProfileController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by Ireneusz Kuliga on 15.04.16.
 */
@FXMLController(value = "/view/app/widokprojektu.fxml", title = "TaskApp")
public class ShowProject extends BaseController{
    @FXMLApplicationContext private ApplicationContext applicationContext;

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
        new StockButtons(operationButtons, flowActionHandler).homeAction();

       project = Project.getProject((long)applicationContext.getRegisteredObject("projectId"));
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
