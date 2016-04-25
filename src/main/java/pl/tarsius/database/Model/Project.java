package pl.tarsius.database.Model;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import io.datafx.io.converter.JdbcConverter;
import javafx.collections.ObservableList;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.sql.*;
import java.sql.Connection;

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

    public static Object[] updateProject(Project project, ObservableList<User> users){
        try {
            Connection connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            PreparedStatement ps = (PreparedStatement) connection.prepareStatement("UPDATE Projekty SET nazwa=?, opis =?, data_zakonczenia =? WHERE projekt_id =?");
            ps.setString(1,project.getNazwa());
            ps.setString(2,project.getOpis());
            ps.setTimestamp(3,project.data_zakonczenia);
            ps.setLong(4,project.getProjekt_id());
            ps.executeUpdate();
            ps = (PreparedStatement) connection.prepareStatement("delete from ProjektyUzytkownicy where projekt_id=? and lider!=1;");
            ps.setLong(1,project.getProjekt_id());
            ps.executeUpdate();

            for (User user : users) {
                ps = (PreparedStatement) connection.prepareStatement("insert into ProjektyUzytkownicy (uzytkownik_id,projekt_id) values (?,?)");
                ps.setLong(1, user.getUzytkownikId());
                ps.setLong(2, project.getProjekt_id());
                ps.addBatch();
            }
            ps.executeBatch();

            connection.commit();


            return new Object[]{true,"Zaktualizowano projekt"};
        } catch (SQLException e) {
            e.printStackTrace();
            return new Object[]{false,"Błąd bazy danych"};
        }
    }

    public Object[] save() {
        String sql = "INSERT INTO `Projekty` " +
                "(`nazwa`, `opis`,`data_zakonczenia`, `lider`, `status`) " +
                "VALUES (?, ?, ?, ?, ?);";
        try {
            Connection connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement;

            preparedStatement = (PreparedStatement) connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS);
                int i = 1;
                preparedStatement.setString(i++, this.nazwa);
                preparedStatement.setString(i++, this.opis);
                if(this.data_zakonczenia!=null) preparedStatement.setTimestamp(i++, data_zakonczenia);
                else preparedStatement.setNull(i++, Types.TIMESTAMP);
                preparedStatement.setLong(i++, this.lider);
                preparedStatement.setInt(i++, 1);
                preparedStatement.executeUpdate();
                ResultSet resultSet = preparedStatement.getGeneratedKeys();
                resultSet.next();
                long lastId = resultSet.getLong(1);

            sql = "insert into ProjektyUzytkownicy (uzytkownik_id,projekt_id,lider) values( ?, ?, ?);";
            preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setLong(1, this.lider);
            preparedStatement.setLong(2,lastId);
            preparedStatement.setInt(3,1);
            preparedStatement.executeUpdate();


            connection.commit();
            return new Object[] {true, lastId, "Nowy projekt został dodany"};
        } catch (SQLException e) {
            loger.debug("Dodawanie nowego projektu", e);
            return new Object[] {false,null,"Błąd bazy danych"};
        }
    }

    public static Object[] addUserToProject(long userId,long projectId) {
        try {
            Connection connection = new InitializeConnection().connect();
            String sql = "insert into ProjektyUzytkownicy (uzytkownik_id,projekt_id) values (?,?)";
            PreparedStatement preparedStatement = (PreparedStatement) connection.prepareStatement(sql);
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, projectId);
            int i = preparedStatement.executeUpdate();
            //sql = "";
            //if(i>0)
            return new Object[] {true,"Użytkownik dodany"};
        } catch (SQLException e) {
            loger.debug("dodawnaie do projektu uzytkownika", e);
            return new Object[] {false,"Błąd bazy danych"};
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

            project = new Project(resultSet.getString("nazwa"),resultSet.getString("opis"),resultSet.getLong("lider"),resultSet.getTimestamp("data_zakonczenia"));
            project.setData_dodania(resultSet.getTimestamp("data_dodania"));
            project.setProjekt_id(resultSet.getLong("projekt_id"));

            DbUtils.closeQuietly(null,preparedStatement,resultSet);

            preparedStatement = (PreparedStatement) connection.prepareStatement("select imie,nazwisko from Uzytkownicy where uzytkownik_id=?");
            preparedStatement.setLong(1, project.getLider());
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            project.setLiderImieNazwisko(resultSet.getString("imie"), resultSet.getString("imie"));
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
                    p.setLiderImieNazwisko(resultSet.getString("l_imie"),resultSet.getString("l_nazwisko"));
                    return p;
                } catch (SQLException e) {
                    loger.debug("JDBC CONVERTER: ",e);
                }
                return null;
            }
        };
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
        this.liderImieNazwisko = imie+" "+nazwisko;
    }
}
