package pl.tarsius.controller.project;

import io.datafx.controller.FXMLController;
import io.datafx.controller.context.ApplicationContext;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.action.ActionMethod;
import io.datafx.controller.flow.action.ActionTrigger;
import io.datafx.controller.flow.action.BackAction;
import io.datafx.controller.flow.context.ActionHandler;
import io.datafx.controller.flow.context.FlowActionHandler;
import io.datafx.controller.util.VetoException;
import io.datafx.io.DataReader;
import io.datafx.io.JdbcSource;
import io.datafx.io.converter.JdbcConverter;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import pl.tarsius.controller.BaseController;
import pl.tarsius.database.InitializeConnection;
import pl.tarsius.database.Model.Invite;
import pl.tarsius.database.Model.Project;
import pl.tarsius.database.Model.User;
import pl.tarsius.util.gui.DataFxEXceptionHandler;
import pl.tarsius.util.gui.StockButtons;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Kontroler odpowiadający za dodawanie użytkowników do projektu
 * Created by Ireneusz Kuliga on 18.04.16.
 */
@FXMLController(value = "/view/app/addUserToProject.fxml", title = "Dodaj uczestników do projektu - Tarsius")
public class AddToProjectController extends BaseController {

    /**
     * Pole mapujące przycisk FXML odpowiadający za cofanie formularza (Do cofania używana adnotacja DataFX <code>@BackAction</code>)
     */
    @FXML @BackAction private Button cancelUser;

    /**
     * Pole odpowiadające za akcje zapisu formularza
     */
    @FXML @ActionTrigger("addToProject")
    private Button saveUser;

    /**
     * DataFX FlowActionHandler
     */
    @ActionHandler
    private FlowActionHandler flowActionHandler;

    /**
     * Pole na użytkowników których można dodać do projektu
     */
    private ObservableList<TableUserModel> users;
    ObservableList<TableUserModel> selected;

    /**
     * Tabela na użytkowników możliwych do dodania
     */
    @FXML private TableView userList;
    /**
     * Tabela na wybranych do dodania użytkowników
     */
    @FXML private TableView selectedUser;

    /**
     * Kolumna na Imię i Nazwisko użytkownika możliwego do dodania
     */
    private TableColumn name;
    /**
     * Kolumna na adres email użytkownika możliwego do dodania
     */
    private TableColumn email;

    /**
     * Kolumna na Imię i Nazwisko wybranego użytkownika
     */
    private TableColumn nameSelected;
    /**
     *
     * Kolumna na Email wybranego użytkownika
     */
    private TableColumn emailSelected;

    /**
     * Button kopcujący użytkowników do tabeli
     */
    @FXML private Button addSelected;
    /**
     * Button odpowiadający za usunięcie wybranych użytkowników
     */
    @FXML private Button removeSelected;

    /**
     * Stronicowanie
     */
    @FXML private Pagination pagination;

    /**
     * CheckBox określający czy wysłać zaproszenia czy od razu dodać użytkowników do projektu
     */
    @FXML private CheckBox insertUserToProject;

    /**
     * Metoda inicjalizująca kontroler Zaproszeń
     */
    @PostConstruct
    public void init() {
        new StockButtons(operationButtons,flowActionHandler).inProjectButton();

        breadCrumb.setSelectedCrumb(addUserToProject);
        breadCrumb.setOnCrumbAction(crumbActionEventEventHandler());

        InitializeConnection initializeConnection = new InitializeConnection();
        saveUser.setDisable(true);
        user = (User) ApplicationContext.getInstance().getRegisteredObject("userSession");
        if(user.isAdmin() || user.isManager()) insertUserToProject.setDisable(false);

        userList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectedUser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        name = (TableColumn) userList.getColumns().get(0);
        email = (TableColumn) userList.getColumns().get(1);

        users = FXCollections.observableArrayList();

        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        email.setCellValueFactory(new PropertyValueFactory<>("email"));
        userList.setItems(users);

        nameSelected = (TableColumn) selectedUser.getColumns().get(0);
        emailSelected = (TableColumn) selectedUser.getColumns().get(1);
        nameSelected.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailSelected.setCellValueFactory(new PropertyValueFactory<>("email"));


        ObservableSet<TableUserModel> toAdd = FXCollections.observableSet();
        ObservableList<TableUserModel> selected = FXCollections.observableArrayList(toAdd);
        selectedUser.setItems(selected);

        //Listener nasłuchujący zmiany w ObservableSet i modyfikujący ObservableList z wierszami tabel
        toAdd.addListener((SetChangeListener<? super TableUserModel>) change -> {
            if(change.wasAdded())
                selected.add(change.getElementAdded());
            else
                selected.remove(change.getElementRemoved());

            if(toAdd.size()>0)
                Platform.runLater(() -> saveUser.setDisable(false));
            else
                Platform.runLater(() -> saveUser.setDisable(true));
        });

        addSelected.setOnAction(event -> toAdd.addAll(userList.getSelectionModel().getSelectedItems())); //Wybrane wiersze z kolumny są dodawane do wybranych
        removeSelected.setOnAction(event -> toAdd.removeAll(selectedUser.getSelectionModel().getSelectedItems())); //Usuwa wiersze z wybranych


        try {
            new Thread(userListTask(initializeConnection.connect(),0)).start();

            pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    new Thread(userListTask(initializeConnection.connect(),newValue.intValue())).start();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            Connection connection = initializeConnection.connect();
            userBarSearch.setOnKeyPressed(event -> {
                if(event.getCode().equals(KeyCode.ENTER)) {
                    search = userBarSearch.getText().trim();
                    new Thread(userListTask(connection,0)).start();
                }
            });



        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * Metoda zwracająca Task dodający do ObservableList użytkowników do dodania
     * @param connection Połączenie z bazą danych
     * @param page Określa która strona ma zostać ustawiona
     * @return Task
     */
    private Task userListTask(Connection connection,int page) {

        Project projectM = (Project) ApplicationContext.getInstance().getRegisteredObject("projectModel");
        String sql = "select {tpl} from Uzytkownicy u \n" +
                "where uzytkownik_id not in (select uzytkownik_id from ProjektyUzytkownicy where projekt_id="+projectM.getProjekt_id()+") ";
        String countSql = sql.replace("{tpl}", "count(*)");
        int perPage=6;
        try {
            ResultSet rs = connection.createStatement().executeQuery(countSql);
            rs.next();
            int pageCount = (int) Math.ceil(rs.getLong(1)/perPage);
            Platform.runLater(() -> {
                if(pageCount==0)
                    pagination.setVisible(false);
                else {
                    pagination.setVisible(true);
                    pagination.setPageCount(pageCount);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sql= sql.replace("{tpl}", "u.uzytkownik_id,u.imie,u.nazwisko,u.avatar_id,u.email");

        if(search!=null && search.length()>0) sql+= " and u.imie like '%"+search+"%' or u.nazwisko like '%"+search+"%' or u.email like '%"+search+"%'";
        //if(searchProperty!=null && !searchProperty.isEmpty().getValue()) sql+= " and u.imie like '%"+searchProperty.getValue()+"%'";

        sql+= " limit "+page*perPage+","+perPage+"";


        JdbcConverter<User> jdbcConverter = User.jdbcConverter();
        DataReader<User> dataReader = new JdbcSource<>(connection, sql, jdbcConverter);
        Task<ObservableList<TableUserModel>> t = new Task<ObservableList<TableUserModel>>() {
            @Override
            protected ObservableList<TableUserModel> call() throws Exception {
                ObservableList<TableUserModel> observableList = FXCollections.observableArrayList();
                dataReader.forEach(u -> observableList.add(new TableUserModel(u.getUzytkownikId(),u.getImieNazwisko(),u.getEmail())));
                return observableList;
            }
        };

        // TODO: 18.04.16 Implemen fail
        t.setOnSucceeded(event -> {
            if(t.getValue().size()>0)
                users.setAll(t.getValue());
        });
        return t;

    }

    /**
     * Metoda obsługująca akcje dodawania użytkowników do projektu
     * Wyświetla informacyjny oraz gdy użytkownicy zostali poprawnie dodani przekierowuje na stronę projektu
     */
    @ActionMethod("addToProject")
    public void addToProject() throws VetoException, FlowException {
        long pId = (long) ApplicationContext.getInstance().getRegisteredObject("projectId");

        List<Invite> i = new ArrayList<>();
        ObservableList<TableUserModel> s = selectedUser.getItems();
        Object[] ms;
        if(insertUserToProject.isSelected()) {
            HashSet<Long> ul = new HashSet<>();
            s.forEach(tableUserModel -> ul.add(tableUserModel.getId()));
            ms = Project.addUsersToProject(ul,pId);
        } else {
            s.forEach(tableUserModel -> i.add(new Invite(pId, tableUserModel.getId(), 1, tableUserModel.getName())));
            ms = Invite.saveList(i);
        }

        Alert.AlertType a = ((boolean)ms[0])?Alert.AlertType.INFORMATION: Alert.AlertType.ERROR;
        new Alert(a,""+ms[1]).show();
        if((boolean)ms[0]) DataFxEXceptionHandler.navigateQuietly(flowActionHandler,ShowProjectController.class);
    }


    /**
     * Klasa reprezentująca model tabeli dla TableView
     */
    public static class TableUserModel {
        /**
         * Identyfikator użytkownika
         */
        private final ObservableValue<Long> id;
        /**
         * Adres email imię i nazwisko użytkownika
         */
        private final SimpleStringProperty name;
        /**
         * Adres email użytkownika
         */
        private final SimpleStringProperty email;

        /**
         * Konstruktor idealizujący pola klasy
         * @param id
         * @param name
         * @param email
         */
        public TableUserModel(long id, String name, String email) {
            this.id = new SimpleObjectProperty<>(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
        }


        /**
         * Getter for property 'id'.
         *
         * @return Value for property 'id'.
         */
        public Long getId() {
            return this.id.getValue();
        }

        /**
         * Getter for property 'name'.
         *
         * @return Value for property 'name'.
         */
        public String getName() {
            return this.name.get();
        }

        /**
         * Getter for property 'email'.
         *
         * @return Value for property 'email'.
         */
        public String getEmail() {
            return email.get();
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof TableUserModel) && (((TableUserModel) obj).getName()).equals(this.getName());
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return id.getValue().hashCode();
        }
    }

}
