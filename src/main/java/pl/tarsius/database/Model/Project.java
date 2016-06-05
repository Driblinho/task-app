package pl.tarsius.database.Model;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import io.datafx.io.converter.JdbcConverter;
import javafx.collections.ObservableList;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.controller.project.AddToProjectController;
import pl.tarsius.database.InitializeConnection;

import java.sql.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Ireneusz Kuliga on 16.04.16.
 */
public class Project {
    private static Logger loger = LoggerFactory.getLogger(Project.class);
    private String nazwa;
    private String opis;
    private long lider;
    private String liderImieNazwisko;
    private Timestamp data_dodania;
    private Timestamp data_zakonczenia;
    private long projekt_id;
    private int status;

    public Project() {
        this.data_dodania = null;
        this.data_zakonczenia = null;
        this.lider = 0;
        this.liderImieNazwisko = null;
        this.nazwa = null;
        this.opis = null;
        this.projekt_id = 0;
    }

    public Project(String nazwa, String opis, long lider, Timestamp data_dodania, Timestamp data_zakonczenia) {
        this.data_dodania = data_dodania;
        this.data_zakonczenia = data_zakonczenia;
        this.lider = lider;
        this.opis = opis;
        this.nazwa = nazwa;
    }

    public Project(long projekt_id, String nazwa, String opis, long lider, Timestamp data_dodania, Timestamp data_zakonczenia) {
        this(nazwa, opis, lider, data_dodania, data_zakonczenia);
        this.projekt_id = projekt_id;
    }

    public Project(String nazwa, String opis, long lider, Timestamp data_zakonczenia) {
        this(nazwa, opis, lider, null, data_zakonczenia);
    }

    public Project(long projekt_id, String nazwa, String opis, long lider, Timestamp data_zakonczenia) {
        this(nazwa, opis, lider, null, data_zakonczenia);
        this.projekt_id = projekt_id;
    }

    public static Object[] updateProject(Project project) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = new InitializeConnection().connect();
            ps = (PreparedStatement) connection.prepareStatement("UPDATE Projekty SET nazwa=?, opis =?, data_zakonczenia =? WHERE projekt_id =?");
            ps.setString(1, project.getNazwa());
            ps.setString(2, project.getOpis());
            ps.setTimestamp(3, project.data_zakonczenia);
            ps.setLong(4, project.getProjekt_id());
            ps.executeUpdate();
            return new Object[]{true, "Zaktualizowano signalProject"};
        } catch (SQLException e) {
            e.printStackTrace();
            return new Object[]{false, "Błąd bazy danych"};
        } finally {
            DbUtils.closeQuietly(connection, ps, null);
        }
    }

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

    public static Project getProject(long projekt_id) {
        Connection connection = null;
        Project project = null;
        try {
            connection = new InitializeConnection().connect();

            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement("select * from Projekty where projekt_id = ?");
            preparedStatement.setLong(1, projekt_id);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            project = new Project(resultSet.getString("nazwa"), resultSet.getString("opis"), resultSet.getLong("lider"), resultSet.getTimestamp("data_zakonczenia"));
            project.setData_dodania(resultSet.getTimestamp("data_dodania"));
            project.setProjekt_id(resultSet.getLong("projekt_id"));
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

    public static Object[] removeUserFormProject(Long userId, Long projectId) {
        try {
            Connection connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            PreparedStatement ps = (PreparedStatement) connection.prepareStatement("DELETE FROM ProjektyUzytkownicy WHERE projekt_id=? AND uzytkownik_id=?;");
            ps.setLong(1, projectId);
            ps.setLong(2, userId);
            int del = ps.executeUpdate();
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

    public Timestamp getData_dodania() {
        return data_dodania;
    }

    public void setData_dodania(Timestamp data_dodania) {
        this.data_dodania = data_dodania;
    }

    public Timestamp getData_zakonczenia() {
        return data_zakonczenia;
    }

    public void setData_zakonczenia(Timestamp data_zakonczenia) {
        this.data_zakonczenia = data_zakonczenia;
    }

    public long getLider() {
        return lider;
    }

    public void setLider(long lider) {
        this.lider = lider;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public long getProjekt_id() {
        return projekt_id;
    }

    public void setProjekt_id(long projekt_id) {
        this.projekt_id = projekt_id;
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getLiderImieNazwisko() {
        return liderImieNazwisko;
    }

    public void setLiderImieNazwisko(String imie, String nazwisko) {
        this.liderImieNazwisko = imie + " " + nazwisko;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isOpen() {
        return this.status == 1;
    }

    public boolean isClose() {
        return !isOpen();
    }
}
