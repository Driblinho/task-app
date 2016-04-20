package pl.tarsius.controller.project;

import io.datafx.controller.FXMLController;
import io.datafx.controller.flow.action.BackAction;
import io.datafx.controller.flow.action.LinkAction;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import pl.tarsius.controller.BaseController;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;

/**
 * Created by Ireneusz Kuliga on 20.04.16.
 */
@FXMLController(value = "/view/app/editproject.fxml", title = "TaskApp")
public class EditProject extends BaseController {

    @FXML
    @BackAction
    private Button editProjectCancel;

    @PostConstruct
    public void init() {
        new StockButtons(operationButtons,flowActionHandler).inProjectButton();
    }

}
