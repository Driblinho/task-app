package pl.tarsius.database.Model;

import io.datafx.io.converter.JdbcConverter;
import javafx.concurrent.Task;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.tarsius.database.InitializeConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ireq on 17.05.16.
 */
public class ReportProject {
    private static Logger loger = LoggerFactory.getLogger(ReportProject.class);

    private ArrayList<Project> projects;

    private String title;
    private String desc;
    private String author;
    private Timestamp start;
    private Timestamp end;
    private String status;

    private long forTestTask;
    private long inProgressTask;
    private long endTask;
    private long newTask;

    private List<String[]> taskTab;
    private List<String[]> userTab;

    public ReportProject() {}

    public ArrayList<ReportProject> genProjects(HashSet<Long> selectedProject){
        ArrayList<ReportProject> reportProjectses = new ArrayList<>();

        try {
            Connection connection = new InitializeConnection().connect();
            String projectsSql = "select p.status,p.projekt_id,nazwa,opis,data_dodania,data_zakonczenia,count(pu.projekt_id),u.imie,u.nazwisko from Projekty p,ProjektyUzytkownicy pu,Uzytkownicy u where p.projekt_id=pu.projekt_id and p.lider=u.uzytkownik_id {selectedproject} \n" +
                    "group by pu.projekt_id";
            if(selectedProject!=null && selectedProject.size()>0)
                projectsSql = projectsSql.replace("{selectedproject}", " and p.projekt_id in ("+selectedProject.toString().replace("[","").replace("]","")+")");
            else
                projectsSql = projectsSql.replace("{selectedproject}", "");

            System.out.println(projectsSql);

            PreparedStatement ps = connection.prepareStatement(projectsSql);
            ResultSet rs = ps.executeQuery();
            loger.debug(rs.getStatement().toString());
            while (rs.next()) {
                ReportProject rp = new ReportProject();
                rp.setAuthor(rs.getString("imie")+" "+rs.getString("nazwisko"));
                rp.setTitle(rs.getString("nazwa"));
                rp.setDesc(rs.getString("opis"));
                rp.setStart(rs.getTimestamp("data_dodania"));
                rp.setEnd(rs.getTimestamp("data_zakonczenia"));

                int status = rs.getInt("status");

                switch (status) {
                    case 1:
                        rp.setStatus("Aktywny");
                    break;
                    default:
                        rp.setStatus("Zakończony");
                }
                long pid = rs.getLong("projekt_id");
                this.setTaskStat(connection, pid, rp);

                String taskSql = "select z.nazwa,z.opis,z.stan,u.imie,u.nazwisko from Zadania z,Uzytkownicy u where projekt_id=? and z.uzytkownik_id=u.uzytkownik_id \n" +
                        "union \n" +
                        "select z.nazwa,z.opis,z.stan,null,null from Zadania z where uzytkownik_id is null and z.projekt_id=?";
                PreparedStatement taskStat = connection.prepareStatement(taskSql);
                taskStat.setLong(1,pid);
                taskStat.setLong(2,pid);
                ResultSet rsTask = taskStat.executeQuery();
                loger.debug(rsTask.getStatement().toString());
                ArrayList<String[]> tasks = new ArrayList<>();
                while (rsTask.next()) {
                    String stan="";
                    switch (rsTask.getInt("stan")) {
                        case 1:
                            stan="Nowe";
                            break;
                        case 2:
                            stan="W trakcie";
                            break;
                        case 3:
                            stan="Do sprawdzenia";
                            break;
                        default:
                            stan="Zakończone";
                            break;
                    }
                    tasks.add(new String[]{
                            rsTask.getString("nazwa"),
                            stan,
                            rsTask.getString("imie")!=null?rsTask.getString("imie")+" "+rsTask.getString("nazwisko"):"Brak"
                    });
                }
                rp.setTaskTab(tasks);

                String sql = "select u.imie,u.nazwisko,u.email,count(case when stan = 2 then 1 else null end) as inprogress,count(case when stan = 0 then 1 else null end) as end,count(case when stan = 3 then 1 else null end) as fortest from Zadania z,Uzytkownicy u where projekt_id=? and z.uzytkownik_id=u.uzytkownik_id group by imie,nazwisko \n" +
                        "union\n" +
                        "select u.imie,u.nazwisko,u.email,0,0,0 from Projekty p,ProjektyUzytkownicy pu,Uzytkownicy u where p.projekt_id=pu.projekt_id and pu.uzytkownik_id=u.uzytkownik_id and p.projekt_id=? and u.uzytkownik_id not in (select uzytkownik_id from Zadania where projekt_id=? and uzytkownik_id is not null)";
                PreparedStatement ps2 = connection.prepareStatement(sql);

                ps2.setLong(1,pid);
                ps2.setLong(2,pid);
                ps2.setLong(3,pid);
                ResultSet rs2 = ps2.executeQuery();
                loger.debug(rs2.getStatement().toString());
                ArrayList<String[]> utmp = new ArrayList<>();
                while (rs2.next()) {
                    utmp.add(new String[]{rs2.getString("imie"), rs2.getString("nazwisko"), rs2.getString("email"),""+rs2.getLong("end"), "" + rs2.getLong("inprogress"), "" + rs2.getLong("fortest")});
                }
                rp.setUserTab(utmp);
                reportProjectses.add(rp);
            }

        } catch (SQLException e) {
            loger.debug("CONNECTION ERROR", e);
        } finally {
            return reportProjectses;
        }

    }

    private void setTaskStat(Connection connection, long projectId, ReportProject reportProject) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("select stan,count(stan) as count from Zadania where projekt_id =? group by stan");
        ps.setLong(1,projectId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            switch (rs.getInt("stan")) {
                case 1:
                    reportProject.setNewTask(rs.getLong("count"));
                    break;
                case 2:
                    reportProject.setInProgressTask(rs.getLong("count"));
                    break;
                case 3:
                    reportProject.setForTestTask(rs.getLong("count"));
                    break;
                default:
                    reportProject.setEndTask(rs.getLong("count"));
                    break;
            }
        }
        DbUtils.closeQuietly(null,ps,rs);
    }

    public HashSet<Long> getUserProject(long userId) {
        HashSet<Long>  set = new HashSet<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            connection = new InitializeConnection().connect();
            preparedStatement = connection.prepareStatement("select distinct projekt_id from ProjektyUzytkownicy where uzytkownik_id=?");
            preparedStatement.setLong(1, userId);
            rs = preparedStatement.executeQuery();
            loger.debug("getUserProject SQL: "+rs.getStatement().toString());
            while (rs.next())
                set.add(rs.getLong("projekt_id"));
        } catch (SQLException e) {
            loger.debug("Get user project ids", e);
        } finally {
            DbUtils.closeQuietly(connection,preparedStatement,rs);
            return set;
        }
    }
        /**
     * Getter for property 'title'.
     *
     * @return Value for property 'title'.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for property 'title'.
     *
     * @param title Value to set for property 'title'.
     */
    public void setTitle(String title) {
        this.title = title;
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
     * Getter for property 'author'.
     *
     * @return Value for property 'author'.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Setter for property 'author'.
     *
     * @param author Value to set for property 'author'.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Getter for property 'start'.
     *
     * @return Value for property 'start'.
     */
    public Timestamp getStart() {
        return start;
    }

    /**
     * Setter for property 'start'.
     *
     * @param start Value to set for property 'start'.
     */
    public void setStart(Timestamp start) {
        this.start = start;
    }

    /**
     * Getter for property 'end'.
     *
     * @return Value for property 'end'.
     */
    public Timestamp getEnd() {
        return end;
    }

    /**
     * Setter for property 'end'.
     *
     * @param end Value to set for property 'end'.
     */
    public void setEnd(Timestamp end) {
        this.end = end;
    }

    /**
     * Getter for property 'forTestTask'.
     *
     * @return Value for property 'forTestTask'.
     */
    public long getForTestTask() {
        return forTestTask;
    }

    /**
     * Setter for property 'forTestTask'.
     *
     * @param forTestTask Value to set for property 'forTestTask'.
     */
    public void setForTestTask(long forTestTask) {
        this.forTestTask = forTestTask;
    }

    /**
     * Getter for property 'inProgressTask'.
     *
     * @return Value for property 'inProgressTask'.
     */
    public long getInProgressTask() {
        return inProgressTask;
    }

    /**
     * Setter for property 'inProgressTask'.
     *
     * @param inProgressTask Value to set for property 'inProgressTask'.
     */
    public void setInProgressTask(long inProgressTask) {
        this.inProgressTask = inProgressTask;
    }

    /**
     * Getter for property 'endTask'.
     *
     * @return Value for property 'endTask'.
     */
    public long getEndTask() {
        return endTask;
    }

    /**
     * Setter for property 'endTask'.
     *
     * @param endTask Value to set for property 'endTask'.
     */
    public void setEndTask(long endTask) {
        this.endTask = endTask;
    }

    /**
     * Getter for property 'newTask'.
     *
     * @return Value for property 'newTask'.
     */
    public long getNewTask() {
        return newTask;
    }

    /**
     * Setter for property 'newTask'.
     *
     * @param newTask Value to set for property 'newTask'.
     */
    public void setNewTask(long newTask) {
        this.newTask = newTask;
    }

    /**
     * Getter for property 'taskTab'.
     *
     * @return Value for property 'taskTab'.
     */
    public List<String[]> getTaskTab() {
        return taskTab;
    }

    /**
     * Setter for property 'taskTab'.
     *
     * @param taskTab Value to set for property 'taskTab'.
     */
    public void setTaskTab(List<String[]> taskTab) {
        this.taskTab = taskTab;
    }

    /**
     * Getter for property 'userTab'.
     *
     * @return Value for property 'userTab'.
     */
    public List<String[]> getUserTab() {
        return userTab;
    }

    /**
     * Setter for property 'userTab'.
     *
     * @param userTab Value to set for property 'userTab'.
     */
    public void setUserTab(List<String[]> userTab) {
        this.userTab = userTab;
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(String status) {
        this.status = status;
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

    public long getTotalTasks() {return endTask+newTask+inProgressTask+forTestTask;}

}
