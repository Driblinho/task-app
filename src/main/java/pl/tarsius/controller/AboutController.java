package pl.tarsius.controller;

import io.datafx.controller.FXMLController;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;

/** Kontroler z informacjami o programie
 * Created by PC on 29.05.2016.
 */
@FXMLController(value = "/view/app/aboutUs.fxml", title = "O programie - Tarsius")
public class AboutController extends BaseController {

    /**
     * DataFX {@link FlowActionHandler}
     */
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    /**
     * Inicjalizacja kontrolera
     */
    @PostConstruct
    public void init () {
        new StockButtons(operationButtons,flowActionHandler).homeAction();
        breadCrumb.setSelectedCrumb(editTask);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());
    }
}
