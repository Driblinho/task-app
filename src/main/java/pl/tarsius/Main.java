package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */


import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.StartupController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.util.gui.ResponsiveDesign;

import java.sql.SQLException;
import java.util.HashSet;

public class Main extends Application {

    private static Logger loger = LoggerFactory.getLogger(Main.class);
    @Override
    public void start(Stage primaryStage) throws FlowException, SQLException {


        ApplicationContext.getInstance().register("userSession",new Object());//
        ApplicationContext.getInstance().register("reportBucket", new HashSet<Long>()); //Inicjalizuje koszyk raportów
        ApplicationContext.getInstance().register("version", "0.1"); //Określa wersje aplikacji
        ApplicationContext.getInstance().register("appName", "Tarsius"); //Określa wersje aplikacji
        InitializeConnection.configLoader(); //Ładowanie dko ApplicationContext konfiguracji połączenia z bazą danych

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
