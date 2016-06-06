package pl.tarsius.util.gui;

import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.scene.control.Alert;

/**
 * Klasa obsługująca nawigację DataFX
 * Created by ireq on 29.05.16.
 */
public class DataFxEXceptionHandler {

    /**
     * Nawigacja z obsługą wyjątków DataFX
     * @param flowActionHandler {@link FlowActionHandler} DataFx
     * @param dest Kontroler do którego aplikacja ma zostać przeniesiona
     */
    public static void navigateQuietly(FlowActionHandler flowActionHandler, Class dest) {
        try {
            flowActionHandler.navigate(dest);
        } catch (VetoException | FlowException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"DataFX ERROR").show();
        }
    }
}
