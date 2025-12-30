package CNC.view.components;

import CNC.controller.Controller;
import CNC.view.Enum.MeasurementUnit;
import CNC.view.MainView;
import CNC.view.dto.PanneauDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class ProjectPanel extends JPanel {
    private final Controller controller;
    private final MainView mainView;
    private final DimensionField widthField;
    private final DimensionField heightField;
    private final DimensionField depthField;

    public ProjectPanel(Controller controller, MainView mainView){
        this.controller = controller;
        this.mainView = mainView;

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(250, 250, 250));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        panel.add(nameLabel);
        panel.add(nameField);

        JLabel widthLabel = new JLabel("Width:");
        JLabel heightLabel = new JLabel("Height:");
        JLabel depthLabel = new JLabel("Depth:");

        widthField = new DimensionField();
        heightField = new DimensionField();
        depthField = new DimensionField();

        panel.add(widthLabel);
        panel.add(widthField);

        panel.add(heightLabel);
        panel.add(heightField);

        panel.add(depthLabel);
        panel.add(depthField);

        JLabel unitLabel = new JLabel("Unit:");
        JComboBox<String> unitSelector = new JComboBox<>(new String[]{MeasurementUnit.METRIC.getABBREVIATION(), MeasurementUnit.IMPERIAL.getABBREVIATION()});

        panel.add(unitLabel);
        panel.add(unitSelector);

        JButton addButton = new JButton("Add");

        addButton.addActionListener(e -> {
            MeasurementUnit unit = MeasurementUnit.getUnitFromAbbreviation((String) Objects.requireNonNull(unitSelector.getSelectedItem()));

            assert unit != null;

            String name = nameField.getText();
            double panelWidth = unit.convertToMetric(widthField.getValue());
            double panelHeight = unit.convertToMetric(heightField.getValue());
            double panelDepth = unit.convertToMetric(depthField.getValue());

            PanneauDTO panelDto = new PanneauDTO(panelWidth, panelHeight, panelDepth);

            createProject(name, panelDto);
        });

        panel.add(new JLabel());
        panel.add(addButton);

        this.add(panel);
    }

    private void createProject(String name, PanneauDTO dto) {
        controller.createProject(name, dto);
        mainView.repaint();
    }
}
