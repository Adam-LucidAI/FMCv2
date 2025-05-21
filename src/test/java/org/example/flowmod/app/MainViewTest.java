package org.example.flowmod.app;

import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainViewTest {
    @Test
    public void testTableHasThreeColumns() throws Exception {
        // initialise JavaFX runtime
        new JFXPanel();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/MainView.fxml"));
        Parent root = loader.load();
        BorderPane pane = (BorderPane) root;
        VBox center = (VBox) pane.getCenter();
        TableView<?> table = (TableView<?>) center.getChildren().get(0);
        assertEquals(3, table.getColumns().size());
    }
}
