package pl.tarsius.util.pdf;

import java.util.List;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class Table {

    // Table attributes
    private float margin;
    private float height;
    private PDRectangle pageSize;
    private boolean isLandscape;
    private float rowHeight;

    // font attributes
    private PDFont textFont;
    private float fontSize;

    // Content attributes
    private Integer numberOfRows;
    private List<Column> columns;
    private String[][] content;
    private float cellMargin;

    /** Constructs a new Table. */
    public Table() {
    }

    /**
     * Getter for property 'numberOfColumns'.
     *
     * @return Value for property 'numberOfColumns'.
     */
    public Integer getNumberOfColumns() {
        return this.getColumns().size();
    }

    /**
     * Getter for property 'width'.
     *
     * @return Value for property 'width'.
     */
    public float getWidth() {
        float tableWidth = 0f;
        for (Column column : columns) {
            tableWidth += column.getWidth();
        }
        return tableWidth;
    }

    /**
     * Getter for property 'margin'.
     *
     * @return Value for property 'margin'.
     */
    public float getMargin() {
        return margin;
    }

    /**
     * Setter for property 'margin'.
     *
     * @param margin Value to set for property 'margin'.
     */
    public void setMargin(float margin) {
        this.margin = margin;
    }

    /**
     * Getter for property 'pageSize'.
     *
     * @return Value for property 'pageSize'.
     */
    public PDRectangle getPageSize() {
        return pageSize;
    }

    /**
     * Setter for property 'pageSize'.
     *
     * @param pageSize Value to set for property 'pageSize'.
     */
    public void setPageSize(PDRectangle pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Getter for property 'textFont'.
     *
     * @return Value for property 'textFont'.
     */
    public PDFont getTextFont() {
        return textFont;
    }

    /**
     * Setter for property 'textFont'.
     *
     * @param textFont Value to set for property 'textFont'.
     */
    public void setTextFont(PDFont textFont) {
        this.textFont = textFont;
    }

    /**
     * Getter for property 'fontSize'.
     *
     * @return Value for property 'fontSize'.
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Setter for property 'fontSize'.
     *
     * @param fontSize Value to set for property 'fontSize'.
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Getter for property 'columnsNamesAsArray'.
     *
     * @return Value for property 'columnsNamesAsArray'.
     */
    public String[] getColumnsNamesAsArray() {
        String[] columnNames = new String[getNumberOfColumns()];
        for (int i = 0; i < getNumberOfColumns(); i++) {
            columnNames[i] = columns.get(i).getName();
        }
        return columnNames;
    }

    /**
     * Getter for property 'columns'.
     *
     * @return Value for property 'columns'.
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Setter for property 'columns'.
     *
     * @param columns Value to set for property 'columns'.
     */
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Getter for property 'numberOfRows'.
     *
     * @return Value for property 'numberOfRows'.
     */
    public Integer getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * Setter for property 'numberOfRows'.
     *
     * @param numberOfRows Value to set for property 'numberOfRows'.
     */
    public void setNumberOfRows(Integer numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    /**
     * Getter for property 'height'.
     *
     * @return Value for property 'height'.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Setter for property 'height'.
     *
     * @param height Value to set for property 'height'.
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Getter for property 'rowHeight'.
     *
     * @return Value for property 'rowHeight'.
     */
    public float getRowHeight() {
        return rowHeight;
    }

    /**
     * Setter for property 'rowHeight'.
     *
     * @param rowHeight Value to set for property 'rowHeight'.
     */
    public void setRowHeight(float rowHeight) {
        this.rowHeight = rowHeight;
    }

    /**
     * Getter for property 'content'.
     *
     * @return Value for property 'content'.
     */
    public String[][] getContent() {
        return content;
    }

    /**
     * Setter for property 'content'.
     *
     * @param content Value to set for property 'content'.
     */
    public void setContent(String[][] content) {
        this.content = content;
    }

    /**
     * Getter for property 'cellMargin'.
     *
     * @return Value for property 'cellMargin'.
     */
    public float getCellMargin() {
        return cellMargin;
    }

    /**
     * Setter for property 'cellMargin'.
     *
     * @param cellMargin Value to set for property 'cellMargin'.
     */
    public void setCellMargin(float cellMargin) {
        this.cellMargin = cellMargin;
    }

    /**
     * Getter for property 'landscape'.
     *
     * @return Value for property 'landscape'.
     */
    public boolean isLandscape() {
        return isLandscape;
    }

    /**
     * Setter for property 'landscape'.
     *
     * @param isLandscape Value to set for property 'landscape'.
     */
    public void setLandscape(boolean isLandscape) {
        this.isLandscape = isLandscape;
    }
}
