package pl.tarsius.util.validator;

import javafx.scene.control.TextField;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import java.util.ArrayList;


/**
 * Klasa dostarczająca {@link Validator} dla systemu
 * Created by Ireneusz Kuliga on 31.03.16.
 */
public class CustomValidator {

    /**
     * Metoda generująca {@link Validator} dla adresu email
     * @param msg - Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity - rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createEmailValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$", severity);
    }

    /**
     * Metoda generująca {@link Validator} dla adresu email
     * @param msg - Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createEmailValidator(String msg) {
        return createEmailValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator} ograniczający długość danych
     * @param msg - Wiadomość tworzona gdy wartość jest nie właściwa
     * @param maxSize - Maksymalny długość
     * @param severity - rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createMaxSizeValidator(String msg, int maxSize, Severity severity) {
        return (control, value) -> {
            boolean condition = value.length()>maxSize;
            return ValidationResult.fromMessageIf(control,msg,severity,condition);
        };
    }

    /**
     * Metoda generująca {@link Validator} ograniczający długość danych
     * @param msg - Wiadomość tworzona gdy wartość jest nie właściwa
     * @param maxSize - Maksymalny długość
     * @return Validator
     */
    public static Validator<String> createMaxSizeValidator(String msg, int maxSize) {
        return createMaxSizeValidator(msg, maxSize, Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator} określający minimalną długość
     * @param msg - Wiadomość tworzona gdy wartość jest nie właściwa
     * @param minSize - Minimalna długość
     * @param severity - Rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createMinSizeValidator(String msg, int minSize, Severity severity) {
        return (control, value) -> {
            boolean condition = value.length()<minSize;
            return ValidationResult.fromMessageIf(control,msg,severity,condition);
        };
    }

    /**
     * Metoda generująca {@link Validator} ograniczająca długość danych
     * @param msg - Wiadomość tworzona gdy wartość jest nie właściwa
     * @param minSize - Maksymalny długość
     * @return Validator
     */
    public static Validator<String> createMinSizeValidator(String msg, int minSize) {
        return createMinSizeValidator(msg, minSize, Severity.ERROR);
    }

    /** Metoda generująca {@link Validator} dla imienia
     * @param msg Wiadomość
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createFirstNameValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}", severity);
    }

    /** Metoda generująca {@link Validator} dla imienia
     * @param msg Wiadomość
     * @return Validator
     */
    public static Validator<String> createFirstNameValidator(String msg) {
        return createFirstNameValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator} dla imienia
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createLastNameValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}", severity);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createLastNameValidator(String msg) {
        return createLastNameValidator(msg,Severity.ERROR);
    }

    /** Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createPhoneValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^\\+(?:[0-9] ?){6,14}[0-9]$", severity);
    }

    /** Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createPhoneValidator(String msg) {
        return createPhoneValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createPESELValidator(String msg, Severity severity) {
        return (control, value) -> {
            PeselValidator peselValidator = new PeselValidator(value);
            return ValidationResult.fromMessageIf(control,msg,severity,!peselValidator.isValid());
        };
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createPESELValidator(String msg) {
        return createPESELValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createCityValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^([a-zA-Z\\u0080-\\u024F]+(?:(\\. )|-| |'))*[a-zA-Z\\u0080-\\u024F]*$", severity);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createCityValidator(String msg) {
        return createCityValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createZpiCodeValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "[0-9]{2}-[0-9]{3}", severity);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createZpiCodeValidator(String msg) {
        return createZpiCodeValidator(msg,Severity.ERROR);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @param severity rodzaj komunikatu dla tworzonej wiadomości
     * @return Validator
     */
    public static Validator<String> createPasswordValidator(String msg, Severity severity) {
        return Validator.createRegexValidator(msg, "^[^\\s]+$", severity);
    }

    /**
     * Metoda generująca {@link Validator}
     * @param msg Wiadomość tworzona gdy wartość jest nie właściwa
     * @return Validator
     */
    public static Validator<String> createPasswordValidator(String msg) {
        return createPasswordValidator(msg,Severity.ERROR);
    }

}