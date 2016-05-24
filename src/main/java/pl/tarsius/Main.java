package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */


import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.context.FXMLApplicationContext;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.StartupController;
import pl.tarsius.database.Model.ReportProject;
import pl.tarsius.util.GenReportProjects;
import pl.tarsius.util.gui.ResponsiveDesign;

import java.sql.SQLException;
import java.util.HashSet;

public class Main extends Application {

    @FXML private StackPane stackPane;
    @FXMLApplicationContext private ApplicationContext applicationContext;
    private Logger loger;
    @Override
    public void start(Stage primaryStage) throws FlowException, SQLException {

        loger = LoggerFactory.getLogger(getClass());


        //new GenReportProjects(new ReportProject().genProjects(null)).run();

        ApplicationContext.getInstance().register("userSession",new Object());
        ApplicationContext.getInstance().register("reportBucket", new HashSet<Long>());

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
