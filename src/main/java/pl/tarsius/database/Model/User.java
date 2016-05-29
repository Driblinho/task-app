package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.util.ImageCloudinaryUpload;
import pl.tarsius.util.validator.PeselValidator;

import java.sql.*;
import java.time.LocalDate;

/**
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

    public long getUzytkownikId() {
        return uzytkownikId;
    }

    public void setUzytkownikId(long uzytkownikId) {
        this.uzytkownikId = uzytkownikId;
    }

    public String getHaslo() {
        return haslo;
    }

    public void setHaslo(String haslo) {
        this.haslo = haslo;
    }

    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public boolean isAktywny() {
        return aktywny;
    }

    public void setAktywny(boolean aktywny) {
        this.aktywny = aktywny;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public Timestamp getBlokada() {
        return blokada;
    }

    public void setBlokada(Timestamp blokada) {
        this.blokada = blokada;
    }

    public Date getDataUrodzenia() {
        return dataUrodzenia;
    }

    public void setDataUrodzenia(Date dataUrodzenia) {
        this.dataUrodzenia = dataUrodzenia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKodPocztowy() {
        return kodPocztowy;
    }

    public void setKodPocztowy(String kodPocztowy) {
        this.kodPocztowy = kodPocztowy;
    }

    public String getMiasto() {
        return miasto;
    }

    public void setMiasto(String miasto) {
        this.miasto = miasto;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }

    public int getNieudaneLogowania() {
        return nieudaneLogowania;
    }

    public void setNieudaneLogowania(int nieudaneLogowania) {
        this.nieudaneLogowania = nieudaneLogowania;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
        PeselValidator p = new PeselValidator(pesel);
        this.setDataUrodzenia(Date.valueOf(LocalDate.of(p.getBirthYear(), p.getBirthMonth(), p.getBirthDay())));
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public int getTyp() {
        return typ;
    }

    public void setTyp(int typ) {
        this.typ = typ;
    }

    public String getUlica() {
        return ulica;
    }

    public void setUlica(String ulica) {
        this.ulica = ulica;
    }

    public String getAvatarUrl() {
        return (this.getAvatarId() != null) ? "" + new ImageCloudinaryUpload().getUrl(this.getAvatarId()) : "assets/img/avatar.png";
    }

    public String getImieNazwisko() {
        return this.getImie() + " " + this.getNazwisko();
    }

    public boolean isAdmin() {
        return (this.typ == 3);
    }

    public boolean isManager() {
        return (this.typ == 2);
    }

    @Override
    public String toString() {
        return getImieNazwisko();
    }

    public static JdbcConverter<User> jdbcConverter() {
        return new JdbcConverter<User>() {
            @Override
            public User convertOneRow(ResultSet resultSet) {
                User user = new User();
                try {
                     user.setUzytkownikId(resultSet.getLong("uzytkownik_id"));
                     user.setImie(resultSet.getString("imie"));
                     user.setNazwisko(resultSet.getString("nazwisko"));
                     if(resultSet.getString("avatar_id")!=null) user.setAvatarId(resultSet.getString("avatar_id"));
                     user.setEmail(resultSet.getString("email"));
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
