package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.flow.*;
import org.datafx.controller.flow.container.DefaultFlowContainer;
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

        //Font.loadFont(getClass().getResourceAsStream("assets/font/RobotoCondensed-Regular.ttf"), 14);
        //Font.loadFont(getClass().getResourceAsStream("assets/font/RobotoCondensed-Light.ttf"), 14);

        Flow flow = new Flow(StartupController.class);

        flow.startInStage(primaryStage);


        primaryStage.setWidth(1170.0);
        primaryStage.setHeight(835.0);
        primaryStage.setX(0.0);


        primaryStage.getScene().heightProperty().addListener((observable, oldValue, newValue) -> {
            new ResponsiveDesign(primaryStage).resizeBodyHeight(newValue.doubleValue());
            loger.info("H"+newValue);
        });

        primaryStage.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
            new ResponsiveDesign(primaryStage).resizeBodyWidth(newValue.doubleValue());
            loger.info("H"+newValue);

        });


    }


    @Override
    public void stop(){
    }


    public static void main(String[] args) {
        launch(args);
    }


}
