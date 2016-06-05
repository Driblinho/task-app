package pl.tarsius.util.gui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/** Klasa skalująca elementy GUI
 * Created by Ireneusz Kuliga on 02.04.16.
 */
public class ResponsiveDesign {
    private final Stage stage;
    private final GridPane topMsgContent;
    private String startUpLogoName = "#logo";
    private String bodyBorderPaneName = "#body";
    private String loadingName = "#loading";
    private String userBarName = "#userBar";
    private String alertTopMsg = "#topMsgContent";
    private StackPane loadingStackPane;
    private BorderPane bodyBorderPane;
    private ImageView logoImageView;
    private GridPane userBar;
    private GridPane inviteItem;

    /** Inicjalizacja klasy pobierającej {@link Stage} aplikacji
     * @param stage
     */
    public ResponsiveDesign(Stage stage) {
        this.stage = stage;
        this.bodyBorderPane = (BorderPane) stage.getScene().lookup(bodyBorderPaneName);
        this.logoImageView = (ImageView) stage.getScene().lookup(startUpLogoName);
        this.loadingStackPane = (StackPane) stage.getScene().lookup(loadingName);
        this.userBar = (GridPane) stage.getScene().lookup(userBarName);
        this.topMsgContent = (GridPane) stage.getScene().lookup(alertTopMsg);
        this.inviteItem = (GridPane) stage.getScene().lookup(".inviteItem");
    }

    /** Skalowanie szerokości aplikacji
     * @param width
     */
    public void resizeBodyWidth(Double width) {
        stage.getScene().getRoot().prefWidth(width);
        resizeStartUpLogoWidth(width);
        if (bodyBorderPane != null) {
            bodyBorderPane.setPrefWidth(width);
        }
        if (loadingStackPane != null) {
            loadingStackPane.setPrefWidth(width);
        }
        if(userBar != null ) {
            userBar.setPrefWidth(width-220.0);
        }
    }

    /** Skalowanie wysokości aplikacji
     * @param height
     */
    public void resizeBodyHeight(Double height) {
        stage.getScene().getRoot().prefHeight(height);
        if (bodyBorderPane !=null) {
            bodyBorderPane.setPrefHeight(height);
        }
        if (loadingStackPane != null) {
            loadingStackPane.setPrefHeight(height);
        }
    }

    /**
     * Skalowanie startowego loga aplikacji
     * @param width Szerokość dla skalowanego elementu
     */
    private void resizeStartUpLogoWidth(Double width) {
        if (logoImageView!=null) {
            logoImageView.setFitWidth(width);
        }
        if(topMsgContent != null) {
            topMsgContent.setPrefWidth(width);
        }
    }

    /**
     * Skalowanie wysokości i szerokości aplikacji podczas przełączania kontrolera
     * @param Node do z widoku potrzebne do pobrania {@link Stage} oraz szerokości i wysokości
     */
    public static void scaleGUI(Node node) {
        Platform.runLater(() -> {
            new ResponsiveDesign((Stage) node.getParent().getScene().getWindow()).resizeBodyWidth(node.getParent().getScene().getWindow().getWidth());
            //-3.48% HACK
            double h = node.getParent().getScene().getWindow().getHeight();
            h = h-h*0.0248;
            new ResponsiveDesign((Stage) node.getParent().getScene().getWindow()).resizeBodyHeight(h);
        });
    }

}
