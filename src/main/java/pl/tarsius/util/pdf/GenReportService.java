package pl.tarsius.util.pdf;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
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
import pl.tarsius.database.Model.ReportProject;
import pl.tarsius.database.Model.ReportTasks;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by ireq on 17.05.16.
 */
public class GenReportService extends Service<Void>{

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

    private List<ReportProject> reportProjectsesList;
    private Long userId;
    private HashSet<Long> projectIds = null;

    private boolean isTaskReport;

    public GenReportService(long userId) {
        this.userId = userId;
    }
    public GenReportService(HashSet<Long> projectIds) {
        this.projectIds = projectIds;
    }

    public GenReportService() {}

    /**
     * Getter for property 'taskReport'.
     *
     * @return Value for property 'taskReport'.
     */
    public boolean isTaskReport() {
        return isTaskReport;
    }

    /**
     * Setter for property 'taskReport'.
     *
     * @param taskReport Value to set for property 'taskReport'.
     */
    public void setTaskReport(boolean taskReport) {
        isTaskReport = taskReport;
    }

    private void genProjectReport() {
        final PDDocument doc = new PDDocument();
        reportProjectsesList=(userId!=null)?new ReportProject().genProjects(new ReportProject().getUserProject(userId)):new ReportProject().genProjects(projectIds);
        reportProjectsesList.forEach(report -> {
            // Create a simple pie chart
            DefaultPieDataset pieDataset = new DefaultPieDataset();

            if(report.getNewTask()>0) pieDataset.setValue("Nowe", report.getNewTask());
            if(report.getInProgressTask()>0) pieDataset.setValue("Wykonywane", report.getInProgressTask());
            if(report.getEndTask()>0) pieDataset.setValue("Zakończone", report.getEndTask());
            if(report.getForTestTask()>0) pieDataset.setValue("Do sprawdzenia", report.getForTestTask());

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

                    PDFont font = PDType0Font.load(doc, getClass().getResourceAsStream("/assets/font/RobotoCondensed-Regular.ttf"));

                    content.beginText();
                    content.newLineAtOffset(50, rect.getHeight() - 50*(++line));
                    content.setFont(font,18);
                    content.showText("Raport ogólny");
                    content.endText();

                    //Tytuł projektu
                    content.beginText();
                    content.setFont(font, 16);
                    content.newLineAtOffset(50, rect.getHeight() - 80);
                    content.showText(report.getTitle());
                    content.endText();

                    //Opis projektu
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

                    if(0<report.getTotalTasks()) {
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        content = new PDPageContentStream(doc,page);
                        content.drawImage(img,80, rect.getHeight() - 700);
                    }


                    if(report.getTaskTab()!=null && report.getTaskTab().size()!=0)
                        new PDFTableGenerator().drawTable(doc, taskTable(report.getTaskTab(),font),"Zadania w projekcie");



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

    private void genTaskReport() {
        final PDDocument doc = new PDDocument();

            // Create a simple pie chart
            DefaultPieDataset pieDataset = new DefaultPieDataset();

            ReportTasks report = new ReportTasks().getTaskReport(userId);

            if(report.getInProgressTask()>0) pieDataset.setValue("Wykonywane", report.getInProgressTask());
            if(report.getEndTask()>0) pieDataset.setValue("Zakończone", report.getEndTask());
            if(report.getForTestTask()>0) pieDataset.setValue("Do sprawdzenia", report.getForTestTask());

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

                    PDFont font = PDType0Font.load(doc, getClass().getResourceAsStream("/assets/font/RobotoCondensed-Regular.ttf"));

                    content.beginText();
                    content.newLineAtOffset(50, rect.getHeight() - 50*(++line));
                    content.setFont(font,18);
                    content.showText("Raport z wykonywanych zadań");
                    content.endText();


                    //Tytuł projektu
                    content.beginText();
                    content.setFont(font, 16);
                    content.newLineAtOffset(50, rect.getHeight() - 80);
                    content.showText("Zadania użytkownika:");
                    content.endText();

                    content.beginText();
                    content.setFont(font, 11);
                    content.newLineAtOffset(50, rect.getHeight() - 210);
                    content.showText("Status projektu: ");
                    content.endText();



                    content.beginText();
                    content.setFont(PDType1Font.TIMES_ITALIC, 12);
                    content.newLineAtOffset(rect.getWidth()-150, rect.getHeight()-20);
                    content.showText("Nazwa aplikacji Ver:01");
                    content.endText();
                    content.close();

                    if(0<report.getTaskTab().size()) {
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        content = new PDPageContentStream(doc,page);
                        content.drawImage(img,80, rect.getHeight() - 700);
                        content.close();
                    }

                    if(report.getTaskTab()!=null && report.getTaskTab().size()!=0)
                        new PDFTableGenerator().drawTable(doc, taskTable(report.getTaskTab(),font),"Zadania");






                } catch (Exception io){
                    io.printStackTrace();
                }

            } catch (Exception e) {
                System.out.println("Problem occurred creating chart.");
            }

        try {
            doc.save("tmp.pdf");
            doc.close();
            System.out.println("CLOSE PDF");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Table userTable(List<String[]> userList, PDFont font) {
        List<Column> columns = new ArrayList<>();
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


    private void generateMultiLineText(PDPageContentStream content, PDFont font, String text) throws IOException {
        int fontSize = 12;
        float leading = 1.5f * fontSize;
        float margin = 100;
        float width = rect.getWidth() - margin;
        float startX = rect.getLowerLeftX() +50;
        float startY = rect.getUpperRightY() - margin;
        ArrayList<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0)
        {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = fontSize * font.getStringWidth(subString) / 1000;
            if (size > width)
            {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            }
            else if (spaceIndex == text.length())
            {
                lines.add(text);
                text = "";
            }
            else {
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


    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Raport jest obecnie generowany po zakończeniu zostaniesz o tym poinformowany").show());
                if(isTaskReport())
                    genTaskReport();
                else
                    genProjectReport();
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,"Raport został wygenerowany").show());
                return null;
            }
        };
    }
}
