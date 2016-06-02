package pl.tarsius.util.gui;

/** Klasa dodająca opcje linkowania do {@link org.controlsfx.control.BreadCrumbBar }
 * Created by ireq on 04.05.16.
 */
public class MyBread {

    /**
     * Pole określające link
     */
    private Class link;
    /**
     * Nazwa okruszka
     */
    private String name;

    /** Konstruktor idealizujący
     * @param name nazwa okruszka
     * @param link link do kontrolera
     */
    public MyBread(String name, Class link) {
        this.name = name;
        this.link = link;
    }

    /**
     * Getter for property 'link'.
     *
     * @return Value for property 'link'.
     */
    public Class getLink() {
        return link;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }
}
