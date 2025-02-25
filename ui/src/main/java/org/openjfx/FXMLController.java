package org.openjfx;

import java.io.File;
import java.net.URL;
import java.util.*;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class FXMLController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<String> ListDB;

    @FXML
    private Button createDBBtn;

    @FXML
    private Button exportDBBtn;

    @FXML
    private TextField fieldNewDB;

    @FXML
    private Button importDBBtn;

    @FXML
    void initialize() {
        ListDB.getItems().addAll(getDataBase());

        createDBBtn.setOnAction(event -> {
            String nameNewDB = fieldNewDB.getText();
            if (nameNewDB.isEmpty()) {
                System.out.println("Введите имя базы данных!");
                return;
            }

            // валидация имени базы данных
            if (nameNewDB.length() > 20) {
                System.out.println("Имя базы данных не должно превышать 20 символов!");
                return;
            }

            ObservableList<String> list = ListDB.getItems();

            for (var item : list) {
                if (item.equals(nameNewDB)) {
                    System.out.println("База данных с таким именем уже существует!");
                    return;
                }
            }

            list.addFirst(nameNewDB);
            fieldNewDB.setText("");

            ListDB.getSelectionModel().selectFirst();
        });

        ListDB.setOnMouseClicked(event -> {
            String selectedDB = ListDB.getSelectionModel().getSelectedItem();
            // Открывает БД selectedDB
        });
    }

    List<String> getDataBase() {
        File folderDB = new File("/Users/mir/Documents/database_java/ui/db");
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
}
