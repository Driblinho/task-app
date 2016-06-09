package pl.tarsius.database.Model;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import io.datafx.io.converter.JdbcConverter;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.sql.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Klasa reprezentująca Projekt
 */
public class Project {
    private static Logger loger = LoggerFactory.getLogger(Project.class);
    private String nazwa;
    private String opis;
    private long lider;
    private String liderImieNazwisko;
    private Timestamp dataDodania;
    private Timestamp data_zakonczenia;
    private long projektId;
    private int status;

    /**
     * Domyślny konstruktor
     */
    public Project() {}

    /**
     * Konstruktor inicjalizujący
     * @param nazwa Nazwa projektu
     * @param opis Opis projektu
     * @param lider ID projektu
     * @param dataDodania {@link Timestamp} dodania projektu
     * @param dataZakonczenia {@link Timestamp} zakończenia projektu
     */
    public Project(String nazwa, String opis, long lider, Timestamp dataDodania, Timestamp dataZakonczenia) {
        this.dataDodania = dataDodania;
        this.data_zakonczenia = dataZakonczenia;
        this.lider = lider;
        this.opis = opis;
        this.nazwa = nazwa;
    }

    /**
     * Konstruktor inicjalizujący
     * @param projektId ID projektu
     * @param nazwa Nazwa projektu
     * @param opis Opis projektu
     * @param lider ID lidera projektu
     * @param dataDodania {@link Timestamp} daty dodania
     * @param dataZakonczenia {@link Timestamp} daty zakończenia
     */
    public Project(long projektId, String nazwa, String opis, long lider, Timestamp dataDodania, Timestamp dataZakonczenia) {
        this(nazwa, opis, lider, dataDodania, dataZakonczenia);
        this.projektId = projektId;
    }

    /**
     * Konstruktor inicjalizujący
     * @param nazwa Nazwa projektu
     * @param opis Opis projektu
     * @param lider ID lidera projektu
     * @param dataZakonczenia {@link Timestamp} daty zakończenia projektu
     */
    public Project(String nazwa, String opis, long lider, Timestamp dataZakonczenia) {
        this(nazwa, opis, lider, null, dataZakonczenia);
    }

    /**
     * Konstruktor inicjalizujący
     * @param projektId ID projektu
     * @param nazwa Nazwa projektu
     * @param opis Opis projektu
     * @param lider ID lidera projektu
     * @param dataZakonczenia {@link Timestamp} daty projektu
     */
    public Project(long projektId, String nazwa, String opis, long lider, Timestamp dataZakonczenia) {
        this(nazwa, opis, lider, null, dataZakonczenia);
        this.projektId = projektId;
    }

    /**
     * Metoda aktualizująca projekt
     * @param project Obiekt klasy {@link Project} który maz zostać zmieniony
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] updateProject(Project project) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = new InitializeConnection().connect();
            ps = (PreparedStatement) connection.prepareStatement("UPDATE Projekty SET nazwa=?, opis =?, data_zakonczenia =? WHERE projekt_id =?");
            ps.setString(1, project.getNazwa());
            ps.setString(2, project.getOpis());
            ps.setTimestamp(3, project.data_zakonczenia);
            ps.setLong(4, project.getProjektId());
            ps.executeUpdate();
            return new Object[]{true, "Zaktualizowano signalProject"};
        } catch (SQLException e) {
            loger.debug("Aktualizacja projektu", e);
            return new Object[]{false, "Błąd bazy danych"};
        } finally {
            DbUtils.closeQuietly(connection, ps, null);
        }
    }

    /**
     * Tworzy nowy projekt
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public Object[] save() {
        String sql = "INSERT INTO `Projekty` " +
                "(`nazwa`, `opis`,`data_zakonczenia`, `lider`, `status`) " +
                "VALUES (?, ?, ?, ?, ?);";
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            preparedStatement = (PreparedStatement) connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            preparedStatement.setString(i++, this.nazwa);
            preparedStatement.setString(i++, this.opis);
            if (this.data_zakonczenia != null) preparedStatement.setTimestamp(i++, data_zakonczenia);
            else preparedStatement.setNull(i++, Types.TIMESTAMP);
            preparedStatement.setLong(i++, this.lider);
            preparedStatement.setInt(i++, 1);
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            long lastId = resultSet.getLong(1);

            sql = "insert into ProjektyUzytkownicy (uzytkownik_id,projekt_id,lider) values( ?, ?, ?);";
            preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setLong(1, this.lider);
            preparedStatement.setLong(2, lastId);
            preparedStatement.setInt(3, 1);
            preparedStatement.executeUpdate();


            connection.commit();
            return new Object[]{true, lastId, "Nowy projekt został dodany"};
        } catch (SQLException e) {
            loger.debug("Dodawanie nowego projektu", e);
            return new Object[]{false, null, "Błąd bazy danych"};
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, resultSet);
        }
    }

    /**
     * Metoda dodająca użytkownika do projektu
     * @param userId ID użytkownika
     * @param projectId ID projektu do którego ma zostać dodany użytkownik
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] addUserToProject(long userId, long projectId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            String sql = "insert into ProjektyUzytkownicy (uzytkownik_id,projekt_id) values (?,?)";
            preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, projectId);
            preparedStatement.executeUpdate();
            return new Object[]{true, "Użytkownik dodany"};
        } catch (SQLException e) {
            loger.debug("dodawnaie do projektu uzytkownika", e);
            return new Object[]{false, "Błąd bazy danych"};
        } finally {
            DbUtils.closeQuietly(connection, preparedStatement, null);
        }
    }

    /**
     * Metoda pobiera projekt z bazy
     * @param projektId ID projektu do pobrania
     * @return Obiekt {@link Project} z wypełnionymi danymi dla istniejącego projektu lub null dla nie znaleźnego projektu
     */
    public static Project getProject(long projektId) {
        Connection connection = null;
        Project project = null;
        try {
            connection = new InitializeConnection().connect();

            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement("select * from Projekty where projekt_id = ?");
            preparedStatement.setLong(1, projektId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            project = new Project(resultSet.getString("nazwa"), resultSet.getString("opis"), resultSet.getLong("lider"), resultSet.getTimestamp("data_zakonczenia"));
            project.setDataDodania(resultSet.getTimestamp("data_dodania"));
            project.setProjektId(resultSet.getLong("projekt_id"));
            project.setStatus(resultSet.getInt("status"));

            DbUtils.closeQuietly(null, preparedStatement, resultSet);

            preparedStatement = (PreparedStatement) connection.prepareStatement("select imie,nazwisko from Uzytkownicy where uzytkownik_id=?");
            preparedStatement.setLong(1, project.getLider());
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            project.setLiderImieNazwisko(resultSet.getString("imie"), resultSet.getString("nazwisko"));
        } catch (SQLException e) {
            loger.debug("getProject", e);
        } finally {
            DbUtils.closeQuietly(connection);
            return project;
        }
    }

    public static JdbcConverter<Project> jdbcConverter() {
        return new JdbcConverter<Project>() {
            @Override
            public Project convertOneRow(ResultSet resultSet) {
                try {
                    Project p = new Project(
                            resultSet.getLong("projekt_id"),
                            resultSet.getString("nazwa"),
                            resultSet.getString("opis"),
                            resultSet.getLong("l_id"),
                            resultSet.getTimestamp("data_dodania"),
                            resultSet.getTimestamp("data_zakonczenia"));
                    p.setLiderImieNazwisko(resultSet.getString("l_imie"), resultSet.getString("l_nazwisko"));
                    p.setStatus(resultSet.getInt("status"));
                    return p;
                } catch (SQLException e) {
                    loger.debug("JDBC CONVERTER: ", e);
                }
                return null;
            }
        };
    }

    /**
     * Dodaje do projektu użytkowników
     * @param users {@link HashSet} z id użytkowników do dodania
     * @param projectId ID projektu do dodania
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] addUsersToProject(HashSet<Long> users,long projectId) {
        Connection connection = null;
        PreparedStatement ps = null;
        Object[] msg = new Object[]{false,"Nie wybrano użytkowników do dodania"};
        if(users.size()==0) return msg;

        try {
            connection =  new InitializeConnection().connect();

            String sql = "INSERT INTO ProjektyUzytkownicy\n" +
                    "(uzytkownik_id, projekt_id)\n" +
                    "VALUES(?, ?);";
            connection.setAutoCommit(false);

            for (Long user: users) {
                ps = (PreparedStatement) connection.prepareStatement(sql);
                ps.setLong(1,user);
                ps.setLong(2,projectId);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.createStatement().executeUpdate("delete from Zaproszenia where projekt_id = "+projectId+" and uzytkownik_id in ("+users.toString().replace("[","").replace("]","")+")");
            connection.commit();
            msg = new Object[]{true,"Użytkownicy zostali dodani do projektu"};
        } catch (SQLException e) {
            loger.debug("addUserToProject", e);
            msg = new Object[]{false,"Błąd podczas dodawania do bazy"};
        } finally {
            DbUtils.closeQuietly(connection,ps,null);
            return msg;
        }
    }

    /**
     * Pobiera statystyki projektu i zwraca mapę z danymi
     * @param projectId ID projektu
     * @return Statystyki projektu w postaci {@link Map} klucz: {@link pl.tarsius.database.Model.TaskDb.Status} Wartość: {@link Long}
     */
    public static Map<TaskDb.Status, Long> getStatistic(Long projectId) {
        Map<TaskDb.Status, Long> stat = new HashMap<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = new InitializeConnection().connect();
            ps = (PreparedStatement) connection.prepareStatement("select stan,count(*) from Zadania where projekt_id=? group by stan;");
            ps.setLong(1, projectId);
            rs = ps.executeQuery();
            while (rs.next())
                stat.put(TaskDb.Status.valueOf(rs.getInt(1)), rs.getLong(2));
        } catch (SQLException e) {
            loger.debug("PROJECT STAT", e);
        } finally {
            DbUtils.closeQuietly(connection, ps, rs);
            return stat;
        }
    }

    /**
     * Metoda usuwająca użytkownika z projektu
     * @param userId ID użytkownika
     * @param projectId ID projektu
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] removeUserFormProject(Long userId, Long projectId) {
        try {
            Connection connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            PreparedStatement ps = (PreparedStatement) connection.prepareStatement("DELETE FROM ProjektyUzytkownicy WHERE projekt_id=? AND uzytkownik_id=?;");
            ps.setLong(1, projectId);
            ps.setLong(2, userId);
            ps.executeUpdate();
            ps = (PreparedStatement) connection.prepareStatement("UPDATE Zadania SET uzytkownik_id=NULL,stan=1 WHERE projekt_id=? AND uzytkownik_id=?");
            ps.setLong(1, projectId);
            ps.setLong(2, userId);
            ps.executeUpdate();
            connection.commit();
            return new Object[] {true, "Użytkownik został usunięty z projektu"};
        } catch (SQLException e) {
            loger.debug("removeUserFormProject", e);
            return new Object[] {false, "Błąd podczas wykonywania zapytania"};
        }
    }

    /**
     * Getter for property 'dataDodania'.
     *
     * @return Value for property 'dataDodania'.
     */
    public Timestamp getDataDodania() {
        return dataDodania;
    }

    /**
     * Setter for property 'dataDodania'.
     *
     * @param dataDodania Value to set for property 'dataDodania'.
     */
    public void setDataDodania(Timestamp dataDodania) {
        this.dataDodania = dataDodania;
    }

    /**
     * Getter for property 'dataZakonczenia'.
     *
     * @return Value for property 'dataZakonczenia'.
     */
    public Timestamp getDataZakonczenia() {
        return data_zakonczenia;
    }

    /**
     * Setter for property 'dataZakonczenia'.
     *
     * @param dataZakonczenia Value to set for property 'dataZakonczenia'.
     */
    public void setDataZakonczenia(Timestamp dataZakonczenia) {
        this.data_zakonczenia = dataZakonczenia;
    }

    /**
     * Getter for property 'lider'.
     *
     * @return Value for property 'lider'.
     */
    public long getLider() {
        return lider;
    }

    /**
     * Setter for property 'lider'.
     *
     * @param lider Value to set for property 'lider'.
     */
    public void setLider(long lider) {
        this.lider = lider;
    }

    /**
     * Getter for property 'opis'.
     *
     * @return Value for property 'opis'.
     */
    public String getOpis() {
        return opis;
    }

    /**
     * Setter for property 'opis'.
     *
     * @param opis Value to set for property 'opis'.
     */
    public void setOpis(String opis) {
        this.opis = opis;
    }

    /**
     * Getter for property 'projektId'.
     *
     * @return Value for property 'projektId'.
     */
    public long getProjektId() {
        return projektId;
    }

    /**
     * Setter for property 'projektId'.
     *
     * @param projektId Value to set for property 'projektId'.
     */
    public void setProjektId(long projektId) {
        this.projektId = projektId;
    }

    /**
     * Getter for property 'nazwa'.
     *
     * @return Value for property 'nazwa'.
     */
    public String getNazwa() {
        return nazwa;
    }

    /**
     * Setter for property 'nazwa'.
     *
     * @param nazwa Value to set for property 'nazwa'.
     */
    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    /**
     * Getter for property 'liderImieNazwisko'.
     *
     * @return Value for property 'liderImieNazwisko'.
     */
    public String getLiderImieNazwisko() {
        return liderImieNazwisko;
    }

    /**
     * Setter ustawiający imię i nazwisko lidera
     * @param imie imię
     * @param nazwisko nazwisko
     */
    public void setLiderImieNazwisko(String imie, String nazwisko) {
        this.liderImieNazwisko = imie + " " + nazwisko;
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Getter for property 'open'.
     *
     * @return Value for property 'open'.
     */
    public boolean isOpen() {
        return this.status == 1;
    }

    /**
     * Getter for property 'close'.
     *
     * @return Value for property 'close'.
     */
    public boolean isClose() {
        return !isOpen();
    }
}
