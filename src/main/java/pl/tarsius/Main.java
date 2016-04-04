package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.StartupController;
import pl.tarsius.util.gui.ResponsiveDesign;

public class Main extends Application {

    @FXML private StackPane stackPane;
    private Logger loger;
    @Override
    public void start(Stage primaryStage) throws FlowException {

        loger = loger = LoggerFactory.getLogger(getClass());
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        new Flow(StartupController.class).startInStage(primaryStage);
        new ResponsiveDesign(primaryStage);


    }


    public static void main(String[] args) {
        launch(args);
    }
}
