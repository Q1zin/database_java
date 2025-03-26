package org.openjfx.services;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileHandler {
    public File openFileDialog(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database file", "*.json"));
        return fileChooser.showOpenDialog(null);
    }

    public File saveFileDialog(String title, String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.setInitialFileName(fileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database file", "*.json"));
        return fileChooser.showSaveDialog(null);
    }

    public void saveToFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}