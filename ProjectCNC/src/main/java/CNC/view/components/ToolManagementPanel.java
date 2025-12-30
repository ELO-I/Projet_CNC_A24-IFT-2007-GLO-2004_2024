package CNC.view.components;

import CNC.controller.Controller;
import CNC.view.Enum.MeasurementUnit;
import CNC.view.MainView;
import CNC.view.dto.CutDTO;
import CNC.view.dto.ToolDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class ToolManagementPanel extends JPanel {
    private final Controller controller;
    private final MainView mainView;

    private final JPanel toolListPanel;
    private final JTextField nameField;
    private final JComboBox<String> unitSelector;
    private final DimensionField dimensionField;

    public ToolManagementPanel(Controller controller, MainView mainView) {
        this.controller = controller;
        this.mainView = mainView;

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(250, 250, 250));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel titleLabel = new JLabel("Tool Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        headerPanel.add(titleLabel);

        toolListPanel = new JPanel();
        toolListPanel.setLayout(new BoxLayout(toolListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(toolListPanel);
        scrollPane.setBorder(null);

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        JPanel addToolPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        addToolPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField();
        addToolPanel.add(nameLabel);
        addToolPanel.add(nameField);

        JLabel widthLabel = new JLabel("Cut Width:");
        dimensionField = new DimensionField();
        addToolPanel.add(widthLabel);
        addToolPanel.add(dimensionField);

        JLabel unitLabel = new JLabel("Unit:");
        unitSelector = new JComboBox<>(new String[]{MeasurementUnit.METRIC.getABBREVIATION(), MeasurementUnit.IMPERIAL.getABBREVIATION()});
        addToolPanel.add(unitLabel);
        addToolPanel.add(unitSelector);

        JButton addButton = new JButton("Add");
        addToolPanel.add(new JLabel()); // Label vide pour l'espacement
        addToolPanel.add(addButton);

        this.add(addToolPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> createTool());

        refreshToolsList();
    }

    private void createTool() {
        // Validation du nom
        String name = nameField.getText().trim();

        if (name.isEmpty() || name.equals(mainView.getNoToolText())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid tool name",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // Validation de la largeur
        double cutWidth = dimensionField.getValue();

        if (cutWidth <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cut width must be greater than 0",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        MeasurementUnit unit = MeasurementUnit.getUnitFromAbbreviation((String) Objects.requireNonNull(unitSelector.getSelectedItem()));

        assert unit != null;
        addTool(new ToolDTO(name, unit.convertToMetric(cutWidth), 0));

        refreshToolsList();
    }

    public void addTool(ToolDTO tool) {
        controller.addTool(tool);
    }

    public void addToolNoSave(ToolDTO tool) {
        controller.addToolNoSave(tool);
    }

    private void deleteTool(ToolDTO tool) {
        controller.deleteTool(tool.storePosition);

        for (CutDTO cut : controller.getCuts()) {
            boolean hasTool = false;

            for (ToolDTO toolDTO : controller.getTools()) {
                if (toolDTO != null && cut.toolName != null) {
                    if (cut.toolName.equals(toolDTO.name)) {
                        hasTool = true;

                        break;
                    }
                }
            }

            if (!hasTool) {
                controller.updateCutNoSaveState(cut);

                mainView.refreshView();
            }
        }

        refreshToolsList();
    }

    public void refreshToolsList() {
        toolListPanel.removeAll();

        for (ToolDTO tool : controller.getTools()) {
            if(tool != null) {
                JPanel toolPanel = getToolPanel(tool);

                toolListPanel.add(toolPanel);
            }
        }

        toolListPanel.repaint();

        nameField.setText("");
        unitSelector.setSelectedItem(mainView.getSelectedUnit().getABBREVIATION());
        dimensionField.setValue(0.0D);

        mainView.refreshTools();
    }

    private JPanel getToolPanel(ToolDTO tool) {
        JPanel toolPanel = new JPanel(new BorderLayout());
        toolPanel.setBorder(BorderFactory.createEtchedBorder());
        toolPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        int index = 0;

        for (int i = 0; i < controller.getTools().length; i++) {
            if (controller.getTools()[i] != null && tool.name.equals(controller.getTools()[i].name)) {
                index = i;

                break;
            }
        }

        JLabel toolLabel = new JLabel(index + ". " + tool.name + " - " + mainView.convertToUnit(tool.cutWidth, mainView.getSelectedUnit()) + " " + mainView.getSelectedUnit().getABBREVIATION());
        toolPanel.add(toolLabel, BorderLayout.CENTER);

        JButton deleteButton = new JButton("X");
        deleteButton.setForeground(Color.RED);

        deleteButton.addActionListener(e -> deleteTool(tool));

        toolPanel.add(deleteButton, BorderLayout.EAST);

        return toolPanel;
    }
}
