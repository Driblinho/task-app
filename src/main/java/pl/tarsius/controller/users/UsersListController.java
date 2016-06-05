package pl.tarsius.controller.users;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
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
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Ireneusz Kuliga on 20.04.16.
 */
@FXMLController(value = "/view/app/usersList.fxml", title = "Zarządzaj użytkownikami- Tarsius")
public class UsersListController extends BaseController {
    @FXML
    private TableView userTable;

    private ObservableList<Person> data;

    @FXML
    private Pagination pagination;

    private static int PER_PAGE = 10;

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

        data = FXCollections.observableArrayList();

        idCol.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        imieCol.setCellValueFactory(
                new PropertyValueFactory<>("imie"));
        nazwiskoCol.setCellValueFactory(
                new PropertyValueFactory<>("nazwisko"));
        emailCol.setCellValueFactory(
                new PropertyValueFactory<>("email"));

        emailCol.setPrefWidth(200);

        rangaCol.setCellValueFactory(
                new PropertyValueFactory<>("typ"));

        rangaCol.setPrefWidth(150);

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        statusCol.setPrefWidth(100);

        akcjaCol.setCellValueFactory(
                new PropertyValueFactory<>("action"));

        akcjaCol.setSortable(false);

        akcjaCol.setPrefWidth(150);

        userTable.setItems(data);


        InitializeConnection ic = new InitializeConnection();

        try {
            Connection conn = ic.connect();
            Task<List<Person>> tx = updateTable(userBarSearch.getText().trim(), conn, 0);
            new Thread(tx).start();

            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    Task<List<Person>> t = updateTable(userBarSearch.getText().trim(), conn, 0);
                    new Thread(t).start();
                }
            });

            pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                Task<List<Person>> t = updateTable(userBarSearch.getText().trim(), conn, newValue.intValue());
                new Thread(t).start();
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public  Task<List<Person>> updateTable(String search, Connection c, int page) {

        int perPage = PER_PAGE;
        String sql = "select * from Uzytkownicy";
        String sqlCount = sql.replace("*", "count(*)");


        try {

            Statement st = c.createStatement();

            if(search!=null && search.length()>0)
                sql+=" where imie like '%"+search+"%' or nazwisko like '%"+search+"%' or email like '%"+search+"%'";
            sql+= " limit "+page*perPage+","+perPage+"";

            System.out.println(sql);
            DataReader<User> dr = new JdbcSource<>(c, sql, User.jdbcConverter());
            Task<List<Person>> task = new Task<List<Person>>() {
                @Override
                protected List<Person> call() throws Exception {
                    List<Person> tmp = new ArrayList<>();

                    ResultSet rs = st.executeQuery(sqlCount);
                    long count = 0;
                    if(rs.next()) count = rs.getLong(1);
                    int pageCount = (int) Math.ceil(count/perPage);
                    Platform.runLater(() -> pagination.setPageCount(pageCount));
                    dr.forEach(u -> tmp.add(new Person(u.getUzytkownikId(),u.getImie(),u.getNazwisko(),u.getEmail(),u.getTyp(),u.isAktywny(),flowActionHandler,loading)));
                    return tmp;
                }
            };

            task.setOnSucceeded(event -> {
                data.clear();
                data.addAll(task.getValue());
            });

            return task;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Person {

        private final ObservableValue<Long> id;
        private final SimpleStringProperty imie;
        private final SimpleStringProperty nazwisko;
        private final SimpleStringProperty email;
        private final SimpleIntegerProperty typ;
        private final SimpleBooleanProperty status;
        private final SimpleObjectProperty<HBox> action;

        private FlowActionHandler flowActionHandler;
        private StackPane loading;

        public Person(Long id, String imie, String nazwisko, String email, int typ, boolean status, FlowActionHandler flowActionHandler, StackPane loading) {
            this.id = new SimpleObjectProperty<>(id);
            this.imie = new SimpleStringProperty(imie);
            this.nazwisko = new SimpleStringProperty(nazwisko);
            this.email = new SimpleStringProperty(email);
            this.typ = new SimpleIntegerProperty(typ);
            this.status = new SimpleBooleanProperty(status);
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

        public int getTyp() {
            return typ.get();
        }

        public SimpleObjectProperty<Button> typProperty() {
            String r;
            switch (typ.get()) {
                case 1: r="Pracownik"; break;
                case 2: r="Kierownik"; break;
                default: r="Admin";
            }

            Button b = new Button(r);
            b.getStyleClass().addAll("squareButton", "squareButtonRang");
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

        public boolean getStatus() {
            return status.get();
        }

        public SimpleObjectProperty<Button> statusProperty() {
            String r = (status.get())?"Aktywny":"Nieaktywny";
            Button b =new Button(r);

            if(status.get())
                b.getStyleClass().add("greenButton");
            else
                b.getStyleClass().add("redButton");

            b.setOnAction(event -> {
                Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Zmienić status użytkownika?");
                Optional<ButtonType> op = a.showAndWait();
                if(op.get().equals(ButtonType.OK)) {
                    Task<Object[]> task = new Task<Object[]>() {
                        @Override
                        protected Object[] call() throws Exception {
                            int s = (getStatus())?0:1;
                            return UserAuth.updateStatus(s,getId());
                        }
                    };
                    task.setOnRunning(event1 -> loading.setVisible(true));
                    task.setOnSucceeded(event1 -> {
                        loading.setVisible(false);
                        if((boolean)task.getValue()[0]) {
                            DataFxEXceptionHandler.navigateQuietly(flowActionHandler, UsersListController.class);
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
                ApplicationContext.getInstance().register("showUserID", getId());
                DataFxEXceptionHandler.navigateQuietly(flowActionHandler, MyProfileController.class);
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
                        DataFxEXceptionHandler.navigateQuietly(flowActionHandler,UsersListController.class);
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
