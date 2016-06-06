package pl.tarsius.util.pdf;

import java.util.List;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Builder Tabeli
 */
public class TableBuilder {

    /**
     * Tabela do zbudowania
     */
    private Table table = new Table();


    /**
     * Ustawia wysokość tabeli
     * @param height wysokość
     * @return referencja do samej siebie
     */
    public TableBuilder setHeight(float height) {
        table.setHeight(height);
        return this;
    }

    /**
     * Ustawia ilość wierszy
     * @param numberOfRows  Ilość wierszy
     * @return referencja do samej siebie
     */
    public TableBuilder setNumberOfRows(Integer numberOfRows) {
        table.setNumberOfRows(numberOfRows);
        return this;
    }

    /**
     * Ustawia wysokość wiersza
     * @param rowHeight wysokość wiersza
     * @return referencja do samej siebie
     */
    public TableBuilder setRowHeight(float rowHeight) {
        table.setRowHeight(rowHeight);
        return this;
    }

    /**
     * Ustawia treść tabeli
     * @param content Tablica {@link String} z treścią tabeli
     * @return referencja do samej siebie
     */
    public TableBuilder setContent(String[][] content) {
        table.setContent(content);
        return this;
    }

    /**
     * Ustawia kolumny
     * @param columns Lista kolumn typu {@link Column}
     * @return referencja do samej siebie
     */
    public TableBuilder setColumns(List<Column> columns) {
        table.setColumns(columns);
        return this;
    }

    /**
     * Ustawia margines komórki
     * @param cellMargin rozmiar marginesu
     * @return referencja do samej siebie
     */
    public TableBuilder setCellMargin(float cellMargin) {
        table.setCellMargin(cellMargin);
        return this;
    }

    /**
     * ustawia marginesy
     * @param margin rozmiar marginesu
     * @return referencja do samej siebie
     */
    public TableBuilder setMargin(float margin) {
        table.setMargin(margin);
        return this;
    }

    /**
     * Ustawia rozmiar strony
     * @param pageSize rozmiar strony {@link PDRectangle}
     * @return referencja do samej siebie
     */
    public TableBuilder setPageSize(PDRectangle pageSize) {
        table.setPageSize(pageSize);
        return this;
    }

    /**
     * Określa tryb Landscape
     * @param landscape Parametr logiczny
     * @return referencja do samej siebie
     */
    public TableBuilder setLandscape(boolean landscape) {
        table.setLandscape(landscape);
        return this;
    }

    /** Ustawia czcionkę
     * @param textFont Czcionka {@link PDFont}
     * @return referencja do samej siebie
     */
    public TableBuilder setTextFont(PDFont textFont) {
        table.setTextFont(textFont);
        return this;
    }

    /** Ustawia rozmiar czcionki
     * @param fontSize rozmiar czcionki
     * @return referencja do samej siebie
     */
    public TableBuilder setFontSize(float fontSize) {
        table.setFontSize(fontSize);
        return this;
    }

    /**
     * Metoda budująca obiekt {@link Table}
     * @return Table
     */
    public Table build() {
        return table;
    }
}
