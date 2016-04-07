package pl.tarsius.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import org.datafx.controller.FXMLController;
import org.datafx.controller.flow.action.LinkAction;

/**
 * Created by Ireneusz Kuliga on 02.04.16.
 */
@FXMLController(value = "/view/home/start.fxml", title = "TaskApp - Home")
public class MyProfileController {

    @FXML private ImageView topBarAvatar;

    @FXML
    @LinkAction(MyProfileController.class)
    private Hyperlink topBarName;

}
