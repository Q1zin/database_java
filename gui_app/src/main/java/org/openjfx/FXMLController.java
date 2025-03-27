package org.openjfx;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openjfx.services.FileHandler;
import org.openjfx.services.NotificationManager;

import mySql.PDO;

public class FXMLController {
    private PDO pdo;
    private NotificationManager notificationManager;
    private final FileHandler fileHandler = new FileHandler();
    private static final Logger logger = LogManager.getLogger(FXMLController.class);
    private boolean isProgrammaticChange = false;
    private Stage settingsStage = null;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnAddField, btnClearSql, btnDoSql, btnCreateNewTable, btnRemoveDb,
            btnRemoveTable, btnSaveStruct, btnWriteDeleteCommand, btnWriteDropCommand,
            btnWriteInsertCommand, btnWriteSelectCommand, createDBBtn, exportDBBtn, importDBBtn, btnSettings;

    @FXML
    private TextField fieldAddField, fieldNameTable, fieldNewDB;

    @FXML
    private Label lableNameDb, viewCommad;

    @FXML
    private VBox messageBlock;

    @FXML
    private Tab tabSql, tabStruct, tabView, tabOperation;

    @FXML
    private TableView<?> tableStruct;

    @FXML
    private ListView<String> ListDB;

    @FXML
    private TableView<Map<String, Object>> tableView;

    @FXML
    private TabPane tabsActions, tabsTable;

    @FXML
    private TextArea textareaSql;

    @FXML
    void initialize() {
        pdo = new PDO(fileHandler.getDbPath());
        notificationManager = new NotificationManager(messageBlock);

        closeDbInterface();
        updateListDB();

        tabsActions.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == null || newTab != tabView) { return; }
            if (isProgrammaticChange) {
                isProgrammaticChange = false;
                return;
            }

            loadTabView();
        });

        ListDB.setOnMouseClicked(event -> openDbInterface());
        createDBBtn.setOnMouseClicked(event -> createDatabase());
        importDBBtn.setOnMouseClicked(event -> importDatabase());
        exportDBBtn.setOnMouseClicked(event -> exportDatabase());
        btnSettings.setOnMouseClicked(event -> openSettings());
        tabsTable.setOnMouseClicked(event -> openTableInterface());
        btnCreateNewTable.setOnMouseClicked(event -> createNewTable());
        btnDoSql.setOnMouseClicked(event -> doSql());

        btnWriteSelectCommand.setOnMouseClicked(event -> {
            textareaSql.setText("SELECT * FROM " + getSelectTable());
        });
        btnWriteInsertCommand.setOnMouseClicked(event -> {
            textareaSql.setText("INSERT INTO " + getSelectTable() + " ()");
        });
        btnWriteDropCommand.setOnMouseClicked(event -> {
            textareaSql.setText("DROP TABLE " + getSelectTable());
        });
        btnWriteDeleteCommand.setOnMouseClicked(event -> {
            textareaSql.setText("DELETE FROM " + getSelectTable() + " WHERE");
        });
        btnClearSql.setOnMouseClicked(event -> {
            textareaSql.setText("");
        });

        btnRemoveDb.setOnMouseClicked(event -> removeDb());
        btnRemoveTable.setOnMouseClicked(event -> removeTable());
    }

    private void removeTable() {
        try {
            pdo.executeSQL("DROP TABLE " + getSelectTable(), getSelectDb());

            tabsTable.getTabs().remove(tabsTable.getSelectionModel().getSelectedItem());
            openDbInterface();

            notificationManager.showNotify("Таблица успешно удалена");
        } catch (IllegalArgumentException e) {
            logger.error("Не удалось удалить таблицу {}. Произошла внутренняя ошибка: {}. StackTrace: {}", getSelectTable(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить таблицу " + getSelectTable() + ". Произошла внутренняя ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Не удалось удалить таблицу {}. Произошла внутренняя ошибка (неожиданная ошибка) : {}. StackTrace: {}", getSelectTable(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить таблицу " + getSelectTable() + ". Произошла внутренняя ошибка (неожиданная ошибка): " + e.getMessage());
        }
    }

    private void removeDb() {
        try {
            pdo.executeSQL("ERASE DATABASE " + getSelectDb(), getSelectDb());

            ListDB.getItems().remove(ListDB.getSelectionModel().getSelectedItem());
            openDbInterface();

            notificationManager.showNotify("База данных успешно удалена");
        } catch (IllegalArgumentException e) {
            logger.error("Не удалось удалить базу данных {}. Произошла внутренняя ошибка: {}. StackTrace: {}", getSelectDb(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить базу данных " + getSelectDb() + ". Произошла внутренняя ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Не удалось удалить базу данных {}. Произошла внутренняя ошибка (неожиданная ошибка) : {}. StackTrace: {}", getSelectDb(), e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось удалить базу данных " + getSelectDb() + ". Произошла внутренняя ошибка (неожиданная ошибка): " + e.getMessage());
        }
    }

    private void openDbInterface() {
        if (getSelectDb().isEmpty()) {
            closeDbInterface();
            return;
        }

        btnCreateNewTable.setVisible(true);
        tabsTable.setVisible(true);
        tabsActions.setVisible(true);

        openTableInterface();
    }

    private void closeDbInterface() {
        btnCreateNewTable.setVisible(false);
        tabsTable.setVisible(false);
        tabsActions.setVisible(false);
    }

    private void createDatabase() {
        String nameNewDB = fieldNewDB.getText();

        if (nameNewDB.isEmpty()) {
            notificationManager.showWarning("Введите имя базы данных!");
            return;
        }

        try {
            logger.info("Создаём базу данных: INIT DATABASE {}", nameNewDB);
            pdo.executeSQL("INIT DATABASE " + nameNewDB, nameNewDB);

            ListDB.getItems().addFirst(nameNewDB);
            fieldNewDB.setText("");

            ListDB.getSelectionModel().selectFirst();
            openDbInterface();

            notificationManager.showNotify("База данных создана.");
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при содании базы данных: : {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Ошибка при содании базы данных: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка при содании базы данных (неожиданная ошибка): {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Ошибка при содании базы данных (неожиданная ошибка): " + e.getMessage());
        }
    }

    private void importDatabase() {
        logger.info("Начали импорт базы данных");
        File file = fileHandler.openFileDialog("Выберите файл базы данных");
        if (file != null) {
            try {
                logger.info("Импорт базы данных в PDO: {}", file.getAbsolutePath());
                pdo.importDB(file.getAbsolutePath());
                notificationManager.showNotify("База данных импортирована.");

                updateListDB();
                ListDB.getSelectionModel().selectLast();

                openDbInterface();
            } catch (IllegalArgumentException e) {
                logger.error("Ошибка импорта: : {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка импорта: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Ошибка импорта (неожиданная ошибка): {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка импорта (неожиданная ошибка): " + e.getMessage());
            }
        } else {
            logger.info("Пользователь не выбрал файл для импорта");
        }
    }

    private void exportDatabase() {
        logger.info("Начали экспорт базы данных");
        String dbName = getSelectDb();
        if (dbName.isEmpty()) {
            logger.info("Пользователь не выбрал базу данных для экспорта");
            notificationManager.showWarning("Выберите базу данных.");
            return;
        }

        File file = fileHandler.saveFileDialog("Сохранить базу данных", dbName + ".json");
        if (file != null) {
            try {
                fileHandler.saveToFile(file, pdo.exportDb(dbName));
                notificationManager.showNotify("Файл сохранен.");
            } catch (IOException e) {
                logger.error("Ошибка экспорта: {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка экспорта: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Ошибка экспорта (неожиданная ошибка): {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                notificationManager.showError("Ошибка экспорта (неожиданная ошибка): " + e.getMessage());
            }
        } else {
            logger.info("Пользователь не выбрал файл для экспорта");
        }
    }

    private void openSettings() {
        if (settingsStage != null) {
            settingsStage.toFront();
            settingsStage.requestFocus();
            return;
        }

        try {
            settingsStage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings_scene.fxml"));
            Parent settingsRoot = loader.load();
            settingsStage.setScene(new Scene(settingsRoot));

            settingsStage.setTitle("VovixBD - Настройки");
            settingsStage.setResizable(false);

            FXMLControllerSettings controller = loader.getController();
            controller.updateText(PDO.getPathDb());

            settingsStage.setOnHiding(event -> {
                String selectedPath = controller.getSelectedPath();
                if (selectedPath != null && !selectedPath.isEmpty() && !selectedPath.equals(PDO.getPathDb())) {
                    PDO.setPathDb(selectedPath);
                    File settingsFile = new File(fileHandler.getAppDataDirectory(), "settings.properties");
                    fileHandler.saveDbPath(settingsFile, PDO.getPathDb());
                    closeDbInterface();
                    updateListDB();
                }
                settingsStage = null;
            });

            settingsStage.show();
            controller.initialize();
        } catch (IOException e) {
            logger.error("Ошибка при открытии окна настроек: {}. StackTrace: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Ошибка: Не удалось загрузить окно настроек.");
        }
    }


    private void openTableInterface() {
        updateTabsTable();
        updateTabsActions();
    }

    private void createNewTable() {
        if (!tabsTable.getTabs().isEmpty() && tabsTable.getTabs().getLast().getText().equals("?")) {
            return;
        }

        tabsTable.getTabs().addLast(new Tab("?"));
        tabsTable.getSelectionModel().selectLast();
        updateTabsActions();
    }

    private void doSql() {
        String sql = textareaSql.getText();
        String nameDB = getSelectDb();

        if (sql.isEmpty() || nameDB.isEmpty()) { return; }

        logger.info("Начали выполнение sql запроса пользователя: nameDb: {}, sql: {}", nameDB, sql);

        String command = sql.split(" ")[0];

        try {
            if (command.equals("SELECT")) {
                logger.info("Переходим в обзор и выводим результат (Команда SELECT)");
                if (!loadTabView(sql)) {
                    notificationManager.showError("Не удалось выполнить запрос: Неверный формат команды!");
                    return;
                }
                isProgrammaticChange = true;
                tabsActions.getSelectionModel().select(0);
                textareaSql.setText("");
                notificationManager.showNotify("Запрос успешно выполнен.");
                return;
            }
            pdo.executeSQL(sql, nameDB);

            textareaSql.setText("");
            notificationManager.showNotify("Запрос успешно выполнен.");
            logger.info("Запрос успешно выполнен ({}). Результат {}", sql, PDO.getResultSql());

            switch (command) {
                case "DROP" -> openDbInterface();
                case "ERASE" -> {
                    updateListDB();
                    openDbInterface();
                }
                case "CREATE" -> updateTabsTable();
                case "INIT" -> updateListDB();
            }
        } catch (IllegalArgumentException e) {
            notificationManager.showError("Не удалось выполнить запрос: " + e.getMessage());
            logger.error("Не удалось выполнить запрос {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
        } catch (Exception e) {
            logger.error("Не удалось выполнить запрос (неожиданная ошибка) {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось выполнить запрос. " + e.getMessage());
        }
    }

    private void updateListDB() {
        List<String> dbs = pdo.getListDataBase();
        Set<String> existingDbNames = new HashSet<>(ListDB.getItems());
        List<String> newDb = new ArrayList<>();

        for (String dbName : dbs) {
            if (!existingDbNames.contains(dbName)) {
                newDb.add(dbName);
            }
        }

        ListDB.getItems().addAll(newDb);

        ListDB.getItems().removeIf(db -> !dbs.contains(db));

        if (ListDB.getItems().isEmpty()) {
            closeDbInterface();
        }
    }

    private void updateTabsTable() {
        Set<String> existingTabNames = new HashSet<>();
        for (Tab tab : tabsTable.getTabs()) {
            existingTabNames.add(tab.getText());
        }

        Set<String> tables = pdo.getSetTables(getSelectDb());

        List<Tab> newTabs = new ArrayList<>();
        for (String tableName : tables) {
            if (!existingTabNames.contains(tableName)) {
                newTabs.add(new Tab(tableName));
            }
        }

        tabsTable.getTabs().addAll(newTabs);

        tabsTable.getTabs().removeIf(tab -> !tables.contains(tab.getText()));

        if (tabsTable.getTabs().isEmpty()) {
            tabsTable.getTabs().add(new Tab("?"));
        }
    }

    private void updateTabsActions() {
        if (tabsTable.getTabs().getLast().getText().equals("?")) {
            tabsActions.getSelectionModel().select(1);
        } else {
            tabsActions.getSelectionModel().select(0);
        }

        loadTabView();
        loadTabStruct();
        loadTabSql();
        loadTabOperation();
    }

    private void loadTabView() {
        loadTabView("SELECT * FROM " + getSelectTable());
    }

    private boolean loadTabView(String sql) {
        if (getSelectTable().equals("?")) {
            viewCommad.setText(sql);
            return false;
        }

        try {
            pdo.executeSQL(sql, getSelectDb());

            viewCommad.setText(sql);
            clearTableData();
            tableView.setPlaceholder(new Label("Таблица пуста"));

            showTable(PDO.getResultSql());

            return true;
        } catch (IllegalArgumentException e) {
            logger.error("Не удалось выполнить запрос для отображения данных {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось отобразить данные. Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Не удалось выполнить запрос для отображения данных (неожиданная ошибка) {}: {}. StackTrace: {}", sql, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось отобразить данные, передайте логи администратору. Ошибка: " + e.getMessage());
        }

        return false;
    }

    private void loadTabOperation() {
        btnRemoveTable.setVisible(!tabsTable.getTabs().getLast().getText().equals("?"));
        btnRemoveDb.setVisible(!getSelectDb().isEmpty());
    }

    private void loadTabSql() {
        lableNameDb.setText(getSelectDb());
        textareaSql.setText("");
    }

    private void loadTabStruct() {
        fieldNameTable.setText(tabsTable.getTabs().getLast().getText().equals("?") ? "" : getSelectTable());
    }

    private String getSelectDb() {
        String dbName = ListDB.getSelectionModel().getSelectedItem();
        return (dbName != null) ? dbName : "";
    }

    private String getSelectTable() {
        var tableName = tabsTable.getSelectionModel().getSelectedItem();
        return (tableName != null) ? tableName.getText() : "";
    }

    private void clearTableData() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    private void showTable(String sqlResult) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> items = mapper.readValue(
                    sqlResult,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            ObservableList<Map<String, Object>> itemsList = FXCollections.observableArrayList();
            itemsList.addAll(items);

            tableView.getColumns().clear();

            if (!items.isEmpty()) {
                Map<String, Object> firstRow = items.getFirst();

                for (String key : firstRow.keySet()) {
                    TableColumn<Map<String, Object>, Object> column = new TableColumn<>(key);
                    column.setCellValueFactory(cellData ->
                            new SimpleObjectProperty<>(cellData.getValue().get(key)));
                    tableView.getColumns().add(column);
                }
            }

            tableView.setItems(itemsList);
        } catch (Exception e) {
            logger.error("Не удалось выполнить преобразования для отображения данных {}: {}. StackTrace: {}", sqlResult, e.getMessage(), Arrays.toString(e.getStackTrace()));
            notificationManager.showError("Не удалось выполнить преобразования для отображения данных. Ошибка: " + e.getMessage());
        }
    }
}
