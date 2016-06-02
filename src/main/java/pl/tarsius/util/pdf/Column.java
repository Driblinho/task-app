package pl.tarsius.util.pdf;

public class Column {

    private String name;
    private float width;

    public Column(String name, float width) {
        this.name = name;
        this.width = width;
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
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'width'.
     *
     * @return Value for property 'width'.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Setter for property 'width'.
     *
     * @param width Value to set for property 'width'.
     */
    public void setWidth(float width) {
        this.width = width;
    }
}
