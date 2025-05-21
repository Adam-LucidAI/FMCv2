package org.example.flowmod.app;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.example.flowmod.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.PrintWriter;

public final class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    @FXML private TextField pipeField, flowField, lenField;
    @FXML private ChoiceBox<String> modeChoice;
    @FXML private Button designBtn, exportCsvBtn, exportSvgBtn;
    @FXML private TableView<HoleSpec> table;
    @FXML private TableColumn<HoleSpec, Number> posCol, rowCol, diaCol;
    @FXML private Label reLabel, uniLabel, sheetLabel, statusLabel;

    private final RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(
            new BasicDesignRules(10, java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0)),
            new DefaultDrillSizePolicy(), new FlowPhysics());

    private HoleLayout layout;

    @FXML
    private void initialize() {
        posCol.setCellValueFactory(c -> new SimpleDoubleProperty(
                c.getValue().axialPosMm()));
        rowCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().rowIndex()));
        diaCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().holeDiameterMm()));
        if (modeChoice != null) {
            modeChoice.getSelectionModel().selectFirst();
        }
    }

    private static double parseDoubleField(TextField field) {
        String text = field.getText();
        if (text == null || text.isBlank()) {
            text = field.getPromptText();
        }
        return Double.parseDouble(text);
    }

    /**
     * Display an error message to the user and log it.
     */
    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText("❌ " + message);
        }
        log.error(message);
    }

    @FXML
    private void onDesign() {
        statusLabel.setText("");
        try {
            double id   = parseDoubleField(pipeField);
            double gpm  = parseDoubleField(flowField);
            double len  = parseDoubleField(lenField);

            log.debug("Parsed input: id={} flow={} len={}", id, gpm, len);

            double lps = gpm * 0.0631;   // GPM → L/s
            FlowParameters p = new FlowParameters(id, lps, len);
            log.debug("Constructed parameters: {}", p);

            layout = optimizer.optimize(p);
            log.debug("Optimiser produced {} holes", layout.getHoles().size());

            table.getItems().setAll(layout.getHoles());

            double Re = FlowPhysics.computeReynolds(p);
            reLabel.setText(String.format("Reynolds: %.0f", Re));

            double err = FlowPhysics.computeUniformityError(layout, p);
            uniLabel.setText(String.format("Uniformity: %.2f %%", err));
            if (err > DesignRules.UNIFORMITY_TARGET_PCT) {
                uniLabel.setStyle("-fx-text-fill: red;");
            } else {
                uniLabel.setStyle("-fx-text-fill: -fx-text-base-color;");
            }
            log.debug("Computed uniformity error {}", err);

            double circumference = Math.PI * p.pipeDiameterMm();
            sheetLabel.setText(String.format("Sheet: %.0f mm × %.0f mm",
                    circumference, p.headerLenMm()));
        } catch (DesignNotConvergedException ex) {
            showError(ex.getMessage());
            table.getItems().clear();

        } catch (Throwable t) {
            t.printStackTrace();
            table.getItems().clear();
            statusLabel.setText("Unhandled: " + t.getClass().getSimpleName());

            uniLabel.setText(t.getMessage());
            uniLabel.setStyle("-fx-text-fill: red;");
            log.error("Design failed", t);
            showError(t.getMessage());

        }
    }

    @FXML
    private void onExportCsv() {
        if (layout == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export CSV");
        File file = chooser.showSaveDialog(exportCsvBtn.getScene().getWindow());
        if (file != null) {
            try (PrintWriter out = new PrintWriter(file)) {
                out.println("row,pos_mm,diameter_mm");
                for (HoleSpec h : layout.getHoles()) {
                    out.printf("%d,%.1f,%.1f%n", h.rowIndex(), h.axialPosMm(), h.holeDiameterMm());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void onExportSvg() {
        if (layout == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export 2-D");
        File file = chooser.showSaveDialog(exportSvgBtn.getScene().getWindow());
        if (file != null) {
            try (PrintWriter out = new PrintWriter(file)) {
                out.println("<!-- TODO: generate SVG of header with holes -->");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
