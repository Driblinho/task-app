package pl.tarsius.util.gui;

/**
 * Created by ireq on 04.05.16.
 */
public class MyBread {

    private Class link;
    private String name;

    public MyBread(String name, Class link) {
        this.name = name;
        this.link = link;
    }

    public Class getLink() {
        return link;
    }

    @Override
    public String toString() {
        return name;
    }
}
