package pl.tarsius.controller.users;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import pl.tarsius.controller.BaseController;
import pl.tarsius.controller.MyProfileController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.UserAuth;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by Ireneusz Kuliga on 20.04.16.
 */
@FXMLController(value = "/view/app/usersList.fxml", title = "Zarządzaj użytkownikami- Tarsius")
public class UsersListController extends BaseController {
    @FXML
    private TableView userTable;
    @PostConstruct
    public void init() {

        breadCrumb.setSelectedCrumb(useresManagment);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        new StockButtons(operationButtons, flowActionHandler).homeAction();
        TableColumn idCol = new TableColumn("ID");
        TableColumn imieCol = new TableColumn("Imię");
        TableColumn nazwiskoCol = new TableColumn("Nazwisko");
        TableColumn emailCol = new TableColumn("Email");
        TableColumn rangaCol = new TableColumn("Ranga");
        TableColumn statusCol = new TableColumn("Status");
        TableColumn akcjaCol = new TableColumn("Akcja");

        userTable.getColumns().setAll(idCol,imieCol,nazwiskoCol,emailCol,rangaCol,statusCol,akcjaCol);

        final ObservableList<Person> data = FXCollections.observableArrayList();

        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        imieCol.setCellValueFactory(
                new PropertyValueFactory<>("imie"));
        nazwiskoCol.setCellValueFactory(
                new PropertyValueFactory<>("nazwisko"));
        emailCol.setCellValueFactory(
                new PropertyValueFactory<>("email"));
        rangaCol.setCellValueFactory(
                new PropertyValueFactory<>("ranga"));
        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        akcjaCol.setCellValueFactory(
                new PropertyValueFactory<>("action"));
        userTable.setItems(data);


        InitializeConnection ic = new InitializeConnection();

        try {
            Connection conn = ic.connect();
            updateTable(null,conn,data);

            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    updateTable(userBarSearch.getText().trim(),conn,data);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void updateTable(String search, Connection c, ObservableList<Person> data) {

            Connection connection = c;

        try {
            String sql = "select * from Uzytkownicy";
            if(search!=null)
                sql+=" where imie like '%"+search+"%' or nazwisko like '%"+search+"%' or email like '%"+search+"%'";
            System.out.println(sql);
            ResultSet rs =  connection.prepareStatement(sql).executeQuery();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    data.clear();
                    while (rs.next()) {
                        data.add(new Person(rs.getLong("uzytkownik_id"),rs.getString("imie"),rs.getString("nazwisko"),rs.getString("email"),rs.getInt("typ"),rs.getInt("aktywny"),flowActionHandler,loading));
                        Thread.sleep(500);
                    }
                    return null;
                }
            };
            new Thread(task).start();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static class Person {

        private final ObservableValue<Long> id;
        private final SimpleStringProperty imie;
        private final SimpleStringProperty nazwisko;
        private final SimpleStringProperty email;
        private final SimpleIntegerProperty ranga;
        private final SimpleIntegerProperty status;
        private final SimpleObjectProperty<HBox> action;

        private FlowActionHandler flowActionHandler;
        private StackPane loading;

        public Person(Long id, String imie, String nazwisko, String email, int ranga, int status,FlowActionHandler flowActionHandler,StackPane loading) {
            this.id = new SimpleObjectProperty<>(id);
            this.imie = new SimpleStringProperty(imie);
            this.nazwisko = new SimpleStringProperty(nazwisko);
            this.email = new SimpleStringProperty(email);
            this.ranga = new SimpleIntegerProperty(ranga);
            this.status = new SimpleIntegerProperty(status);
            this.action = new SimpleObjectProperty<>(new HBox());
            this.flowActionHandler = flowActionHandler;
            this.loading = loading;
        }



        public Long getId() {
            return id.getValue();
        }

        public ObservableValue<Long> idProperty() {
            return id;
        }

        public String getImie() {
            return imie.get();
        }

        public SimpleStringProperty imieProperty() {
            return imie;
        }

        public String getNazwisko() {
            return nazwisko.get();
        }

        public SimpleStringProperty nazwiskoProperty() {
            return nazwisko;
        }

        public String getEmail() {
            return email.get();
        }

        public SimpleStringProperty emailProperty() {
            return email;
        }

        public int getRanga() {
            return ranga.get();
        }

        public SimpleObjectProperty<Button> rangaProperty() {
            String r;
            switch (ranga.get()) {
                case 1: r="Pracownik"; break;
                case 2: r="Kierownik"; break;
                default: r="Admin";
            }

            Button b = new Button(r);
            b.setOnAction(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Zmiana Rangi");
                alert.setHeaderText("Zmiana rangi użytkownika");
                alert.setContentText("Wybierz dostępne opcje:");
                ButtonType buttonTypeOne = new ButtonType("Pracownik");
                ButtonType buttonTypeTwo = new ButtonType("Kierownik");
                ButtonType buttonTypeThree = new ButtonType("Admin");
                ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeCancel);

                Optional<ButtonType> result = alert.showAndWait();
                int t = 0;
                if (result.get() == buttonTypeOne){
                    t=1;
                } else if (result.get() == buttonTypeTwo) {
                    t=2;
                } else if (result.get() == buttonTypeThree) {
                    System.out.println("Admin");
                    t=3;
                }


                int finalT = t;
                Task<Object[]> task = new Task<Object[]>() {
                    @Override
                    protected Object[] call() throws Exception {
                        return UserAuth.updateTyp(finalT,getId());
                    }
                };
                task.setOnRunning(event1 -> {
                    loading.setVisible(true);
                });
                task.setOnSucceeded(event1 -> {
                    loading.setVisible(false);
                    if((boolean) task.getValue()[0]) {
                        try {
                            flowActionHandler.navigate(UsersListController.class);
                        } catch (VetoException e) {
                            e.printStackTrace();
                        } catch (FlowException e) {
                            e.printStackTrace();
                        }
                    } else new Alert(Alert.AlertType.ERROR,""+task.getValue()[1]).show();
                });
                if(t>0)
                    new Thread(task).start();
            });

            return new SimpleObjectProperty<>(b);
        }

        public int getStatus() {
            return status.get();
        }

        public SimpleObjectProperty<Button> statusProperty() {
            String r = (status.get()==1)?"Aktywny":"Nieaktywny";
            Button b =new Button(r);
            b.setOnAction(event -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Zmienić status użytkownika?");
                Optional<ButtonType> op = a.showAndWait();
                if(op.get().equals(ButtonType.OK)) {
                    Task<Object[]> task = new Task<Object[]>() {
                        @Override
                        protected Object[] call() throws Exception {
                            int s = (getStatus()==1)?0:1;
                            return UserAuth.updateStatus(s,getId());
                        }
                    };
                    task.setOnRunning(event1 -> loading.setVisible(true));
                    task.setOnSucceeded(event1 -> {
                        loading.setVisible(false);
                        if((boolean)task.getValue()[0]) {
                            try {
                                flowActionHandler.navigate(UsersListController.class);
                            } catch (VetoException e) {
                                e.printStackTrace();
                            } catch (FlowException e) {
                                e.printStackTrace();
                            }
                        } else new Alert(Alert.AlertType.ERROR,""+task.getValue()[1]).show();
                    });
                    new Thread(task).start();
                }

            });
            return new SimpleObjectProperty<>(b);
        }

        public HBox getAction() {
            return action.get();
        }

        public SimpleObjectProperty<HBox> actionProperty() {
            Button del = new Button("Usuń");
            Button edytuj = new Button("Edytuj");
            del.getStyleClass().setAll("redButton");
            Insets padd = new Insets(2, 2, 2, 2);
            del.paddingProperty().set(padd);
            edytuj.paddingProperty().set(padd);
            edytuj.getStyleClass().setAll("greenButton");

            edytuj.setOnAction(event -> {
                try {
                    ApplicationContext.getInstance().register("showUserID", getId());
                    flowActionHandler.navigate(MyProfileController.class);
                } catch (VetoException e) {
                    e.printStackTrace();
                } catch (FlowException e) {
                    e.printStackTrace();
                }
            });
            del.setOnAction(event -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Usunąć użytkownika?");
                Task<Object[]> task = new Task<Object[]>() {
                    @Override
                    protected Object[] call() throws Exception {
                        User u = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
                        return UserAuth.deleteUser(getId(), u.getUzytkownikId());
                    }
                };
                task.setOnRunning(event1 -> loading.setVisible(true));
                task.setOnSucceeded(event1 -> {
                    loading.setVisible(false);
                    if((boolean)task.getValue()[0]) {
                        try {
                            flowActionHandler.navigate(UsersListController.class);
                        } catch (VetoException e) {
                            e.printStackTrace();
                        } catch (FlowException e) {
                            e.printStackTrace();
                        }
                    } else {
                        new Alert(Alert.AlertType.ERROR,""+task.getValue()[1]).show();
                    }
                });
                if(a.showAndWait().get().equals(ButtonType.OK)){
                    new Thread(task).start();
                }
            });
            HBox f = new HBox();
            f.spacingProperty().setValue(5);
            f.getChildren().addAll(del,edytuj);
            action.set(f);
            return action;
        }
    }
}
