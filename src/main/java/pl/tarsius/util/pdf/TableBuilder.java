package pl.tarsius.util.pdf;

import java.util.List;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class TableBuilder {

    private Table table = new Table();

    /**
     * Setter for property 'height'.
     *
     * @param height Value to set for property 'height'.
     */
    public TableBuilder setHeight(float height) {
        table.setHeight(height);
        return this;
    }

    /**
     * Setter for property 'numberOfRows'.
     *
     * @param numberOfRows Value to set for property 'numberOfRows'.
     */
    public TableBuilder setNumberOfRows(Integer numberOfRows) {
        table.setNumberOfRows(numberOfRows);
        return this;
    }

    /**
     * Setter for property 'rowHeight'.
     *
     * @param rowHeight Value to set for property 'rowHeight'.
     */
    public TableBuilder setRowHeight(float rowHeight) {
        table.setRowHeight(rowHeight);
        return this;
    }

    /**
     * Setter for property 'content'.
     *
     * @param content Value to set for property 'content'.
     */
    public TableBuilder setContent(String[][] content) {
        table.setContent(content);
        return this;
    }

    /**
     * Setter for property 'columns'.
     *
     * @param columns Value to set for property 'columns'.
     */
    public TableBuilder setColumns(List<Column> columns) {
        table.setColumns(columns);
        return this;
    }

    /**
     * Setter for property 'cellMargin'.
     *
     * @param cellMargin Value to set for property 'cellMargin'.
     */
    public TableBuilder setCellMargin(float cellMargin) {
        table.setCellMargin(cellMargin);
        return this;
    }

    /**
     * Setter for property 'margin'.
     *
     * @param margin Value to set for property 'margin'.
     */
    public TableBuilder setMargin(float margin) {
        table.setMargin(margin);
        return this;
    }

    /**
     * Setter for property 'pageSize'.
     *
     * @param pageSize Value to set for property 'pageSize'.
     */
    public TableBuilder setPageSize(PDRectangle pageSize) {
        table.setPageSize(pageSize);
        return this;
    }

    /**
     * Setter for property 'landscape'.
     *
     * @param landscape Value to set for property 'landscape'.
     */
    public TableBuilder setLandscape(boolean landscape) {
        table.setLandscape(landscape);
        return this;
    }

    /**
     * Setter for property 'textFont'.
     *
     * @param textFont Value to set for property 'textFont'.
     */
    public TableBuilder setTextFont(PDFont textFont) {
        table.setTextFont(textFont);
        return this;
    }

    /**
     * Setter for property 'fontSize'.
     *
     * @param fontSize Value to set for property 'fontSize'.
     */
    public TableBuilder setFontSize(float fontSize) {
        table.setFontSize(fontSize);
        return this;
    }

    /**
     * Metoda budująca obiekt
     * @return Table
     */
    public Table build() {
        return table;
    }
}
