package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.util.ImageCloudinaryUpload;
import pl.tarsius.util.validator.PeselValidator;

import java.sql.*;
import java.time.LocalDate;

/**
 * Klasa reprezentująca dane użytkownika
 * Created by Ireneusz Kuliga on 05.04.16.
 */
public class User {
    private long uzytkownikId;
    private String email;
    private String nazwisko;
    private String imie;
    private int typ;
    private Date dataUrodzenia;
    private String telefon;
    private String avatarId;
    private String kodPocztowy;
    private String miasto;
    private String ulica;
    private boolean aktywny;
    private String pesel;
    private int nieudaneLogowania;
    private Timestamp blokada;
    private String haslo;


    /**
     * Getter for property 'uzytkownikId'.
     *
     * @return Value for property 'uzytkownikId'.
     */
    public long getUzytkownikId() {
        return uzytkownikId;
    }

    /**
     * Setter for property 'uzytkownikId'.
     *
     * @param uzytkownikId Value to set for property 'uzytkownikId'.
     */
    public void setUzytkownikId(long uzytkownikId) {
        this.uzytkownikId = uzytkownikId;
    }

    /**
     * Getter for property 'haslo'.
     *
     * @return Value for property 'haslo'.
     */
    public String getHaslo() {
        return haslo;
    }

    /**
     * Setter for property 'haslo'.
     *
     * @param haslo Value to set for property 'haslo'.
     */
    public void setHaslo(String haslo) {
        this.haslo = haslo;
    }

    /**
     * Getter for property 'imie'.
     *
     * @return Value for property 'imie'.
     */
    public String getImie() {
        return imie;
    }

    /**
     * Setter for property 'imie'.
     *
     * @param imie Value to set for property 'imie'.
     */
    public void setImie(String imie) {
        this.imie = imie;
    }

    /**
     * Getter for property 'aktywny'.
     *
     * @return Value for property 'aktywny'.
     */
    public boolean isAktywny() {
        return aktywny;
    }

    /**
     * Setter for property 'aktywny'.
     *
     * @param aktywny Value to set for property 'aktywny'.
     */
    public void setAktywny(boolean aktywny) {
        this.aktywny = aktywny;
    }

    /**
     * Getter for property 'avatarId'.
     *
     * @return Value for property 'avatarId'.
     */
    public String getAvatarId() {
        return avatarId;
    }

    /**
     * Setter for property 'avatarId'.
     *
     * @param avatarId Value to set for property 'avatarId'.
     */
    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    /**
     * Getter for property 'blokada'.
     *
     * @return Value for property 'blokada'.
     */
    public Timestamp getBlokada() {
        return blokada;
    }

    /**
     * Setter for property 'blokada'.
     *
     * @param blokada Value to set for property 'blokada'.
     */
    public void setBlokada(Timestamp blokada) {
        this.blokada = blokada;
    }

    /**
     * Getter for property 'dataUrodzenia'.
     *
     * @return Value for property 'dataUrodzenia'.
     */
    public Date getDataUrodzenia() {
        return dataUrodzenia;
    }

    /**
     * Setter for property 'dataUrodzenia'.
     *
     * @param dataUrodzenia Value to set for property 'dataUrodzenia'.
     */
    public void setDataUrodzenia(Date dataUrodzenia) {
        this.dataUrodzenia = dataUrodzenia;
    }

    /**
     * Getter for property 'email'.
     *
     * @return Value for property 'email'.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for property 'email'.
     *
     * @param email Value to set for property 'email'.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for property 'kodPocztowy'.
     *
     * @return Value for property 'kodPocztowy'.
     */
    public String getKodPocztowy() {
        return kodPocztowy;
    }

    /**
     * Setter for property 'kodPocztowy'.
     *
     * @param kodPocztowy Value to set for property 'kodPocztowy'.
     */
    public void setKodPocztowy(String kodPocztowy) {
        this.kodPocztowy = kodPocztowy;
    }

    /**
     * Getter for property 'miasto'.
     *
     * @return Value for property 'miasto'.
     */
    public String getMiasto() {
        return miasto;
    }

    /**
     * Setter for property 'miasto'.
     *
     * @param miasto Value to set for property 'miasto'.
     */
    public void setMiasto(String miasto) {
        this.miasto = miasto;
    }

    /**
     * Getter for property 'nazwisko'.
     *
     * @return Value for property 'nazwisko'.
     */
    public String getNazwisko() {
        return nazwisko;
    }

    /**
     * Setter for property 'nazwisko'.
     *
     * @param nazwisko Value to set for property 'nazwisko'.
     */
    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }

    /**
     * Getter for property 'nieudaneLogowania'.
     *
     * @return Value for property 'nieudaneLogowania'.
     */
    public int getNieudaneLogowania() {
        return nieudaneLogowania;
    }

    /**
     * Setter for property 'nieudaneLogowania'.
     *
     * @param nieudaneLogowania Value to set for property 'nieudaneLogowania'.
     */
    public void setNieudaneLogowania(int nieudaneLogowania) {
        this.nieudaneLogowania = nieudaneLogowania;
    }

    /**
     * Getter for property 'pesel'.
     *
     * @return Value for property 'pesel'.
     */
    public String getPesel() {
        return pesel;
    }

    /**
     * Setter for property 'pesel'.
     *
     * @param pesel Value to set for property 'pesel'.
     */
    public void setPesel(String pesel) {
        this.pesel = pesel;
        PeselValidator p = new PeselValidator(pesel);
        this.setDataUrodzenia(Date.valueOf(LocalDate.of(p.getBirthYear(), p.getBirthMonth(), p.getBirthDay())));
    }

    /**
     * Getter for property 'telefon'.
     *
     * @return Value for property 'telefon'.
     */
    public String getTelefon() {
        return telefon;
    }

    /**
     * Setter for property 'telefon'.
     *
     * @param telefon Value to set for property 'telefon'.
     */
    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    /**
     * Getter for property 'typ'.
     *
     * @return Value for property 'typ'.
     */
    public int getTyp() {
        return typ;
    }

    /**
     * Setter for property 'typ'.
     *
     * @param typ Value to set for property 'typ'.
     */
    public void setTyp(int typ) {
        this.typ = typ;
    }

    /**
     * Getter for property 'ulica'.
     *
     * @return Value for property 'ulica'.
     */
    public String getUlica() {
        return ulica;
    }

    /**
     * Setter for property 'ulica'.
     *
     * @param ulica Value to set for property 'ulica'.
     */
    public void setUlica(String ulica) {
        this.ulica = ulica;
    }

    /**
     * Getter for property 'avatarUrl'.
     *
     * @return Value for property 'avatarUrl'.
     */
    public String getAvatarUrl() {
        if(this.getAvatarId() != null) {
            String img = new ImageCloudinaryUpload().getUrl(this.getAvatarId());

            if (ImageCloudinaryUpload.exists(img)) return img;
        }
        return  "assets/img/avatar.png";

    }

    /**
     * Getter for property 'imieNazwisko'.
     *
     * @return Value for property 'imieNazwisko'.
     */
    public String getImieNazwisko() {
        return this.getImie() + " " + this.getNazwisko();
    }

    /**
     * Getter for property 'admin'.
     *
     * @return Value for property 'admin'.
     */
    public boolean isAdmin() {
        return (this.typ == 3);
    }

    /**
     * Getter for property 'manager'.
     *
     * @return Value for property 'manager'.
     */
    public boolean isManager() {
        return (this.typ == 2);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getImieNazwisko();
    }

    /** JdbcConverter DataFx dla pobieranych danych użytkownika
     * @return JdbcConverter
     */
    public static JdbcConverter<User> jdbcConverter() {
        return new JdbcConverter<User>() {
            @Override
            public User convertOneRow(ResultSet resultSet) {
                User user = new User();
                try {
                    user.setUzytkownikId(resultSet.getLong("uzytkownik_id"));
                    user.setImie(resultSet.getString("imie"));
                    user.setNazwisko(resultSet.getString("nazwisko"));
                    if (resultSet.getString("avatar_id") != null) user.setAvatarId(resultSet.getString("avatar_id"));
                    user.setEmail(resultSet.getString("email"));
                    user.setAktywny(resultSet.getBoolean("aktywny"));
                    user.setTyp(resultSet.getInt("typ"));
                    return user;
                } catch (SQLException e) {
                    // TODO: 21.04.16 LOG
                    e.printStackTrace();
                } finally {
                    return user;
                }
            }
        };
    }
}
