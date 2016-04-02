package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.datafx.controller.context.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.Controller;

import java.io.IOException;

public class Main extends Application {

    @FXML private StackPane stackPane;
    private Logger loger;
    @Override
    public void start(Stage primaryStage) throws IOException {

        //InitializeConnection in = new InitializeConnection();
        //SqlPrev sqlPrev = new SqlPrev();
        //sqlPrev.getUsers();

        loger = LoggerFactory.getLogger(getClass());

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("view/startup/welcome.fxml"));
        Parent root = fxmlLoader.load();
        Parent loadingOverlay = FXMLLoader.load(getClass().getClassLoader().getResource("view/loading.fxml"));


        ApplicationContext.getInstance().register("User","Dane");

        ApplicationContext.getInstance().register("loadingOverlay",loadingOverlay);

        stackPane=new StackPane();
        stackPane.setAlignment(Pos.TOP_CENTER);

        stackPane.getChildren().addAll(root,loadingOverlay);

        Scene scene = new Scene(stackPane, 800, 600);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            loger.info("Sakluj serokość  BorderPane i ładowania");
            ((BorderPane)root).setPrefWidth(newValue.doubleValue());
            ((StackPane)loadingOverlay).setPrefWidth(newValue.doubleValue());
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
                loger.info("Sakluj wyskokośc ładowania");
                ((StackPane)loadingOverlay).setPrefHeight(newValue.doubleValue());
        });

        primaryStage.setTitle("Task App");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        ((Controller) fxmlLoader.getController()).start();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
