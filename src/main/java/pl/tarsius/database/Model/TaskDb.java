package pl.tarsius.database.Model;

import com.mysql.jdbc.Statement;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import io.datafx.io.converter.JdbcConverter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Klasa reprezentująca zadanie
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

    /**
     * Konstruktor inicjalizujący
     * @param id ID zadania
     * @param name Nazwa zadania
     * @param desc Opis zadania
     * @param status Status zadania
     * @param projectId ID projektu
     * @param endDate Data zakończenia
     */
    public TaskDb(Long id, String name, String desc, Status status, Long projectId, Date endDate) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.status = status.getValue();
        this.projectId = projectId;
        this.endDate = endDate;
    }

    /**
     * Konstruktor inicjalizujący
     * @param id ID zadania
     * @param name Nazwa zadania
     * @param desc Opis zadania
     * @param status Status zadania
     * @param projectId ID projektu
     * @param endDate Data zakończenia
     * @param userName Nazwa użytkownika
     * @param userId ID użytkownika
     */
    public TaskDb(Long id, String name, String desc, Status status, Long projectId, Date endDate, String userName, Long userId) {
        this(id,name,desc,status,projectId,endDate);
        this.userName=userName;
        this.userId=userId;
    }

    /**
     * Konstruktor inicjalizujący
     * @param name Nazwa zadania
     * @param desc Opis zadania
     * @param status Status zadania
     * @param projectId ID projektu
     * @param endDate Data zakończenia
     */
    public TaskDb(String name, String desc, Status status, Long projectId,Date endDate) {
        this(null, name, desc, status, projectId, endDate);
    }

    /**
     * Enum statusów projektów
     */
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


        /**
         * Getter for property 'value'.
         *
         * @return Value for property 'value'.
         */
        public int getValue(){
            return this.value;
        }
    }

    /**
     * Metoda dodająca nowe zadanie
     * @param taskDb Obiekt {@link TaskDb}
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] insert(TaskDb taskDb) {
        return insertWithUser(taskDb,null);
    }

    public static Object[] insertWithUser(TaskDb taskDb, Long userId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("INSERT INTO Zadania (nazwa, opis, data_zakonczenia, stan, projekt_id, uzytkownik_id) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
        }
    }

    /**
     * Aktualizacja zadania
     * @param taskDb Obiekt {@link TaskDb}
     * @param userId ID użytkownika
     * @param taskId ID zadania
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] updateTask(TaskDb taskDb,Long userId, Long taskId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            preparedStatement = connection.prepareStatement("update Zadania set nazwa=?,opis=?,data_zakonczenia=?,uzytkownik_id=?,stan=? where zadanie_id=?");
            preparedStatement.setString(1, taskDb.getName());//Nazwa
            preparedStatement.setString(2, taskDb.getDesc());//Opis
            if(taskDb.endDate!=null) preparedStatement.setDate(3,taskDb.getEndDate());//Data zakonczenia
            else preparedStatement.setNull(3, Types.DATE); //Date Null
            if(userId!=null) preparedStatement.setLong(4, userId);
            else preparedStatement.setNull(4, Types.BIGINT);
            preparedStatement.setInt(5, taskDb.getStatus());
            preparedStatement.setLong(6, taskId);
            preparedStatement.executeUpdate();
            return new Object[] {true, "Zadanie zostało zaktualizowane"};
        } catch (SQLException e) {
            loger.debug("Update Task", e);
            return new Object[] {false, "Aktualizacja zadania nie powiodła się"};
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
        }
    }

    /**
     * Metoda usuwa zadanie
     * @param id ID zadania
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] remove(Long id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            preparedStatement = connection.prepareStatement("DELETE FROM Zadania WHERE zadanie_id= ?");
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
            return new Object[]{true, "Zadanie zostało usunięte"};
        } catch (SQLException e) {
            loger.debug("remove ", e);
            return new Object[]{false,"Usuwanie zadania nie powiodło się"};
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
        }
    }

    /**
     * Aktualizuje status zadania
     * @param id ID zadania
     * @param status Now status zadania
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] updateStatus(Long id,TaskDb.Status status) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            preparedStatement = connection.prepareStatement("UPDATE Zadania SET stan = ? WHERE zadanie_id= ?");
            preparedStatement.setInt(1,status.getValue());
            preparedStatement.setLong(2,id);
            preparedStatement.executeUpdate();
            loger.debug("SQL (updateStatus): "+preparedStatement.toString());
            return new Object[] {true, "Status został zmieniony"};
        } catch (SQLException e) {
            loger.debug("updateStatus", e);
            return new Object[]{false, "Zmiana statusu nie nie powiodła się"};
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
        }
    }

    /**
     * Metod aktualizująca status
     * @param id ID zadania
     * @param status Nowy status zadania
     * @param comment Komentarz
     * @return Zwraca tablice {@link Object} z logiczną wartością określającą status operacji i wiadomością na temat operacji
     */
    public static Object[] updateStatus(Long id, TaskDb.Status status, String comment) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = new InitializeConnection().connect();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("UPDATE Zadania SET stan = ? WHERE zadanie_id= ?");
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
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,null);
        }
    }

    /**
     * DataFX JDBCConverter dla zadań
     * @return JDBCConverter
     */
    public static JdbcConverter<TaskDb> jdbcConverter() {
        return new JdbcConverter<TaskDb>() {
            @Override
            public TaskDb convertOneRow(ResultSet resultSet) {

                try {
                    String un="";
                    if(resultSet.getString("imie")!=null)
                        un=resultSet.getString("imie")+" "+resultSet.getString("nazwisko");
                    Long uid = (resultSet.getLong("uzytkownik_id")==0)?null:resultSet.getLong("uzytkownik_id");
                    return new TaskDb(
                            resultSet.getLong("zadanie_id"),
                            resultSet.getString("nazwa"),
                            resultSet.getString("opis"),
                            Status.valueOf(resultSet.getInt("stan")),
                            resultSet.getLong("projekt_id"),
                            resultSet.getDate("data_zakonczenia"),
                            un,
                            uid
                    );
                } catch (SQLException e) {
                    loger.debug("JDBCConverter", e);
                    return null;
                }
            }
        };
    }

    /**
     * Pobiera zadanie po ID
     * @param id ID zadania
     * @return TaskDb
     */
    public static TaskDb getById(long id) {
        String sql = "select z.*,u.imie,u.nazwisko from Zadania z,Uzytkownicy u where z.uzytkownik_id=u.uzytkownik_id and z.zadanie_id="+id+"\n" +
                "union \n" +
                "select *,null,null from Zadania where uzytkownik_id is null and zadanie_id="+id;
        Connection connection = null;
        try {
            connection = new InitializeConnection().connect();
            DataReader<TaskDb> dr = new JdbcSource<>(connection, sql, TaskDb.jdbcConverter());
            return dr.get();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'desc'.
     *
     * @return Value for property 'desc'.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Setter for property 'desc'.
     *
     * @param desc Value to set for property 'desc'.
     */
    public void setDesc(String desc) {
        this.desc = desc;
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
     * Getter for property 'projectId'.
     *
     * @return Value for property 'projectId'.
     */
    public Long getProjectId() {
        return projectId;
    }

    /**
     * Setter for property 'projectId'.
     *
     * @param projectId Value to set for property 'projectId'.
     */
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * Getter for property 'endDate'.
     *
     * @return Value for property 'endDate'.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Setter for property 'endDate'.
     *
     * @param endDate Value to set for property 'endDate'.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Getter for property 'userName'.
     *
     * @return Value for property 'userName'.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Setter for property 'userName'.
     *
     * @param userName Value to set for property 'userName'.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Getter for property 'userId'.
     *
     * @return Value for property 'userId'.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Setter for property 'userId'.
     *
     * @param userId Value to set for property 'userId'.
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
