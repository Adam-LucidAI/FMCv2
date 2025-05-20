package org.example.flowmod.app;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.flowmod.engine.*;

public final class MainController {

    @FXML private TextField pipeField, flowField, lenField;
    @FXML private TableView<HoleSpec> table;
    @FXML private TableColumn<HoleSpec, Number> rowCol, diaCol;
    @FXML private Label statusLabel;

    private final RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(
            new BasicDesignRules(10, java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0)),
            new DefaultDrillSizePolicy(), new FlowPhysics());

    @FXML
    private void initialize() {
        rowCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().rowIndex()));
        diaCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().holeDiameterMm()));
    }

    @FXML
    private void onDesign() {
        try {
            double id   = Double.parseDouble(pipeField.getText());
            double gpm  = Double.parseDouble(flowField.getText());
            double len  = Double.parseDouble(lenField.getText());

            double lps = gpm * 0.0631;   // GPM → L/s
            FlowParameters p = new FlowParameters(id, lps, len);

            HoleLayout layout = optimizer.optimize(p);

            table.getItems().setAll(layout.getHoles());
            double err = FlowPhysics.computeUniformityError(layout, p);
            statusLabel.setText(String.format("Uniformity: %.2f %% ✓", err));
        } catch (DesignNotConvergedException ex) {
            statusLabel.setText("❌ " + ex.getMessage());
            table.getItems().clear();
        } catch (Exception ex) {
            statusLabel.setText("Input error: " + ex.getMessage());
            table.getItems().clear();
        }
    }
}
