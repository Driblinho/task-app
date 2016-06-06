package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/** Klasa reprezentująca komentarze do zadań
 * Created by ireq on 03.05.16.
 */
public class TaskComment {
    private long id;
    private String desc;
    private Timestamp addDate;
    private long tasId;
    private static Logger loger = LoggerFactory.getLogger(TaskComment.class);

    /**
     * Domyślny konstruktor
     */
    public TaskComment() {
    }

    /**
     * Konstruktor inicjalizujący
     * @param id ID komarza
     * @param desc Komentarz
     * @param addDate Data dodania
     * @param tasId ID zadania
     */
    public TaskComment(long id, String desc, Timestamp addDate, long tasId) {
        this.id = id;
        this.desc = desc;
        this.addDate = addDate;
        this.tasId = tasId;
    }

    /**
     * DdataFX JDBCConverter
     * @return JDBCConverter
     */
    public static JdbcConverter<TaskComment> jdbcConverter() {
        return new JdbcConverter<TaskComment>() {
            @Override
            public TaskComment convertOneRow(ResultSet resultSet) {

                try {
                    return new TaskComment(
                            resultSet.getLong("zadanie_komentarz_id"),
                            resultSet.getString("komentarz"),
                            resultSet.getTimestamp("data_dodania"),
                            resultSet.getLong("zadanie_id")
                    );
                } catch (SQLException e) {
                    loger.debug("JDbcConverter TaskComment", e);
                    return null;
                }
            }
        };
    }

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public long getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(long id) {
        this.id = id;
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
     * Getter for property 'addDate'.
     *
     * @return Value for property 'addDate'.
     */
    public Timestamp getAddDate() {
        return addDate;
    }

    /**
     * Setter for property 'addDate'.
     *
     * @param addDate Value to set for property 'addDate'.
     */
    public void setAddDate(Timestamp addDate) {
        this.addDate = addDate;
    }

    /**
     * Getter for property 'tasId'.
     *
     * @return Value for property 'tasId'.
     */
    public long getTasId() {
        return tasId;
    }

    /**
     * Setter for property 'tasId'.
     *
     * @param tasId Value to set for property 'tasId'.
     */
    public void setTasId(long tasId) {
        this.tasId = tasId;
    }
}
