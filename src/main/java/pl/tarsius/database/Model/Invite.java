package pl.tarsius.database.Model;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.io.converter.JdbcConverter;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.sql.*;
import java.util.List;

/**
 * Created by Jarosław Kuliga on 18.04.16.
 */
public class Invite {

    private  long zaproszeniId;
    private long projektId;
    private long uzytkownikId;
    private int stan;

    private String uzytkownikImieNazwisko;
    private Timestamp dataDodania;
    private Timestamp dataZakonczenia;
    private String nazwaProjektu;
    private long lider;

    private static Logger loger = LoggerFactory.getLogger(Invite.class);

    public Invite(long projektId, long uzytkownikId, int stan, String uzytkownikImieNazwisko) {
        this.projektId = projektId;
        this.stan = stan;
        this.uzytkownikId = uzytkownikId;
        this.uzytkownikImieNazwisko = uzytkownikImieNazwisko;
    }

    public Invite(long projektId, String nazwaProjektu, Timestamp dataZakonczenia, Timestamp dataDodania, String uzytkownikImieNazwisko, long uzytkownikId, long zaproszeniId, long lider) {
        this.projektId = projektId;
        this.nazwaProjektu = nazwaProjektu;
        this.dataZakonczenia = dataZakonczenia;
        this.dataDodania = dataDodania;
        this.uzytkownikImieNazwisko = uzytkownikImieNazwisko;
        this.uzytkownikId = uzytkownikId;
        this.zaproszeniId = zaproszeniId;
        this.lider = lider;
    }

    public Object[] save() {
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement= (PreparedStatement) connection.prepareStatement("insert into Zaproszenia (projekt_id,uzytkownik_id,stan) values (?,?,?);");
            preparedStatement.setLong(1,this.projektId);
            preparedStatement.setLong(2,this.uzytkownikId);
            preparedStatement.setInt(3,this.stan);
            preparedStatement.executeUpdate();
            return new Object[]{true, "Zaproszenie dodane"};
        } catch (SQLException e) {
            return new Object[]{false,"Błąd bazy danych"};
        }
    }

    public static Object[] saveList(List<Invite> lu) {
        int[] x=new int[1];
        String msg = "Użytkownicy zaproszeni do projektu";
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement= (PreparedStatement) connection.prepareStatement("insert into Zaproszenia (projekt_id,uzytkownik_id,stan) values (?,?,?);");
            lu.forEach(invite -> {
                try {
                    preparedStatement.setLong(1, invite.getProjektId());
                    preparedStatement.setLong(2, invite.getUzytkownikId());
                    preparedStatement.setInt(3, invite.getStan());
                    preparedStatement.addBatch();
                } catch (SQLException e) {
                    loger.debug("InvSaveListLoop: ", e);
                }
            });
            x = preparedStatement.executeBatch();
            return new Object[]{true, msg};
        } catch (SQLException e) {
            if (e.getSQLState().contains("23000")) {
                if(x.length>0) return new Object[]{true, msg};
            }
            loger.debug("svaeInvList:", e);
            return new Object[]{false, "Błąd bazy danych"};
        }
    }

    public static Object[] getUserInv(long userID) {
        String sql = "select p.projekt_id,p.nazwa,p.data_zakonczenia,lider,u.imie,u.nazwisko,z.data_dodania from Zaproszenia z,Projekty p,Uzytkownicy u where p.projekt_id=z.projekt_id and p.lider=u.uzytkownik_id and z.uzytkownik_id=? and stan=1;";
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement ps = (PreparedStatement) connection.prepareStatement(sql);
            ps.setLong(1,userID);
            ResultSet rs = ps.executeQuery();
            return new Object[]{ true };
        } catch (SQLException e) {
            loger.debug("getUserInv", e);
            return new Object[]{false,"Problem z bazą"};
        }

    }

    public static JdbcConverter<Invite> jdbcConverter() {
        return new JdbcConverter<Invite>() {
            @Override
            public Invite convertOneRow(ResultSet resultSet) {
                try {
                    String im = resultSet.getString("imie")+" "+resultSet.getString("nazwisko");
                    return new Invite(resultSet.getLong("projekt_id"),resultSet.getString("nazwa"),resultSet.getTimestamp("data_zakonczenia"),resultSet.getTimestamp("data_dodania"),im,resultSet.getLong("uzytkownik_id"),resultSet.getLong("zaproszenie_id"),resultSet.getLong("lider"));
                } catch (SQLException e) {
                    loger.debug("Inv jdbcConverter", e);
                    return null;
                }

            }
        };
    }

    public static Object[] remove(long id) {
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement ps = (PreparedStatement) connection.prepareStatement("delete from Zaproszenia where zaproszenie_id=?");
            ps.setLong(1,id);
            ps.executeUpdate();
            return new Object[] {true, "Zaproszenie usunięte"};
        } catch (SQLException e) {
            loger.debug("Usuwanie zaproszenia", e);
            return new Object[] {false, "Błąd podczas wykonywania zapytania"};
        }
    }


    public long getProjektId() {
        return projektId;
    }

    public long getUzytkownikId() {
        return uzytkownikId;
    }

    public int getStan() {
        return stan;
    }

    public Timestamp getDataDodania() {
        return dataDodania;
    }

    public Timestamp getDataZakonczenia() {
        return dataZakonczenia;
    }

    public String getNazwaProjektu() {
        return nazwaProjektu;
    }

    public String getUzytkownikImieNazwisko() {
        return uzytkownikImieNazwisko;
    }

    public long getZaproszeniId() {
        return zaproszeniId;
    }
}
