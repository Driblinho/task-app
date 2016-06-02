package pl.tarsius.util.gui;

import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.Callback;

import java.time.LocalDate;

/** Klasa obsługująca blokowanie wstecznej daty w {@link DatePicker}
 * Created by ireq on 24.05.16.
 */
public class BlockDatePicker implements Callback<DatePicker, DateCell> {
    /** {@inheritDoc} */
    @Override
    public DateCell call(DatePicker param) {
        return new DateCell(){
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        };
    }
}
