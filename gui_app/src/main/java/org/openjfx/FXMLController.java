package org.openjfx;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
    void initialize() {
        pdo.setPathDb(PATH_DB);

        btnCreateNewTable.setVisible(false);
        tabsTable.setVisible(false);
        tabsActions.setVisible(false);

        ListDB.getItems().addAll(getListDataBase());

        createDBBtn.setOnMouseClicked(event -> {
            String nameNewDB = fieldNewDB.getText();
            if (nameNewDB.isEmpty()) {
                System.out.println("Введите имя базы данных!");
                return;
            }

            try {
                pdo.executeSQL("INIT DATABASE " + nameNewDB, nameNewDB);

                ListDB.getItems().addFirst(nameNewDB);
                fieldNewDB.setText("");

                ListDB.getSelectionModel().selectFirst();
            } catch (IllegalArgumentException e) {
                System.out.println("База данных с таким именем уже существует! Поняли чуть позже =)");
            } catch (Exception e) {
                System.out.println("Не удалось записать файл(((((");
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
        });
    }

    private String getSelectDb() {
        return ListDB.getSelectionModel().getSelectedItem();
    }

    private String getSelectTable() {
        return tabsTable.getSelectionModel().getSelectedItem().getText();
    }

    private List<String> getListDataBase() {
    File folderDB = new File(PATH_DB);
        File[] listFileDB = folderDB.listFiles();
        List<String> listDB = new ArrayList<>();

        for (File file : listFileDB) {
            if (file.isFile() && file.getName().endsWith(".json")) {
                String fileNameWithoutExtension = file.getName().substring(0, file.getName().length() - 5);
                listDB.add(fileNameWithoutExtension);
            }
        }
        return listDB;
    }

    private void openDbInterface() {
        btnCreateNewTable.setVisible(true);
        tabsTable.setVisible(true);
        tabsActions.setVisible(true);

        updateTabsTable();
        updateTabsActions();
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

            PauseTransition delay = new PauseTransition(Duration.seconds(5));
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
}
