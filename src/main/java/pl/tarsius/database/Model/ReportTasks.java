package pl.tarsius.database.Model;

import org.apache.commons.dbutils.DbUtils;
import pl.tarsius.database.InitializeConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ireq on 17.05.16.
 */
public class ReportTasks {
    private String userName;
    private long forTestTask;
    private long inProgressTask;
    private long endTask;
    private List<String[]> taskTab;



    public ReportTasks getTaskReport(long userId) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ReportTasks rt = new ReportTasks();
        try {
            connection = new InitializeConnection().connect();
            ps = connection.prepareStatement("select z.nazwa,z.stan,u.imie,u.nazwisko from Zadania z,Projekty p, Uzytkownicy u  where z.projekt_id=p.projekt_id and z.uzytkownik_id=u.uzytkownik_id and u.uzytkownik_id=? order by stan desc");
            ps.setLong(1,userId);
            rs = ps.executeQuery();
            ArrayList<String[]> tasks = new ArrayList<>();
            while (rs.next()) {
                String stan="";
                switch (rs.getInt("stan")) {
                    case 2:
                        stan="W trakcie";
                        rt.setInProgressTask(rt.getInProgressTask()+1);
                        break;
                    case 3:
                        stan="Do sprawdzenia";
                        rt.setForTestTask(rt.getForTestTask()+1);
                        break;
                    default:
                        stan="Zakończone";
                        rt.setEndTask(rt.getEndTask()+1);
                        break;
                }
                tasks.add(new String[] {
                        rs.getString("nazwa"),
                        stan,
                        rs.getString("imie")+" "+rs.getString("nazwisko")
                });
                rt.setTaskTab(tasks);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(connection,ps,rs);
            return rt;
        }

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
}
