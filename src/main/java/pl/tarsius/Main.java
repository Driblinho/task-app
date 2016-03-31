package pl.tarsius;
/**
 * Created by Ireneusz Kuliga on 03.03.16.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.tarsius.database.SqlPrev;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        //InitializeConnection in = new InitializeConnection();
        //SqlPrev sqlPrev = new SqlPrev();
        //sqlPrev.getUsers();
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("view/startup/welcome.fxml"));

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Task App");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
