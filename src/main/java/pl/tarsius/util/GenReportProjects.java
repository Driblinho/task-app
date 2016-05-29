package pl.tarsius.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.ReportProject;
import pl.tarsius.util.pdf.Column;
import pl.tarsius.util.pdf.PDFTableGenerator;
import pl.tarsius.util.pdf.Table;
import pl.tarsius.util.pdf.TableBuilder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ireq on 18.05.16.
 */
public class GenReportProjects implements Runnable {

    private final PDRectangle PAGE_SIZE = PDRectangle.A4;

    //Table configuration
    // Page configuration
    private final float MARGIN = 20;
    private final boolean IS_LANDSCAPE = true;
    // Font configuration
    private final float FONT_SIZE = 10;
    // Table configuration
    private final float ROW_HEIGHT = 15;
    private final float CELL_MARGIN = 2;

    private PDRectangle rect;

    private ReportProject reportProject;
    List<ReportProject> reportProjectsesList;

    public GenReportProjects() {}

    public GenReportProjects(ReportProject reportProject) {
        this.reportProject = reportProject;
    }

    public GenReportProjects(List<ReportProject> reportProjectses) {
        reportProjectsesList = reportProjectses;
    }

    @Override
    public void run() {
        final PDDocument doc = new PDDocument();
        reportProjectsesList.forEach(report -> {
            // Create a simple pie chart
            DefaultPieDataset pieDataset = new DefaultPieDataset();
            pieDataset.setValue("Wykonuje", new Integer(75));
            pieDataset.setValue("Zakończone", new Integer(10));
            pieDataset.setValue("Do sprawdzenia", new Integer(10));

            JFreeChart chart = ChartFactory.createPieChart
                    ("Zestawienia zadań w projekcie", // Title
                            pieDataset, // Dataset
                            true, // Show legend
                            true, // Use tooltips
                            false // Configure chart to generate URLs?
                    );

            final PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelGap(0);
            plot.setInteriorGap(0.0);
            plot.setMaximumLabelWidth(0.30);

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ChartUtilities.writeChartAsJPEG(out,chart,450,600);



                try{

                    PDPage page = new PDPage(PAGE_SIZE);
                    rect = page.getMediaBox();
                    doc.addPage(page);
                    PDPageContentStream content = new PDPageContentStream(doc,page);
                    PDImageXObject img = JPEGFactory.createFromStream(doc, new ByteArrayInputStream(out.toByteArray()));
                    int line = 0;
                    //PDFont font = PDType0Font.load(doc, new File(getClass().getClassLoader().getResource("/assets/font/RobotoCondensed-Regular.ttf").toURI()));
                    PDFont font = PDType0Font.load(doc, new File("/home/ireq/Dev/Java/task-app/src/main/resources/assets/font/RobotoCondensed-Regular.ttf"));

                    content.beginText();
                    content.newLineAtOffset(50, rect.getHeight() - 50*(++line));
                    content.setFont(font,18);
                    content.showText("Raport ogólny");
                    content.endText();



                    content.beginText();
                    content.setFont(font, 16);
                    content.newLineAtOffset(50, rect.getHeight() - 80);
                    content.showText(report.getTitle());
                    content.endText();


                    generateMultiLineText(content,font, report.getDesc());

                    content.beginText();
                    content.setFont(font, 11);
                    content.newLineAtOffset(50, rect.getHeight() - 210);
                    content.showText("Status projektu: ");
                    content.endText();

                    content.beginText();
                    content.setFont(font, 11);
                    content.setNonStrokingColor(Color.GRAY);
                    content.newLineAtOffset(120, rect.getHeight() - 210);
                    content.showText(report.getStatus());
                    content.endText();

                    content.setNonStrokingColor(Color.BLACK);


                    content.beginText();
                    content.setFont(font, 11);
                    content.newLineAtOffset(50, rect.getHeight() - 225);
                    content.showText("W projekcie bierze udział "+report.getUserTab().size()+" osób, koordynatorem jest: "+report.getAuthor());
                    content.endText();

                    content.beginText();
                    content.setFont(font, 11);
                    content.newLineAtOffset(50, rect.getHeight() - 240);

                    String endDate = "";
                    if(report.getEnd()!=null) endDate=" Data zakończenia: "+report.getEnd().toString();

                    content.showText("Data dodania: "+report.getStart().toString()+endDate);
                    content.endText();


                    content.beginText();
                    content.setFont(PDType1Font.TIMES_ITALIC, 12);
                    content.newLineAtOffset(rect.getWidth()-150, rect.getHeight()-20);
                    content.showText("Nazwa aplikacji Ver:01");
                    content.endText();



                    content.close();

                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);

                    content = new PDPageContentStream(doc,page);



                    ArrayList<String[]> ctn = new ArrayList<>();

                    for (int i = 0; i < 5; i++) {
                        ctn.add(new String[]{ "Zadanie", "status", "opis", "Imie naziwsko"});
                    }
                    if(report.getTaskTab()!=null && report.getTaskTab().size()!=0)
                        new PDFTableGenerator().drawTable(doc, taskTable(report.getTaskTab(),font),"Zadania w projekcie");
                    content.drawImage(img,80, rect.getHeight() - 700);

                    if(report.getUserTab()!=null && report.getUserTab().size()!=0)
                        new PDFTableGenerator().drawTable(doc, userTable(report.getUserTab(),font), "Uzytkownicy w projekcie");
                    content.close();



                } catch (Exception io){
                    io.printStackTrace();
                }

            } catch (Exception e) {
                System.out.println("Problem occurred creating chart.");
            }
        });
        try {
            doc.save("tmp.pdf");
            doc.close();
            System.out.println("CLOSE PDF");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





    private void generateMultiLineText(PDPageContentStream content, PDFont font, String text) throws IOException {
        int fontSize = 12;
        float leading = 1.5f * fontSize;
        float margin = 100;
        float width = rect.getWidth() - margin;
        float startX = rect.getLowerLeftX() +50;
        float startY = rect.getUpperRightY() - margin;
        //String text ="Lorem Ipsum jest tekstem stosowanym jako przykładowy wypełniacz w przemyśle poligraficznym. Został po raz pierwszy użyty w XV w. przez nieznanego drukarza do wypełnienia tekstem próbnej książki. Pięć wieków później zaczął być używany przemyśle elektronicznym, pozostając praktycznie niezmienionym. Spopularyzował się w latach 60. XX w. wraz z publikacją arkuszy Letrasetu, zawierających fragmenty Lorem Ipsum, a ostatnio z zawierającym różne wersje Lorem Ipsum oprogramowaniem przeznaczonym do realizacji druków na komputerach osobistych, jak Aldus PageMaker";

        ArrayList<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0)
        {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = fontSize * font.getStringWidth(subString) / 1000;
            //System.out.printf("'%s' - %f of %f\n", subString, size, width);
            if (size > width)
            {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                //System.out.printf("'%s' is line\n", subString);
                lastSpace = -1;
            }
            else if (spaceIndex == text.length())
            {
                lines.add(text);
                //System.out.printf("'%s' is line\n", text);
                text = "";
            }
            else
            {
                lastSpace = spaceIndex;
            }
        }

        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(startX, startY);
        for (String lineT: lines)
        {
            content.showText(lineT);
            content.newLineAtOffset(0, -leading);
        }
        content.endText();
    }


    private Table userTable(List<String[]> userList, PDFont font) {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("Imię", 100));
        columns.add(new Column("Nazwisko", 100));
        columns.add(new Column("Email", 230));
        columns.add(new Column("Zakończone", 100));
        columns.add(new Column("W trakcie", 100));
        columns.add(new Column("Do sprawdzenia", 100));

        String[][] content = new String[1][1];



       content = userList.toArray(content);

        float tableHeight = IS_LANDSCAPE ? PAGE_SIZE.getWidth() - (2 * MARGIN) : PAGE_SIZE.getHeight() - (2 * MARGIN);

        Table table = new TableBuilder()
                .setCellMargin(CELL_MARGIN)
                .setColumns(columns)
                .setContent(content)
                .setHeight(tableHeight)
                .setNumberOfRows(content.length)
                .setRowHeight(ROW_HEIGHT)
                .setMargin(MARGIN)
                .setPageSize(PAGE_SIZE)
                .setLandscape(IS_LANDSCAPE)
                .setTextFont(font)
                .setFontSize(FONT_SIZE)
                .build();
        return table;
    }

    private Table taskTable(List<String[]> userList, PDFont font) {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("Zadanie", 400));
        columns.add(new Column("Status", 100));
        columns.add(new Column("Wykonawca", 300));

        String[][] content = new String[1][1];



        content = userList.toArray(content);

        float tableHeight = IS_LANDSCAPE ? PAGE_SIZE.getWidth() - (2 * MARGIN) : PAGE_SIZE.getHeight() - (2 * MARGIN);

        Table table = new TableBuilder()
                .setCellMargin(CELL_MARGIN)
                .setColumns(columns)
                .setContent(content)
                .setHeight(tableHeight)
                .setNumberOfRows(content.length)
                .setRowHeight(ROW_HEIGHT)
                .setMargin(MARGIN)
                .setPageSize(PAGE_SIZE)
                .setLandscape(IS_LANDSCAPE)
                .setTextFont(font)
                .setFontSize(FONT_SIZE)
                .build();
        return table;
    }

    public ArrayList<ReportProject> genProjects(HashSet<Long> selectedProject){
        ArrayList<ReportProject> reportProjectses = new ArrayList<>();
        try {
            Connection connection = new InitializeConnection().connect();
            String projectsSql = "select p.status,p.projekt_id,nazwa,opis,data_dodania,data_zakonczenia,count(pu.projekt_id),u.imie,u.nazwisko from Projekty p,ProjektyUzytkownicy pu,Uzytkownicy u where p.projekt_id=pu.projekt_id and p.lider=u.uzytkownik_id {selectedproject} \n" +
                    "group by pu.projekt_id";
            if(selectedProject!=null)
                projectsSql = projectsSql.replace("{selectedproject}", " and p.projekt_id in ("+selectedProject.toString().replace("[","").replace("]","")+")");
            else
                projectsSql = projectsSql.replace("{selectedproject}", "");

            System.out.println(projectsSql);

            PreparedStatement ps = connection.prepareStatement(projectsSql);
            ResultSet rs = ps.executeQuery();

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
                String taskSql = "select z.nazwa,z.opis,z.stan,u.imie,u.nazwisko from Zadania z,Uzytkownicy u where projekt_id=? and z.uzytkownik_id=u.uzytkownik_id \n" +
                        "union \n" +
                        "select z.nazwa,z.opis,z.stan,null,null from Zadania z where uzytkownik_id is null and z.projekt_id=?";

                PreparedStatement taskStat = connection.prepareStatement(taskSql);
                taskStat.setLong(1,pid);
                taskStat.setLong(2,pid);
                ResultSet rsTask = taskStat.executeQuery();
                ArrayList<String[]> tasks = new ArrayList<>();
                while (rsTask.next()) {
                    tasks.add(new String[]{
                            rsTask.getString("nazwa"),
                            ""+rsTask.getInt("stan"),
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
                ArrayList<String[]> utmp = new ArrayList<>();
                while (rs2.next()) {
                    utmp.add(new String[]{rs2.getString("imie"), rs2.getString("nazwisko"), rs2.getString("email"),""+rs2.getLong("end"), "" + rs2.getLong("inprogress"), "" + rs2.getLong("fortest")});
                }
                rp.setUserTab(utmp);
                reportProjectses.add(rp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            //loger.debug("CONNECTION ERROR", e);
        } finally {
            return reportProjectses;
        }

    }




}
