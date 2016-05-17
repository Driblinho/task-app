package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ireq on 17.05.16.
 */
public class ReportProjects {
    private static Logger loger = LoggerFactory.getLogger(ReportProjects.class);

    private ArrayList<Project> projects;

    public ReportProjects(ArrayList<Long> projectsIds) {

    }
    public ReportProjects() {

    }

    public ReportProjects getReport() {
        return null;
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
                            0L,
                            resultSet.getTimestamp("data_dodania"),
                            resultSet.getTimestamp("data_zakonczenia"));
                    p.setLiderImieNazwisko(resultSet.getString("l_imie"),resultSet.getString("l_nazwisko"));
                    p.setStatus(resultSet.getInt("status"));
                    return p;
                } catch (SQLException e) {
                    loger.debug("JDBC CONVERTER: ",e);
                }
                return null;
            }
        };
    }

}
