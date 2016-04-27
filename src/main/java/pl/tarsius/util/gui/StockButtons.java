package pl.tarsius.util.gui;


import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import pl.tarsius.controller.HomeController;
import pl.tarsius.controller.project.AddToProjectController;
import pl.tarsius.controller.project.EditProject;
import pl.tarsius.controller.project.NewProjectController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by Ireneusz Kuliga on 15.04.16.
 */
public class StockButtons {

    @FXML private VBox container;
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    public StockButtons(VBox container, FlowActionHandler flowActionHandler) {
        this.container = container;
        this.flowActionHandler = flowActionHandler;
    }

    public StockButtons(VBox container) {
        this.container = container;
    }

    private Button stockButton(String name) {
        Button b = new Button(name);
        b.getStyleClass().add("stock-button");
        return b;
    }

    public void homeAction() {
        Button b = this.stockButton("Nowy projekt");
        container.getChildren().add(b);
        flowActionHandler.attachLinkEventHandler(b, NewProjectController.class);
    }

    public void inProjectButton() {
        Button user = this.stockButton("Dodaj uczestnika");
        Button task = this.stockButton("Dodaj zadanie");
        Button edit = this.stockButton("Edytuj projekt");
        Button end = this.stockButton("Zakończ projekt");

        User curUser = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        Project project = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");


        if(curUser.isAdmin() || project.getLider()==curUser.getUzytkownikId()) {
            container.getChildren().addAll(user, task, edit, end);
            flowActionHandler.attachLinkEventHandler(edit, EditProject.class);
            flowActionHandler.attachLinkEventHandler(user, AddToProjectController.class);
            end.setOnAction(
                    event ->  {
                        // TODO: 28.04.16 Task
                        Optional<ButtonType> x = new Alert(Alert.AlertType.CONFIRMATION, "Zakończ Projekt").showAndWait();
                        if(x.get() == ButtonType.OK){
                            try {
                                long pid = (long) ApplicationContext.getInstance().getRegisteredObject("projectId");
                                Connection connection = new InitializeConnection().connect();
                                //connection.setAutoCommit(false);
                                PreparedStatement ps = connection.prepareStatement("UPDATE `Projekty` SET `status` = '0' WHERE `Projekty`.`projekt_id` = ?;");
                                ps.setLong(1, pid);
                                ps.executeUpdate();
                                flowActionHandler.navigate(HomeController.class);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } catch (FlowException e) {
                                e.printStackTrace();
                            } catch (VetoException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } else homeAction();



    }





}
