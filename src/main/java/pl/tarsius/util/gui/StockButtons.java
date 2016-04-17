package pl.tarsius.util.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.datafx.controller.flow.context.ActionHandler;
import org.datafx.controller.flow.context.FlowActionHandler;
import pl.tarsius.controller.project.NewProjectController;

/**
 * Created by Ireneusz Kuliga on 15.04.16.
 */
public class StockButtons {

    @FXML private VBox container;
    @ActionHandler private FlowActionHandler flowActionHandler;

    public StockButtons(VBox container, FlowActionHandler flowActionHandler) {
        this.container = container;
        this.flowActionHandler = flowActionHandler;
    }

    public StockButtons(VBox container) {
        this.container = container;
    }

    private Button stockButton(String name) {
        Button b = new Button(name);
        b.getStyleClass().add("stock-button");
        return b;
    }

    public void homeAction() {
        Button b = this.stockButton("Nowy projekt");
        container.getChildren().add(b);
        flowActionHandler.attachLinkEventHandler(b, NewProjectController.class);
    }





}
