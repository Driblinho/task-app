package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.flow.Flow;
import org.datafx.controller.flow.FlowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.HomeController;
import pl.tarsius.controller.StartupController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.util.gui.ResponsiveDesign;

import java.sql.SQLException;

public class Main extends Application {

    @FXML private StackPane stackPane;
    private Logger loger;
    @Override
    public void start(Stage primaryStage) throws FlowException, SQLException {

        loger = LoggerFactory.getLogger(getClass());


        ApplicationContext.getInstance().register("connection", new InitializeConnection().connect());

        ApplicationContext.getInstance().register("userSesion", null);


        new Flow(StartupController.class).startInStage(primaryStage);
        primaryStage.setWidth(1170.0);
        primaryStage.setHeight(835.0);
        primaryStage.getScene().heightProperty().addListener((observable, oldValue, newValue) -> {
            new ResponsiveDesign(primaryStage).resizeBodyHeight(newValue.doubleValue());
            loger.info("H"+newValue);
        });

        primaryStage.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
            new ResponsiveDesign(primaryStage).resizeBodyWidth(newValue.doubleValue());
            loger.info("W"+newValue);
        });


    }


    @Override
    public void stop(){
    }


    public static void main(String[] args) {
        launch(args);
    }


}
