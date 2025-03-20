package org.openjfx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import java.net.URL;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import mySql.dataBase.Column;
import mySql.dataBase.Database;
import mySql.dataBase.Table;

import mySql.PDO;

public class FXMLController {
    public PDO pdo = new PDO();

    public final String PATH_DB = "/Users/mir/Documents/database_java/db";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<String> ListDB;

    @FXML
    private Button btnAddField;

    @FXML
    private Button btnClearSql;

    @FXML
    private Button btnCreateNewTable;

    @FXML
    private Button btnDoSql;

    @FXML
    private Button btnRemoveDb;

    @FXML
    private Button btnRemoveTable;

    @FXML
    private Button btnSaveStruct;

    @FXML
    private Button btnWriteDeleteCommand;

    @FXML
    private Button btnWriteDropCommand;

    @FXML
    private Button btnWriteInsertCommand;

    @FXML
    private Button btnWriteSelectCommand;

    @FXML
    private Button createDBBtn;

    @FXML
    private Button exportDBBtn;

    @FXML
    private TextField fieldAddField;

    @FXML
    private TextField fieldNameTable;

    @FXML
    private TextField fieldNewDB;

    @FXML
    private Button importDBBtn;

    @FXML
    private Label lableNameDb;

    @FXML
    private VBox messageBlock;

    @FXML
    private Tab tabOperation;

    @FXML
    private Tab tabSql;

    @FXML
    private Tab tabStruct;

    @FXML
    private Tab tabView;

    @FXML
    private TableView<?> tableStruct;

    @FXML
    private TabPane tabsActions;

    @FXML
    private TabPane tabsTable;

    @FXML
    private TextArea textareaSql;

    @FXML
    private Label viewCommad;

    @FXML
    private TableView<Map<String, Object>> tableView;

    @FXML
    void initialize() {
        pdo.setPathDb(PATH_DB);

        btnCreateNewTable.setVisible(false);
        tabsTable.setVisible(false);
        tabsActions.setVisible(false);

        ListDB.getItems().addAll(pdo.getListDataBase());

        createDBBtn.setOnMouseClicked(event -> {
            String nameNewDB = fieldNewDB.getText();
            if (nameNewDB.isEmpty()) {
                showWarning("Введите имя базы данных!");
                return;
            }

            try {
                pdo.executeSQL("INIT DATABASE " + nameNewDB, nameNewDB);

                ListDB.getItems().addFirst(nameNewDB);
                fieldNewDB.setText("");

                ListDB.getSelectionModel().selectFirst();

                openDbInterface();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (Exception e) {
                showError("Не удалось выполнить запрос. " + e.getMessage());
            }     
        });

        importDBBtn.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл базы данных");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Database file", "*.json")
            );

            File selectedFile = fileChooser.showOpenDialog(importDBBtn.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    pdo.importDB(selectedFile.getAbsolutePath());
                    showNotify("База данных успешно импортирована");
                } catch (IllegalArgumentException e) {
                    showError("Не удалось импортировать. " + e.getMessage());
                } catch (Exception e) {
                    showError("Не удалось импортировать. " + e.getMessage());
                }    
            }

            updateListDB();
            ListDB.getSelectionModel().select(ListDB.getItems().size() - 1);
            
            openDbInterface();
        });

        exportDBBtn.setOnMouseClicked(event -> {
            if (ListDB.getSelectionModel().isEmpty()) {
                showNotify("Выберите базу данных =)");
                return;
            }

            String jsonDb = pdo.exportDb(getSelectDb());

            if (jsonDb == null || jsonDb.isEmpty()) {
                showError("Ошибка экспорта");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить базу данных");
            fileChooser.setInitialFileName(getSelectDb() + ".json");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Database file", "*.json")
            );

            String userHome = System.getProperty("user.home");
            fileChooser.setInitialDirectory(new File(userHome + "/Downloads"));

            Window window = exportDBBtn.getScene().getWindow();
            File file = fileChooser.showSaveDialog(window);

            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(jsonDb);
                    showNotify("Файл успешно сохранен: " + file.getAbsolutePath());
                } catch (IOException e) {
                    showError("Ошибка при сохранении файла: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        ListDB.setOnMouseClicked(event -> {
            if (ListDB.getSelectionModel().isEmpty()) {
                return;
            }

            openDbInterface();
        });

        tabsTable.setOnMouseClicked(event -> {
            updateTabsTable();
            updateTabsActions();
        });
    
        btnCreateNewTable.setOnMouseClicked(event -> {
            if (!tabsTable.getTabs().isEmpty() && tabsTable.getTabs().get(tabsTable.getTabs().size() - 1).getText().equals("?")) {
                return;   
            }

            tabsTable.getTabs().add(new Tab("?"));
            tabsTable.getSelectionModel().select(tabsTable.getTabs().size() - 1);
            updateTabsActions();
        });

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

        btnRemoveDb.setOnMouseClicked(event -> {
            pdo.executeSQL("ERASE DATABASE " + getSelectDb(), getSelectDb());

            ListDB.getItems().remove(ListDB.getSelectionModel().getSelectedItem());
            openDbInterface();
        });

        btnRemoveTable.setOnMouseClicked(event -> {
            pdo.executeSQL("DROP TABLE " + getSelectTable(), getSelectDb());

            tabsTable.getTabs().remove(tabsTable.getSelectionModel().getSelectedItem());
            openDbInterface();
        });
    }

    private String getSelectDb() {
        return ListDB.getSelectionModel().getSelectedItem();
    }

    private String getSelectTable() {
        return tabsTable.getSelectionModel().getSelectedItem().getText();
    }

    private void openDbInterface() {
        btnCreateNewTable.setVisible(true);
        tabsTable.setVisible(true);
        tabsActions.setVisible(true);

        updateTabsTable();
        updateTabsActions();
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
            btnCreateNewTable.setVisible(false);
            tabsTable.setVisible(false);
            tabsActions.setVisible(false);
        }
    }

    private void updateTabsActions() {
        if (tabsTable.getTabs().get(tabsTable.getTabs().size() - 1).getText().equals("?")) {
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
        viewCommad.setText("SELECT * FROM " + getSelectTable());
        clearTableData();
        tableView.setPlaceholder(new Label("Таблица пуста"));
        if (getSelectTable().equals("?")) {
            return;
        }

        pdo.executeSQL("SELECT * FROM " + getSelectTable(), getSelectDb());
        // pdo.executeSQL("INSERT INTO " + getSelectTable() + " ((1, ggwp, 22.02.12), (2, test, 25.05.30))", getSelectDb());
        System.out.println("Выводим в таблицу: " + pdo.getResiltSql());

        Table table = pdo.getTable(getSelectDb(), getSelectTable());

        // table.addColumn(new Column("Имя", "String", List.of()));
        // table.insertData(Map.of("Имя", "Петр", "Возраст", 35, "Зарплата", 70000));

        initializeTableData(table);
    }

    private void loadTabStruct() {
        if (!tabsTable.getTabs().get(tabsTable.getTabs().size() - 1).getText().equals("?")) {
            fieldNameTable.setText(getSelectTable());
        } else {
            fieldNameTable.setText("");
        }
    }

    private void loadTabSql() {
        lableNameDb.setText(getSelectDb());
        textareaSql.setText("");
    }

    private void loadTabOperation() {
        if (!tabsTable.getTabs().get(tabsTable.getTabs().size() - 1).getText().equals("?")) {
            btnRemoveTable.setVisible(true);
        } else {
            btnRemoveTable.setVisible(false);
        }

        if (ListDB.getSelectionModel().isEmpty()) {
            btnRemoveDb.setVisible(false);
        } else {
            btnRemoveDb.setVisible(true);
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

        if (tabsTable.getTabs().size() == 0) {
            tabsTable.getTabs().add(new Tab("?"));
        }
    }

    private void showError(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("error.fxml"));
            AnchorPane newBlock = loader.load();

            Text newText = (Text) newBlock.lookup("#errorBlockText");
            newText.setText(message);

            messageBlock.getChildren().add(newBlock);

            PauseTransition delay = new PauseTransition(Duration.seconds(7));
            delay.setOnFinished(event -> {
                messageBlock.getChildren().remove(newBlock);
            });
            delay.play();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

    private void showWarning(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("warning.fxml"));
            AnchorPane newBlock = loader.load();

            Text newText = (Text) newBlock.lookup("#warningBlockText");
            newText.setText(message);

            messageBlock.getChildren().add(newBlock);

            PauseTransition delay = new PauseTransition(Duration.seconds(5));
            delay.setOnFinished(event -> {
                messageBlock.getChildren().remove(newBlock);
            });
            delay.play();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

    private void showNotify(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("notify.fxml"));
            AnchorPane newBlock = loader.load();

            Text newText = (Text) newBlock.lookup("#notifyBlockText");
            newText.setText(message);

            messageBlock.getChildren().add(newBlock);

            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> {
                messageBlock.getChildren().remove(newBlock);
            });
            delay.play();
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

    private void clearTableData() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }

    public void initializeTableData(Table table) {
        clearTableData();

        if (getSelectTable().equals("?")) return;

        System.out.println("Идёт запись...");

        for (Column column : table.getColumns()) {
            TableColumn<Map<String, Object>, Object> tableColumn = new TableColumn<>(column.getName());

            tableColumn.setCellValueFactory(data -> {
                Map<String, Object> row = data.getValue();
                return new SimpleObjectProperty<>(row.get(column.getName()));
            });

            tableView.getColumns().add(tableColumn);
        }

        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(table.getData());
        tableView.setItems(data);
    }
}
