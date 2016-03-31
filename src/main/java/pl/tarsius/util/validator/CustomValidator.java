package pl.tarsius.util.validator;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;


/**
 * Created by Ireneusz Kuliga on 31.03.16.
 */
public class CustomValidator {

    /**
     * Metoda generująca {@link Validator<String>} dla adresu email
     * @param msg - Wiadomość tworzona gdy wartość jest nie własciwa
     * @param severity - rodzaj komunikatu dla towrzonej wiadomości
     * @return Validator<String>
     */
    public static Validator<String> createEmailValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$", severity);
    }

    /**
     * Metoda generująca {@link Validator<String>} dla adresu email
     * @param msg - Wiadomość tworzona gdy wartość jest nie własciwa
     * @return Validator<String>
     */
    public static Validator<String> createEmailValidator(String msg) {
        return createEmailValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator<String>} ograniczający długość danych
     * @param msg - Wiadomość tworzona gdy wartość jest nie własciwa
     * @param maxSize - Maksymalny długość
     * @param severity - rodzaj komunikatu dla towrzonej wiadomości
     * @return Validator<String>
     */
    public static Validator<String> createMaxSizeValidator(String msg, int maxSize, Severity severity) {
        return (control, value) -> {
            boolean condition = value.length()>maxSize;
            return ValidationResult.fromMessageIf(control,msg,severity,condition);
        };
    }

    /**
     * Metoda generująca {@link Validator<String>} ograniczający długość danych
     * @param msg - Wiadomość tworzona gdy wartość jest nie własciwa
     * @param maxSize - Maksymalny długość
     * @return Validator<String>
     */
    public static Validator<String> createMaxSizeValidator(String msg, int maxSize) {
        return createMaxSizeValidator(msg, maxSize, Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator<String>} określający minimalną długosć
     * @param msg - Wiadomość tworzona gdy wartość jest nie własciwa
     * @param minSize - Minimalna długość
     * @param severity - Rodzaj komunikatu dla towrzonej wiadomości
     * @return Validator<String>
     */
    public static Validator<String> createMinSizeValidator(String msg, int minSize, Severity severity) {
        return (control, value) -> {
            boolean condition = value.length()<minSize;
            return ValidationResult.fromMessageIf(control,msg,severity,condition);
        };
    }

    /**
     * Metoda generująca {@link Validator<String>} ograniczająca długość danych
     * @param msg - Wiadomość tworzona gdy wartość jest nie własciwa
     * @param minSize - Maksymalny długość
     * @return Validator<String>
     */
    public static Validator<String> createMinSizeValidator(String msg, int minSize) {
        return createMinSizeValidator(msg, minSize, Severity.ERROR);
    }


}
