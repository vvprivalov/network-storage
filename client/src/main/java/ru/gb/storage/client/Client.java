package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    public static StartWindowController startController;
    public static MainController mainController;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/StartWindow.fxml"));
        Parent parent = fxmlLoader.load();
        startController = fxmlLoader.getController();
        Scene scene = new Scene(parent, 517, 348);
        stage.setTitle("Сетевое хранилище");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {

        launch();
    }
}
