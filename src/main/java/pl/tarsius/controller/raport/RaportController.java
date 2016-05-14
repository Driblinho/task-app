package pl.tarsius.controller.raport;

import io.datafx.controller.FXMLController;
import pl.tarsius.controller.BaseController;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;

/**
 * Created by ireq on 05.05.16.
 */
@FXMLController(value = "/view/app/generatingReports.fxml", title = "Generowanie Raportów TaskApp")
public class RaportController extends BaseController {

    @PostConstruct
    public void  init() {
        new StockButtons(operationButtons, flowActionHandler).homeAction();
    }

}
