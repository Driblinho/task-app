package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Created by ireq on 03.05.16.
 */
public class TaskComment {
    private long id;
    private String desc;
    private Timestamp addDate;
    private long tasId;
    private static Logger loger = LoggerFactory.getLogger(TaskComment.class);
    public TaskComment() {
    }

    public TaskComment(long id, String desc, Timestamp addDate, long tasId) {
        this.id = id;
        this.desc = desc;
        this.addDate = addDate;
        this.tasId = tasId;
    }

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
                    // TODO: 21.04.16 LOG
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Timestamp getAddDate() {
        return addDate;
    }

    public void setAddDate(Timestamp addDate) {
        this.addDate = addDate;
    }

    public long getTasId() {
        return tasId;
    }

    public void setTasId(long tasId) {
        this.tasId = tasId;
    }
}
