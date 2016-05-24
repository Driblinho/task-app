package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ireq on 17.05.16.
 */
public class ReportItem {
    private static Logger loger = LoggerFactory.getLogger(ReportItem.class);
    private Long projectId;
    private String name;
    private String author;
    private long tasks;
    private long users;

    public ReportItem(Long projectId, String name, String author, long tasks, long users) {
        this.projectId=projectId;
        this.name = name;
        this.author = author;
        this.tasks = tasks;
        this.users = users;
    }

    public static JdbcConverter<ReportItem> jdbcConverter() {
        return new JdbcConverter<ReportItem>() {
            @Override
            public ReportItem convertOneRow(ResultSet resultSet) {
                try {
                    return new ReportItem(
                            resultSet.getLong("projekt_id"),
                            resultSet.getString("nazwa"),
                            resultSet.getString("imie")+" "+resultSet.getString("nazwisko"),
                            resultSet.getLong("t_count"),
                            resultSet.getLong("u_count")
                    );
                } catch (SQLException e) {
                    loger.debug("JDBC CONVERTER: ",e);
                }
                return null;
            }
        };
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
     * Getter for property 'author'.
     *
     * @return Value for property 'author'.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Getter for property 'tasks'.
     *
     * @return Value for property 'tasks'.
     */
    public long getTasks() {
        return tasks;
    }

    /**
     * Getter for property 'users'.
     *
     * @return Value for property 'users'.
     */
    public long getUsers() {
        return users;
    }

    /**
     * Getter for property 'projectId'.
     *
     * @return Value for property 'projectId'.
     */
    public Long getProjectId() {
        return projectId;
    }
}
