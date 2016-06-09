package pl.tarsius.util.validator.form;

import org.controlsfx.validation.Validator;
import pl.tarsius.util.validator.CustomValidator;

/**
 * Klasa przygotowująca {@link Validator} dla formularza zadań
 * Created by ireq on 10.05.16.
 */
public class TaskFormValidator {
    /**
     * Pobiera {@link Validator} dla nazwy zadania
     *
     * @return Validator
     */
    public static Validator getName() {
        return Validator.combine(
                Validator.createEmptyValidator("Pole jest wymagane"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        );
    }
    /**
     * Pobiera {@link Validator} dla opisu zadania
     *
     * @return Validator
     */
    public static Validator getDescription() {
        return Validator.combine(
                Validator.createEmptyValidator("Opis jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków", 200)
        );
    }
}
