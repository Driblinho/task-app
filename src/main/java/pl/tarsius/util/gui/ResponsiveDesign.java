package pl.tarsius.util.gui;

import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
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
    public ResponsiveDesign(Stage stage) {
        this.stage = stage;
        this.bodyBorderPane = (BorderPane) stage.getScene().lookup(bodyBorderPaneName);
        this.logoImageView = (ImageView) stage.getScene().lookup(startUpLogoName);
        this.loadingStackPane = (StackPane) stage.getScene().lookup(loadingName);
        this.userBar = (GridPane) stage.getScene().lookup(userBarName);
        this.topMsgContent = (GridPane) stage.getScene().lookup(alertTopMsg);
    }




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

    public void resizeBodyHeight(Double height) {
        stage.getScene().getRoot().prefHeight(height);
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
        if(topMsgContent != null) {
            topMsgContent.setPrefWidth(width);
        }
    }

    private void resizeStartUpLogoHeight(Double height) {
        // TODO: 02.04.16 Skalowanie wysokości loga startowego
    }
}
