package pl.tarsius.util.validator;

import javafx.scene.control.TextField;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import java.util.ArrayList;


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

    public static Validator<String> createFirstNameValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "[A-Z][a-zA-Z]*", severity);
    }

    public static Validator<String> createFirstNameValidator(String msg) {
        return Validator.createRegexValidator(msg, "[A-Z][a-zA-Z]*", Severity.ERROR);
    }

    public static Validator<String> createLastNameValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}", severity);
    }

    public static Validator<String> createLastNameValidator(String msg) {
        return createLastNameValidator(msg,Severity.ERROR);
    }

    public static Validator<String> createPhoneValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^\\+(?:[0-9] ?){6,14}[0-9]$", severity);
    }

    public static Validator<String> createPhoneValidator(String msg) {
        return createPhoneValidator(msg,Severity.ERROR);
    }

    public static Validator<String> createPESELValidator(String msg, Severity severity) {
        return (control, value) -> {
            PeselValidator peselValidator = new PeselValidator(value);
            return ValidationResult.fromMessageIf(control,msg,severity,!peselValidator.isValid());
        };
    }

    public static Validator<String> createPESELValidator(String msg) {
        return createPESELValidator(msg,Severity.ERROR);
    }

    public static Validator<String> createCityValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^([a-zA-Z\\u0080-\\u024F]+(?:(\\. )|-| |'))*[a-zA-Z\\u0080-\\u024F]*$", severity);
    }

    public static Validator<String> createCityValidator(String msg) {
        return createCityValidator(msg,Severity.ERROR);
    }

    public static Validator<String> createZpiCodeValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "[0-9]{2}-[0-9]{3}", severity);
    }

    public static Validator<String> createZpiCodeValidator(String msg) {
        return createZpiCodeValidator(msg,Severity.ERROR);
    }

    public static Validator<String> createPasswordValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^[^\\s]+$", severity);
    }

    public static Validator<String> createPasswordValidator(String msg) {
        return createPasswordValidator(msg,Severity.ERROR);
    }



}
