package pl.tarsius.util.validator.form;

import org.controlsfx.validation.Validator;
import pl.tarsius.util.validator.CustomValidator;

import java.util.Collection;

/**
 * Validator z ustawionymi wiadomościami
 * Created by Ireneusz Kuliga on 10.04.16.
 */
public class UserFormValidator {
    /**
     * Pobiera {@link Validator} dla adresu email
     *
     * @return Validator
     */
    public static Validator getEmail() {
        return Validator.combine(
                Validator.createEmptyValidator("Email jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 254 znaków", 254),
                CustomValidator.createEmailValidator("Nieprawidłowy email")
        );
    }

    /**
     * Pobiera {@link Validator} dla hasła
     *
     * @return Validator
     */
    public static Validator getPassword() {
        return Validator.combine(
                Validator.createEmptyValidator("Hasło jest wymagane"),
                CustomValidator.createPasswordValidator("Hasło nie może zwierać spacji"),
                CustomValidator.createMinSizeValidator("Minimalnie 6 znaków", 6)
        );
    }

    /**
     * Pobiera {@link Validator} dla imienia
     *
     * @return Validator
     */
    public static Validator getName() {
        return Validator.combine(
                Validator.createEmptyValidator("Imię jest wymagane"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100),
                CustomValidator.createFirstNameValidator("Nieprawidłowe imię")
        );
    }

    /**
     * Pobiera {@link Validator} dla nazwiska
     *
     * @return Validator
     */
    public static Validator getSurname() {
        return Validator.combine(
                Validator.createEmptyValidator("Nazwisko jest wymagane"),
                CustomValidator.createLastNameValidator("Nieprawidłowe nazwisko"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        );
    }

    /**
     * Pobiera {@link Validator} dla numeru telefonu
     *
     * @return Validator
     */
    public static Validator getPhone() {
        return Validator.combine(
                Validator.createEmptyValidator("Numer telefonu jest wymagany"),
                CustomValidator.createPhoneValidator("Nieprawidłowy numer telefonu"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 16 znaków",16)
        );
    }

    /**
     * Pobiera {@link Validator} dla nr pesel
     *
     * @return Validator
     */
    public static Validator getPesel() {
        return CustomValidator.createPESELValidator("Nnieprawidłowy PESEL");
    }

    /**
     * Pobiera {@link Validator} dla miasta
     *
     * @return Validator
     */
    public static Validator getCity() {
        return Validator.combine(
                Validator.createEmptyValidator("Miasto jest wymagane"),
                CustomValidator.createCityValidator("Nieprawidłowa nazwa miasta"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków", 200)
        );
    }

    /**
     * Pobiera {@link Validator} dla ulicy
     *
     * @return Validator
     */
    public static Validator getStreet() {
        return Validator.combine(
                Validator.createEmptyValidator("Ulica jest wymagana"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 200)
        );
    }

    /**
     * Pobiera {@link Validator} dla kodu pocztowego
     *
     * @return Validator
     */
    public static Validator getZip() {
        return Validator.combine(
                Validator.createEmptyValidator("Kod pocztowy jest wymagany"),
                CustomValidator.createZpiCodeValidator("Niepoprawny kod pocztowy")
        );
    }

    /**
     * Pobiera {@link Validator} dla hasła
     * @param password {@link Collection} z hasłem do porównania
     * @return  Validator
     */
    public static Validator getEqPassword(Collection password) {
        return Validator.createEqualsValidator("Hasła muszą być identyczne",password);
    }


}
