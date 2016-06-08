package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */


import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.StartupController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.util.Mail;
import pl.tarsius.util.gui.ResponsiveDesign;

import java.io.File;
import java.io.FilenameFilter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Główna klasa aplikacji inicjalizująca aplikację
 */
public class Main extends Application {

    private static Logger loger = LoggerFactory.getLogger(Main.class);

    /**
     * Inicjalizacja aplikacji JavaFX
     * @param primaryStage {@link Stage}
     */
    @Override
    public void start(Stage primaryStage) {


        ApplicationContext.getInstance().register("userSession",new Object());//
        ApplicationContext.getInstance().register("reportBucket", new HashSet<Long>()); //Inicjalizuje koszyk raportów
        ApplicationContext.getInstance().register("version", "0.1"); //Określa wersje aplikacji
        ApplicationContext.getInstance().register("appName", "Tarsius"); //Określa wersje aplikacji
        InitializeConnection.configLoader(); //Ładowanie dko ApplicationContext konfiguracji połączenia z bazą danych

        Flow flow = new Flow(StartupController.class);
        try {
            flow.startInStage(primaryStage);
        } catch (FlowException e) {
           new Alert(Alert.AlertType.ERROR,"Błąd DataFX").show();
        }
        primaryStage.setWidth(1170.0);
        primaryStage.setHeight(835.0);
        primaryStage.centerOnScreen();
        primaryStage.setMinWidth(1138);
        primaryStage.setMinHeight(800);

        primaryStage.setMaximized(true);


        primaryStage.getScene().heightProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> new ResponsiveDesign(primaryStage).resizeBodyHeight(newValue.doubleValue()));
            loger.info("H"+newValue);
        });

        primaryStage.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> new ResponsiveDesign(primaryStage).resizeBodyWidth(newValue.doubleValue()));
            loger.info("W"+newValue);
        });
    }


    /**
     * Metoda usuwa podczas zamykania aplikacji wygenerowane raporty pdf
     */
    @Override
    public void stop(){
        URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path = URLDecoder.decode(url.getFile(), "UTF-8");
            final File folder = new File(new File("").getAbsolutePath());
            final File[] files = folder.listFiles( new FilenameFilter() {
                @Override
                public boolean accept( final File dir,
                                       final String name ) {
                    return name.matches( ".*\\.pdf" );
                }
            } );
            for ( final File file : files ) {
                if ( !file.delete() ) {
                    System.err.println( "Can't remove " + file.getAbsolutePath() );
                }
            }



        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        launch(args);
    }


}
