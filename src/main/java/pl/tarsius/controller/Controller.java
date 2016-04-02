package pl.tarsius.controller;

import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.datafx.controller.context.ApplicationContext;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
public abstract class Controller implements Initializable {

    private static final String  LOADING_CONTEXT = "loadingOverlay";


    public void setLoading(boolean status) {
        if (status) {
            this.startLoading();
        } else {
            this.stopLoading();
        }
    }

    private void startLoading() {
        ((StackPane) ApplicationContext.getInstance().getRegisteredObject(Controller.LOADING_CONTEXT)).setVisible(true);
    }

    private void stopLoading() {
        ((StackPane) ApplicationContext.getInstance().getRegisteredObject(Controller.LOADING_CONTEXT)).setVisible(false);
    }

    public void start() {

    }
}
