package pl.tarsius.database.Model;

import com.mysql.jdbc.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ireq on 28.04.16.
 */
public class TaskDb {
    private Long id;
    private String name;
    private  String desc;
    private int status;
    private Long projectId;

    private static Logger loger = LoggerFactory.getLogger(TaskDb.class);

    public TaskDb(Long id, String name, String desc, int status, Long projectId) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.status = status;
        this.projectId = projectId;
    }

    public TaskDb(String name, String desc, int status, Long projectId) {
        this(null,name,desc,status,projectId);
    }

    public enum Status {
        NEW(1),INPROGRES(2),FORTEST(3),END(0);
        private final int value;
        Status(int value) {
            this.value = value;
        }
        public int getValue(){
            return this.value;
        }
    }

    public static Object[] insert(TaskDb taskDb) {
        return insertWithUser(taskDb,null);
    }

    public static Object[] insertWithUser(TaskDb taskDb, Long userId) {
        try {
            Connection connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Zadania (nazwa, opis, data_zakonczenia, stan, projekt_id) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, taskDb.getName());//Nazwa
            preparedStatement.setString(2, taskDb.getDesc());//Opis
            preparedStatement.setDate(3,null);//Data zakonczenia
            preparedStatement.setInt(4, taskDb.getStatus());//Stan
            preparedStatement.setLong(5,11);//Projekt id
            preparedStatement.executeUpdate();
            loger.debug("SQL (Insert Zadanie):"+preparedStatement.toString());
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next() && userId!=null) {
                preparedStatement = connection.prepareStatement("INSERT INTO ZadaniaUzytkownicy (uzytkownik_id, zadanie_id) VALUES (?, ?)");
                preparedStatement.setLong(1,userId);
                preparedStatement.setLong(2,rs.getLong(1));
                preparedStatement.executeUpdate();
                loger.debug("SQL(Dodanie użytkownika do zadania):"+preparedStatement.toString());
            }
            connection.commit();
            return new Object[]{true,"Zadanie zostało dodane"};
        } catch (SQLException e) {
            loger.debug("insertWithUser", e);
            return new Object[]{false,"Zadanie nie zostało dodane"};
        }
    }

    public static Object[] remove(Long id) {
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Zadania WHERE zadanie_id= ?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
            return new Object[]{true, "Zadanie zostało usunięte"};
        } catch (SQLException e) {
            loger.debug("remove ", e);
            return new Object[]{false,"Usuwanie zadania nie powiodło się"};
        }
    }

    public static Object[] removeUser(Long id, Long userId) {
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ZadaniaUzytkownicy WHERE uzytkownik_id=? AND zadanie_id=?");
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
            loger.debug("SQL (removeUser):", preparedStatement.toString());
            return new Object[] {true, "Użytkownik usunięty z zadania"};
        } catch (SQLException e) {
            loger.debug("removeUser", e);
            return new Object[]{false,"Usuwanie użytkownika nie powiodło się"};
        }
    }

    public static Object[] updateStatus(Long id,TaskDb.Status status) {
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Zadania SET stan = ? WHERE zadanie_id= ?");
            preparedStatement.setInt(1,status.getValue());
            preparedStatement.setLong(2,id);
            preparedStatement.executeUpdate();
            loger.debug("SQL (updateStatus): "+preparedStatement.toString());
            return new Object[] {true, "Status został zmieniony"};
        } catch (SQLException e) {
            loger.debug("updateStatus", e);
            return new Object[]{false, "Zmiana statusu nie nie powiodła się"};
        }
    }

    public static Object[] updateStatus(Long id, TaskDb.Status status, String comment) {
        try {
            Connection connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Zadania SET stan = ? WHERE zadanie_id= ?");
            preparedStatement.setInt(1, status.getValue());
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
            loger.debug("SQL (updateStatus): "+preparedStatement.toString());
            preparedStatement = connection.prepareStatement("INSERT INTO ZadaniaKomentarze (komentarz, zadanie_id) VALUES (?, ?)");
            preparedStatement.setString(1, comment);
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
            loger.debug("SQL (updateStatus): "+preparedStatement.toString());
            connection.commit();
            return new Object[] {true, "Status został zmieniony a komentarz dodany"};
        } catch (SQLException e) {
            loger.debug("updateStatus", e);
            return new Object[]{false, "Zmiana statusu nie nie powiodła się"};
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
