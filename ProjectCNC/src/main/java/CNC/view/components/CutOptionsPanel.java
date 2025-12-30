package CNC.view.components;

import CNC.controller.Controller;
import CNC.domain.entities.CutType;
import CNC.view.Enum.MeasurementUnit;
import CNC.view.MainView;
import CNC.view.dto.CutDTO;
import CNC.view.dto.ToolDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CutOptionsPanel extends JPanel {
    private final Controller controller;
    private final MainView mainView;
    private final JTextField cutNameField;
    private final JLabel cutTypeLabel;

    private final JComboBox<String> toolSelector;
    private final JComboBox<String> parentSelector;
    private final DimensionField xPositionField;
    private final DimensionField yPositionField;
    private final DimensionField widthField;
    private final DimensionField heightField;

    private final JLabel xPositionFieldSuffix;
    private final JLabel yPositionFieldSuffix;

    private final JLabel widthSuffix;
    private final JLabel heightSuffix;

    private final JLabel validityLabel; // "Valide" ou "Invalide + raison"

    private final String NO_PARENT_TEXT = "Aucune";
    public final String NO_TOOL_TEXT = "Aucun";

    public CutOptionsPanel(Controller controller, MainView mainView) {
        this.controller = controller;
        this.mainView = mainView;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(new Color(250, 250, 250));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Cut Properties");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        this.add(titleLabel);
        this.add(Box.createVerticalStrut(20));

        JLabel cutNameLabel = new JLabel("Cut Name: ");
        cutNameField = new JTextField();
        cutNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        cutTypeLabel = new JLabel();

        toolSelector = new JComboBox<>();
        toolSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        this.add(cutNameLabel);
        this.add(cutNameField);
        this.add(cutTypeLabel);
        this.add(Box.createVerticalStrut(15));
        this.add(toolSelector);

        this.add(new JLabel("Coupe de référence"));

        parentSelector = new JComboBox<>();
        parentSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        this.add(parentSelector);
        this.add(Box.createVerticalStrut(15));

        xPositionField = new DimensionField();
        yPositionField = new DimensionField();
        widthField = new DimensionField();
        heightField = new DimensionField();

        JLabel xPositionFieldPrefix = new JLabel("X ");
        JLabel yPositionFieldPrefix = new JLabel("Y ");

        xPositionFieldSuffix = new JLabel(" " + MeasurementUnit.METRIC.getABBREVIATION());
        yPositionFieldSuffix = new JLabel(" " + MeasurementUnit.METRIC.getABBREVIATION());

        JLabel widthPrefix = new JLabel("Width ");
        JLabel heightPrefix = new JLabel("Height ");

        widthSuffix = new JLabel(" " + MeasurementUnit.METRIC.getABBREVIATION());
        heightSuffix = new JLabel(" " + MeasurementUnit.METRIC.getABBREVIATION());

        xPositionField.add(xPositionFieldPrefix, BorderLayout.WEST);
        xPositionField.add(xPositionFieldSuffix, BorderLayout.EAST);
        yPositionField.add(yPositionFieldPrefix, BorderLayout.WEST);
        yPositionField.add(yPositionFieldSuffix, BorderLayout.EAST);

        widthField.add(widthPrefix, BorderLayout.WEST);
        widthField.add(widthSuffix, BorderLayout.EAST);
        heightField.add(heightPrefix, BorderLayout.WEST);
        heightField.add(heightSuffix, BorderLayout.EAST);

        xPositionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        yPositionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        widthField.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        heightField.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));

        this.add(xPositionField);
        this.add(yPositionField);
        this.add(widthField);
        this.add(heightField);

        this.add(Box.createVerticalGlue());

        validityLabel = new JLabel();

        this.add(validityLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteButton = new JButton("Delete");
        JButton saveButton = new JButton("Save");

        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);

        this.add(buttonPanel);

        saveButton.addActionListener(e -> editCut());
        deleteButton.addActionListener(e -> deleteCut());
    }

    public void refreshToolsList() {
        toolSelector.removeAllItems();
        toolSelector.addItem(NO_TOOL_TEXT);

        for (ToolDTO tool : controller.getTools()) {
            if (tool != null) {
                toolSelector.addItem(tool.name);
            }
        }

        CutDTO selectedCut = mainView.getSelectedCut();

        if (selectedCut != null && selectedCut.toolName != null) {
            toolSelector.setSelectedItem(selectedCut.toolName);
        }
    }

    private void editCut() {
        CutDTO selectedCut = mainView.getSelectedCut();

        if (selectedCut != null) {
            if (toolSelector.getSelectedIndex() == 0) {
                selectedCut.toolName = null;
            }
            else {
                selectedCut.toolName = (String) toolSelector.getSelectedItem();
            }

            if (!cutNameField.getText().isEmpty()) {
                selectedCut.name = cutNameField.getText();
            }

            mainView.getCanvasPanel().setLastUsedTool(mainView.getSelectedCut().toolName);

            if (parentSelector.getSelectedItem() != NO_PARENT_TEXT) {
                CutDTO parent = controller.getCutDTOByName((String) Objects.requireNonNull(parentSelector.getSelectedItem()));

                if (parent.children == null) {
                    parent.children = new ArrayList<>();
                }

                if (!parent.children.contains(selectedCut.id.toString())) {
                    parent.children.add(selectedCut.id.toString());
                }

                controller.updateCut(parent);
            }
            else {
                for (CutDTO cut : controller.getCuts()) {
                    if (cut.children != null && cut.children.contains(selectedCut.id.toString())) {
                        cut.children.remove(selectedCut.id.toString());

                        controller.updateCutNoSaveState(cut);
                    }
                }

                controller.saveState();
            }

            double minX = getMinX(selectedCut.points);
            double minY = getMinY(selectedCut.points);
            double maxX = getMaxX(selectedCut.points);
            double maxY = getMaxY(selectedCut.points);

            double oldWidth = maxX - minX;
            double oldHeight = maxY - minY;

            double newX = xPositionField.getValue() == -1 ? minX : mainView.getSelectedUnit().convertToMetric(xPositionField.getValue());
            double newY = yPositionField.getValue() == -1 ? minY : mainView.getSelectedUnit().convertToMetric(yPositionField.getValue());
            double newWidth = widthField.getValue() == -1 ? oldWidth : mainView.getSelectedUnit().convertToMetric(widthField.getValue());
            double newHeight = heightField.getValue() == -1 ? oldHeight : mainView.getSelectedUnit().convertToMetric(heightField.getValue());

            double dx = minX - newX;
            double dy = minY - newY;
            double scaleX = newWidth / (oldWidth == 0 ? 1 : oldWidth);
            double scaleY = newHeight / (oldHeight == 0 ? 1 : oldHeight);

            // Si oldWidth ou oldHeight = 0, on évite scale bizarre
            if (oldWidth == 0) {
                scaleX = 1;
            }

            if (oldHeight == 0) {
                scaleY = 1;
            }

            transformCutAndChildren(selectedCut, dx, dy, scaleX, scaleY);

            controller.updateCutNoSaveState(selectedCut);
            mainView.selectCut(selectedCut);
        }
    }

    private void transformCutAndChildren(CutDTO cut, double dx, double dy, double scaleX, double scaleY) {
        if (cut.points != null) {
            if (cut.type == CutType.L) {
                if (dx != 0) {
                    double minX = getMinX(cut.points);

                    for (int i = 0; i < cut.points.size(); i++) {
                        if (i != 1) {
                            cut.points.get(i).x = (int) Math.round((cut.points.get(i).x - minX) * scaleX + minX - dx);
                        }
                    }
                }

                if (dy != 0) {
                    double minY = getMinY(cut.points);

                    for (int i = 0; i < cut.points.size(); i++) {
                        if (i != 2) {
                            cut.points.get(i).y = (int) Math.round((cut.points.get(i).y - minY) * scaleY + minY - dy);
                        }
                    }
                }
            }
            else {
                double minX = getMinX(cut.points);
                double minY = getMinY(cut.points);

                for (Point point : cut.points) {
                    point.x = (int) Math.round((point.x - minX) * scaleX + minX - dx);
                    point.y = (int) Math.round((point.y - minY) * scaleY + minY - dy);
                }
            }
        }

        if (cut.children != null) {
            for (String childId : cut.children) {
                CutDTO child = controller.getCutDTOById(UUID.fromString(childId));

                transformCutAndChildren(child, dx, dy, scaleX, scaleY);
                controller.updateCutNoSaveState(child);
            }
        }
    }

    private void deleteCut() {
        if (mainView.getSelectedCut() != null) {
            controller.deleteCut(mainView.getSelectedCut().id);
            mainView.selectCut(null);
        }
    }

    public void updateCutProperties() {
        CutDTO selectedCut = mainView.getSelectedCut();
        parentSelector.removeAllItems();

        if (selectedCut != null) {
            cutNameField.setText(selectedCut.name);
            cutTypeLabel.setText("Cut Type: " + selectedCut.type);

            refreshToolsList();

            parentSelector.removeAllItems();
            parentSelector.addItem(NO_PARENT_TEXT);

            CutDTO parent = null;

            for (CutDTO cut : controller.getCuts()) {
                if (cut.children != null && cut.children.contains(selectedCut.id.toString())) {
                    parent = cut;
                }

                if (!cut.id.equals(selectedCut.id)) {
                    if (!isDescendant(selectedCut, cut)) {
                        parentSelector.addItem(cut.name);
                    }
                }
            }

            if (parent != null) {
                parentSelector.setSelectedItem(parent.name);
            }
            else {
                parentSelector.setSelectedItem(NO_PARENT_TEXT);
            }

            double minX = getMinX(selectedCut.points);
            double minY = getMinY(selectedCut.points);
            double maxX = getMaxX(selectedCut.points);
            double maxY = getMaxY(selectedCut.points);

            double width = maxX - minX;
            double height = maxY - minY;

            xPositionField.setValue(mainView.convertToUnit(minX, mainView.getSelectedUnit()));
            yPositionField.setValue(mainView.convertToUnit(minY, mainView.getSelectedUnit()));
            widthField.setValue(mainView.convertToUnit(width, mainView.getSelectedUnit()));
            heightField.setValue(mainView.convertToUnit(height, mainView.getSelectedUnit()));

            setFieldsVisibility(selectedCut.type);

            validityLabel.setVisible(true);

            if (selectedCut.type != CutType.FORBIDDEN_ZONE) {
                if (!selectedCut.valid) {
                    validityLabel.setText("Invalide (coupe hors panneau ou zone interdite)");
                    validityLabel.setForeground(Color.RED);
                }
                else {
                    validityLabel.setText("Valide");
                    validityLabel.setForeground(Color.GREEN);
                }
            }
            else {
                validityLabel.setVisible(false);
            }
        }

        this.repaint();
    }

    private void setFieldsVisibility(CutType type) {
        // Par défaut, tout caché
        xPositionField.setVisible(false);
        yPositionField.setVisible(false);
        widthField.setVisible(false);
        heightField.setVisible(false);

        switch (type) {
            case VERTICAL -> {
                xPositionField.setVisible(true);
            }
            case HORIZONTAL -> {
                yPositionField.setVisible(true);
            }
            case RECTANGULAR, FORBIDDEN_ZONE, L -> {
                xPositionField.setVisible(true);
                yPositionField.setVisible(true);
                widthField.setVisible(true);
                heightField.setVisible(true);
            }
            case BORDER_CUT -> {
                widthField.setVisible(true);
                heightField.setVisible(true);
            }
        }
    }

    private boolean isDescendant(CutDTO parent, CutDTO target) {
        if (parent.children == null || parent.children.isEmpty()) {
            return false;
        }

        for (String childId : parent.children) {
            CutDTO child = controller.getCutDTOById(UUID.fromString(childId));

            if (child == null) {
                continue;
            }

            if (child.id.equals(target.id) || isDescendant(child, target)) {
                return true;
            }
        }

        return false;
    }
    public DimensionField getXPositionField() {
        return xPositionField;
    }

    public DimensionField getYPositionField() {
        return yPositionField;
    }

    public void setMeasurementUnit(MeasurementUnit unit) {
        xPositionFieldSuffix.setText(" " + unit.getABBREVIATION());
        yPositionFieldSuffix.setText(" " + unit.getABBREVIATION());

        widthSuffix.setText(" " + unit.getABBREVIATION());
        heightSuffix.setText(" " + unit.getABBREVIATION());
    }

    private double getMinX(List<Point> points) {
        double minX = Double.POSITIVE_INFINITY;

        for (Point p : points) {
            if (p.x < minX) {
                minX = p.x;
            }
        }

        return minX == Double.POSITIVE_INFINITY ? 0 : minX;
    }

    private double getMinY(List<Point> points) {
        double minY = Double.POSITIVE_INFINITY;

        for (Point p : points) {
            if (p.y < minY) {
                minY = p.y;
            }
        }

        return minY == Double.POSITIVE_INFINITY ? 0 : minY;
    }

    private double getMaxX(List<Point> points) {
        double maxX = Double.NEGATIVE_INFINITY;

        for (Point p : points) {
            if (p.x > maxX) {
                maxX = p.x;
            }
        }

        return maxX == Double.NEGATIVE_INFINITY ? 0 : maxX;
    }

    private double getMaxY(List<Point> points) {
        double maxY = Double.NEGATIVE_INFINITY;

        for (Point p : points) {
            if (p.y > maxY) {
                maxY = p.y;
            }
        }

        return maxY == Double.NEGATIVE_INFINITY ? 0 : maxY;
    }
}
