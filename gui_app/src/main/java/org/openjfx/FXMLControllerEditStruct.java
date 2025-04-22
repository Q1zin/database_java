package org.openjfx;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FXMLControllerEditStruct {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    @FXML
    private CheckBox checkboxNotNull;

    @FXML
    private CheckBox checkboxUnique;

    @FXML
    private TextField fieldName;

    @FXML
    private ComboBox<String> fieldType;

    public void setData(TableRowData rowData) {
        fieldName.setText(rowData.getName());
        fieldType.setValue(rowData.getType());
        checkboxNotNull.setSelected(rowData.isNotNull());
        checkboxUnique.setSelected(rowData.isUnique());
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.hide();
    }

    @FXML
    private void onSave() {
        onCancel();
    }

    @FXML
    void initialize() {
        btnSave.setOnMouseClicked(event -> {
            onSave();
        });

        btnCancel.setOnMouseClicked(event -> {
            onCancel();
        });
    }
}