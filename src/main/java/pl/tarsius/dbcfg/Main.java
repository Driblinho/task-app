package pl.tarsius.dbcfg;

import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import pl.tarsius.dbcfg.Controller.StartupController;

/**
 * Created by ireq on 08.06.16.
 */
public class Main extends Application{

    public void start(Stage primaryStage) throws Exception {
        Flow flow = new Flow(StartupController.class);
        try {
            flow.startInStage(primaryStage);
        } catch (FlowException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Błąd DataFX").show();
        }
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
