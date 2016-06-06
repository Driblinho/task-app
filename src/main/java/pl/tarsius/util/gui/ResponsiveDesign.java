package pl.tarsius.util.gui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Klasa skalująca elementy GUI
 * Created by Ireneusz Kuliga on 02.04.16.
 */
public class ResponsiveDesign {
    /**
     * Pole na referencje so {@link Stage} aplikacji
     */
    private final Stage stage;
    /**
     * Pole na referencje do startowych wiadomości
     */
    private final GridPane topMsgContent;
    /**
     * ID loga startowego
     */
    private String startUpLogoName = "#logo";
    /**
     * ID ciała aplikacji
     */
    private String bodyBorderPaneName = "#body";
    /**
     * ID Loadingu
     */
    private String loadingName = "#loading";
    /**
     * ID Paska użytkownika
     */
    private String userBarName = "#userBar";
    /**
     * ID Wiadomości: ekran startowy
     */
    private String alertTopMsg = "#topMsgContent";
    /**
     * Pole na referencje {@link StackPane} loadingu
     */
    private StackPane loadingStackPane;
    /**
     * Pole na referencje ciała aplikacji
     */
    private BorderPane bodyBorderPane;
    /**
     * Pole na referencje loga startowego
     */
    private ImageView logoImageView;
    /**
     * Pole na referencje paska użytkownika
     */
    private GridPane userBar;

    /** Inicjalizacja klasy pobierającej {@link Stage} aplikacji
     * @param stage {@link Stage} potrzebny do skalowania GUI
     */
    public ResponsiveDesign(Stage stage) {
        this.stage = stage;
        this.bodyBorderPane = (BorderPane) stage.getScene().lookup(bodyBorderPaneName);
        this.logoImageView = (ImageView) stage.getScene().lookup(startUpLogoName);
        this.loadingStackPane = (StackPane) stage.getScene().lookup(loadingName);
        this.userBar = (GridPane) stage.getScene().lookup(userBarName);
        this.topMsgContent = (GridPane) stage.getScene().lookup(alertTopMsg);
    }

    /** Skalowanie szerokości aplikacji
     * @param width szerokość do której GUI ma być skalowane
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
     * @param height Wysokość do której GUI ma być skalowane
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
     * @param node {@link Node} do z widoku potrzebne do pobrania {@link Stage} oraz szerokości i wysokości
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
