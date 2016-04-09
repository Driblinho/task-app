package pl.tarsius.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.action.LinkAction;
import pl.tarsius.database.Model.User;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/app/start.fxml", title = "TaskApp - Home")
public class HomeController extends BaseController{



    @FXMLApplicationContext private ApplicationContext applicationContext;
    @FXML private Circle profileAvatar;
    @PostConstruct
    public void init() {
        User user = (User) applicationContext.getRegisteredObject("userSession");
        setUserBar(user);



    }


}
