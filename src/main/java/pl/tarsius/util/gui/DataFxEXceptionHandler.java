package pl.tarsius.util.gui;

import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.scene.control.Alert;

/**
 * Created by ireq on 29.05.16.
 */
public class DataFxEXceptionHandler {

    public static void navigateQuietly(FlowActionHandler flowActionHandler, Class dest) {
        try {
            flowActionHandler.navigate(dest);
        } catch (VetoException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"DataFX ERROR").show();
        } catch (FlowException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"DataFX ERROR").show();
        }
    }
}
