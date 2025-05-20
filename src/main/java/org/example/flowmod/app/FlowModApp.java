package org.example.flowmod.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FlowModApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(
            new FXMLLoader(getClass().getResource("/layout/MainView.fxml")).load()));
        stage.setTitle("Flow Modifier Designer");
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
