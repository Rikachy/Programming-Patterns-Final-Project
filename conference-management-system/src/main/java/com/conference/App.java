package com.conference;
import com.conference.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseInitializer.initialize();
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/FXML/LoginIN.FXML"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Conference Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}