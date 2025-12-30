package CNC.view.components;

import CNC.view.Enum.MeasurementUnit;
import CNC.view.MainView;
import javax.swing.*;
import java.awt.*;

public class BottomPanel extends JPanel {
    private final JLabel cursorPositionLabel;
    private final JLabel panelSizeLabel;
    private final JSpinner zoomSpinner;
    private boolean updatingZoom = false; // Pour éviter les boucles de mise à jour

    public BottomPanel(MainView mainView) {
        this.setLayout(new FlowLayout(FlowLayout.LEFT));

        cursorPositionLabel = new JLabel("Cursor: (0, 0)");
        cursorPositionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        this.add(cursorPositionLabel);

        this.add(new JLabel(" | "));

        zoomSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 1));

        zoomSpinner.addChangeListener(e -> {
            if (!updatingZoom) {
                mainView.getCanvasPanel().setZoomPercentage((int) zoomSpinner.getValue());
            }
        });

        this.add(new JLabel("Zoom:"));
        this.add(zoomSpinner);

        this.add(new JLabel("% | "));

        // Label pour la taille du panneau
        panelSizeLabel = new JLabel("Panel Size: 0 x 0");
        panelSizeLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        this.add(panelSizeLabel);
    }

    public void updateCursorPosition(double x, double y, MeasurementUnit unit) {
        double displayX = (unit == MeasurementUnit.METRIC) ? x : MeasurementUnit.METRIC.convertToImperial(x);
        double displayY = (unit == MeasurementUnit.METRIC) ? y : MeasurementUnit.METRIC.convertToImperial(y);

        cursorPositionLabel.setText(String.format("Cursor: (%.2f, %.2f) %s", displayX, displayY, unit.getABBREVIATION()));
    }

    public void updateZoomPercentage(int zoomPercent) {
        updatingZoom = true;
        zoomSpinner.setValue(zoomPercent);
        updatingZoom = false;
    }

    public void updatePanelDimensions(double lengthMetric, double widthMetric, MeasurementUnit unit) {
        double displayLength = (unit == MeasurementUnit.METRIC) ? lengthMetric : MeasurementUnit.METRIC.convertToImperial(lengthMetric);
        double displayWidth = (unit == MeasurementUnit.METRIC) ? widthMetric : MeasurementUnit.METRIC.convertToImperial(widthMetric);

        panelSizeLabel.setText(String.format("Panel Size: %.2f x %.2f %s", displayLength, displayWidth, unit.getABBREVIATION()));
    }
}
