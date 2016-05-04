package pl.tarsius.database.Model;

import com.mysql.jdbc.Statement;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import io.datafx.io.converter.JdbcConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ireq on 28.04.16.
 */
public class TaskDb {
    private Long id;
    private String name;
    private  String desc;
    private int status;
    private Long projectId;
    private Date endDate;
    private String userName;
    private Long userId;

    private static Logger loger = LoggerFactory.getLogger(TaskDb.class);

    public TaskDb(Long id, String name, String desc, Status status, Long projectId, Date endDate) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.status = status.getValue();
        this.projectId = projectId;
        this.endDate = endDate;
    }

    public TaskDb(Long id, String name, String desc, Status status, Long projectId, Date endDate, String userName, Long userId) {
        this(id,name,desc,status,projectId,endDate);
        this.userName=userName;
        this.userId=userId;
    }

    public TaskDb(String name, String desc, Status status, Long projectId,Date endDate) {
        this(null, name, desc, status, projectId, endDate);
    }

    public enum Status {
        NEW(1),INPROGRES(2),FORTEST(3),END(0);
        private final int value;
        private static Map map = new HashMap<>();
        Status(int value) {
            this.value = value;
        }

        static {
            for (Status status : Status.values()) {
                map.put(status.value, status);
            }
        }

        public static Status valueOf(int status) {
            return (Status) map.get(status);
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
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Zadania (nazwa, opis, data_zakonczenia, stan, projekt_id, uzytkownik_id) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, taskDb.getName());//Nazwa
            preparedStatement.setString(2, taskDb.getDesc());//Opis

            if(taskDb.endDate!=null) preparedStatement.setDate(3,taskDb.getEndDate());//Data zakonczenia
            else preparedStatement.setNull(3, Types.DATE); //Date Null

            preparedStatement.setInt(4, taskDb.getStatus());//Stan
            preparedStatement.setLong(5,taskDb.getProjectId());//Projekt id
            if(userId!=null) preparedStatement.setLong(6, userId);
            else preparedStatement.setNull(6, Types.BIGINT);
            preparedStatement.executeUpdate();
            loger.debug("SQL (Insert Zadanie):"+preparedStatement.toString());
            ResultSet rs = preparedStatement.getGeneratedKeys();
            rs.next();
            connection.commit();
            return new Object[]{true,"Zadanie zostało dodane", rs.getLong(1)};
        } catch (SQLException e) {
            loger.debug("insertWithUser", e);
            return new Object[]{false,"Zadanie nie zostało dodane"};
        }
    }

    public static Object[] updateTask(TaskDb taskDb,Long userId, Long taskId) {
        try {
            Connection connection = new InitializeConnection().connect();
            PreparedStatement preparedStatement = connection.prepareStatement("update Zadania set nazwa=?,opis=?,data_zakonczenia=?,uzytkownik_id=? where zadanie_id=?");
            preparedStatement.setString(1, taskDb.getName());//Nazwa
            preparedStatement.setString(2, taskDb.getDesc());//Opis
            if(taskDb.endDate!=null) preparedStatement.setDate(3,taskDb.getEndDate());//Data zakonczenia
            else preparedStatement.setNull(3, Types.DATE); //Date Null
            if(userId!=null) preparedStatement.setLong(4, userId);
            else preparedStatement.setNull(4, Types.BIGINT);
            preparedStatement.setLong(5, taskId);
            preparedStatement.executeUpdate();
            return new Object[] {true, "Zadanie zostało zaktualizowane"};
        } catch (SQLException e) {
            loger.debug("Update Task", e);
            return new Object[] {false, "Aktualizacja zadania nie powiodła się"};
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

    public static JdbcConverter<TaskDb> jdbcConverter() {
        return new JdbcConverter<TaskDb>() {
            @Override
            public TaskDb convertOneRow(ResultSet resultSet) {

                try {
                    String un="";
                    if(resultSet.getString("imie")!=null)
                        un=resultSet.getString("imie")+" "+resultSet.getString("nazwisko");
                    return new TaskDb(
                            resultSet.getLong("zadanie_id"),
                            resultSet.getString("nazwa"),
                            resultSet.getString("opis"),
                            Status.valueOf(resultSet.getInt("stan")),
                            resultSet.getLong("projekt_id"),
                            resultSet.getDate("data_zakonczenia"),
                            un,
                            resultSet.getLong("uzytkownik_id")
                    );
                } catch (SQLException e) {
                    // TODO: 21.04.16 LOG
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public static TaskDb getById(long id) {
        String sql = "select z.*,u.imie,u.nazwisko from Zadania z,Uzytkownicy u where z.uzytkownik_id=u.uzytkownik_id and z.zadanie_id="+id+"\n" +
                "union \n" +
                "select *,null,null from Zadania where uzytkownik_id is null and zadanie_id="+id;
        try {
            Connection connection = new InitializeConnection().connect();
            DataReader<TaskDb> dr = new JdbcSource<>(connection, sql, TaskDb.jdbcConverter());
            return dr.get();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
