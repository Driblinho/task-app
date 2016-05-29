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
import pl.tarsius.controller.project.ShowProject;
import pl.tarsius.controller.task.EditTaskController;
import pl.tarsius.controller.task.NewTaskController;
import pl.tarsius.controller.task.StatusController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.TaskDb;
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
            container.getChildren().addAll(user, task, edit);
            flowActionHandler.attachLinkEventHandler(edit, EditProject.class);
            flowActionHandler.attachLinkEventHandler(user, AddToProjectController.class);
            flowActionHandler.attachLinkEventHandler(task, NewTaskController.class);

            if(project.isOpen()) {
                container.getChildren().add(end);
                end.setOnAction(
                        event ->  {
                            // TODO: 28.04.16 TaskDb
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
            }

        } else homeAction();



    }

    public void inTask() {
        Button edit = this.stockButton("Edytuj");
        Button remove = this.stockButton("Usuń");
        Button status = this.stockButton("Zmień status");
        Button end = this.stockButton("Zatwierdź");
        flowActionHandler.attachLinkEventHandler(edit, EditTaskController.class);
        //flowActionHandler.attachEventHandler(end, "taskEnd");
        flowActionHandler.attachLinkEventHandler(status, StatusController.class);
        //flowActionHandler.attachEventHandler(remove, "taskRemove");
        User curUser = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        Project project = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
        TaskDb taskDb = (TaskDb) ApplicationContext.getInstance().getRegisteredObject("taskModel");

        end.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Zatwierdź task");
            alert.setHeaderText("Zadanie zostanie zatwierdzone");
            alert.setContentText("Jesteś pewien że chcesz zatwierdzić task?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Object[] msg = TaskDb.updateStatus(taskDb.getId(), TaskDb.Status.END);
                if((boolean) msg[0]) {
                    new Alert(Alert.AlertType.INFORMATION,""+msg[1]).show();
                    try {
                        flowActionHandler.navigate(ShowProject.class);
                    } catch (VetoException e) {
                        e.printStackTrace();
                    } catch (FlowException e) {
                        e.printStackTrace();
                    }
                } else new Alert(Alert.AlertType.ERROR,""+msg[1]).show();
            }
        });

        remove.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Usuń task");
            alert.setHeaderText("Zadanie zostanie usunięte");
            alert.setContentText("Jesteś pewien że chcesz usunąć task?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Object[] msg = TaskDb.remove(taskDb.getId());
                if((boolean) msg[0]) {
                    new Alert(Alert.AlertType.INFORMATION,""+msg[1]).show();
                    try {
                        flowActionHandler.navigate(ShowProject.class);
                    } catch (VetoException e) {
                        e.printStackTrace();
                    } catch (FlowException e) {
                        e.printStackTrace();
                    }
                } else new Alert(Alert.AlertType.ERROR,""+msg[1]).show();
            }
        });


        if((taskDb.getUserId()==curUser.getUzytkownikId() && taskDb.getStatus()!=TaskDb.Status.END.getValue() && taskDb.getStatus()!=TaskDb.Status.FORTEST.getValue()) || curUser.isAdmin()) {
            container.getChildren().add(status);
        }
        if(curUser.isAdmin() || project.getLider()==curUser.getUzytkownikId()) {
            container.getChildren().addAll(edit, end,remove);
        }

    }

    public void inCloseTask() {
        this.inTask();
        container.getChildren().remove(2);
    }




}
