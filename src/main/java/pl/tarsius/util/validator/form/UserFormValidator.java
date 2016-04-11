package pl.tarsius.util.validator.form;

import org.controlsfx.validation.Validator;
import pl.tarsius.util.validator.CustomValidator;

import java.util.Collection;

/**
 * Created by Ireneusz Kuliga on 10.04.16.
 * Validator z ustawionymi wiadomościami
 */
public class UserFormValidator {
    public static Validator getEmail() {
        return Validator.combine(
                Validator.createEmptyValidator("Email jest wymagany"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100),
                CustomValidator.createEmailValidator("Nieprawidłowy email")
        );
    }

    public static Validator getPassword() {
        return Validator.combine(
                Validator.createEmptyValidator("Hasło jest wymagane"),
                CustomValidator.createPasswordValidator("Hasło nie może zwaierać spacji"),
                CustomValidator.createMinSizeValidator("Minimalnie 6 znaków", 6)
        );
    }

    public static Validator getName() {
        return Validator.combine(
                Validator.createEmptyValidator("Imię jest wymagane"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100),
                CustomValidator.createFirstNameValidator("Nieprawidlowe imie")
        );
    }

    public static Validator getSurname() {
        return Validator.combine(
                Validator.createEmptyValidator("Nazwisko jest wymagane"),
                CustomValidator.createLastNameValidator("Nieprawidłowe nazwisko"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 100)
        );
    }

    public static Validator getPhone() {
        return Validator.combine(
                Validator.createEmptyValidator("Numer telefonu jest wymagany"),
                CustomValidator.createPhoneValidator("Nieprawidłwy numer telefonu"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 16 znaków",16)
        );
    }

    public static Validator getPesel() {
        return CustomValidator.createPESELValidator("Nnieprawidłowy PESEL");
    }

    public static Validator getCity() {
        return Validator.combine(
                Validator.createEmptyValidator("Miasto jest wymagane"),
                CustomValidator.createCityValidator("Nieprawidłowa nazwa miasta"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 200 znaków", 200)
        );
    }

    public static Validator getStreet() {
        return Validator.combine(
                Validator.createEmptyValidator("Ulica jest wymagana"),
                CustomValidator.createMaxSizeValidator("Maksymalnie 100 znaków", 200)
        );
    }

    public static Validator getZip() {
        return Validator.combine(
                Validator.createEmptyValidator("Kod pocztowy jest wymagany"),
                CustomValidator.createZpiCodeValidator("Niepoprawny kod pocztowy")
        );
    }

    public static Validator getEqPassword(Collection password) {
        return Validator.createEqualsValidator("Hasła muszą być identyczne",password);
    }


}
