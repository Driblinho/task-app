package pl.tarsius.util.gui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
public class ResponsiveDesign {
    private final Stage stage;
    private String startUpLogoName = "#logo";
    private String bodyBorderPaneName = "#body";
    private String loadingName = "#loading";
    private StackPane loadingStackPane;
    private BorderPane bodyBorderPane;
    private ImageView logoImageView;
    public ResponsiveDesign(Stage stage) {
        this.stage = stage;
        this.bodyBorderPane = (BorderPane) stage.getScene().lookup(bodyBorderPaneName);
        this.logoImageView = (ImageView) stage.getScene().lookup(startUpLogoName);
        this.loadingStackPane = (StackPane) stage.getScene().lookup(loadingName);
        this.startUp();
    }

    public void startUp() {

        this.resizeStartUpLogoWidth(stage.getScene().getWidth());
        this.resizeBodyWidth(stage.getScene().getWidth());
        this.resizeBodyHeight(stage.getScene().getHeight());

        stage.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
            this.resizeStartUpLogoWidth(newValue.doubleValue());
            this.resizeBodyWidth(newValue.doubleValue());


        });

        stage.getScene().heightProperty().addListener((observable, oldValue, newValue) -> {
            this.resizeBodyHeight(newValue.doubleValue());
        });

    }

    private void resizeBodyWidth(Double width) {
        if (bodyBorderPane != null) {
            bodyBorderPane.setPrefWidth(width);
        }
        if (loadingStackPane != null) {
            loadingStackPane.setPrefWidth(width);
        }
    }

    private void resizeBodyHeight(Double height) {
        if (bodyBorderPane !=null) {
            bodyBorderPane.setPrefHeight(height);
        }
        if (loadingStackPane != null) {
            loadingStackPane.setPrefHeight(height);
        }
    }

    private void resizeStartUpLogoWidth(Double width) {
        if (logoImageView!=null) {
            logoImageView.setFitWidth(width);
        }
    }

    private void resizeStartUpLogoHeight(Double height) {
        // TODO: 02.04.16 Skalowanie wysokości loga startowego
    }
}
