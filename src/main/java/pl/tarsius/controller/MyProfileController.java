package pl.tarsius.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import org.datafx.controller.FXMLController;
import org.datafx.controller.context.ApplicationContext;
import org.datafx.controller.context.FXMLApplicationContext;
import org.datafx.controller.flow.action.LinkAction;
import pl.tarsius.database.Model.User;

import javax.annotation.PostConstruct;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/home/start.fxml", title = "TaskApp - Moje Dane")
public class MyProfileController {

    @FXML private ImageView topBarAvatar;

    @FXML
    @LinkAction(MyProfileController.class)
    private Hyperlink topBarName;

    @FXMLApplicationContext
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        topBarName.setText(((User)applicationContext.getRegisteredObject("userSession")).getEmail());
    }

}
