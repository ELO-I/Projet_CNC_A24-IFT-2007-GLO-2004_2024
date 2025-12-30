package CNC.view;

import CNC.controller.Controller;
import CNC.domain.entities.CutType;
import CNC.view.Enum.MeasurementUnit;
import CNC.view.components.*;
import CNC.view.components.canvas.CanvasPanel;
import CNC.view.dto.CutDTO;
import CNC.view.dto.PanneauDTO;
import CNC.view.dto.ToolDTO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainView extends JFrame {
    private static final String VIEW_TITLE = "CNC Designer";
    private final Dimension DEFAULT_SIZE = new Dimension(1280, 720);
    private final Dimension SIDE_PANEL_SIZE = new Dimension(280, 720);
    private final int TOOLBAR_HEIGHT = 40;
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color ACCENT_COLOR = new Color(70, 130, 180);
    public final String DEFAULT_TOOL_NAME = "Default";
    private final double DEFAULT_TOOL_WIDTH = 12.7D;

    private final Controller controller;

    private final JPanel mainPanel;
    private final CanvasPanel canvasPanel;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final CardLayout leftCardLayout;
    private final CardLayout rightCardLayout;

    private final CutOptionsPanel cutOptionsPanel;
    private final ToolManagementPanel toolManagementPanel;
    private final ProjectPanel projectPanel;
    private final BottomPanel bottomPanel;
    private final JPopupMenu cutTypeMenu;

    private MeasurementUnit selectedUnit;
    private CutDTO selectedCut;

    public MainView() {
        super(VIEW_TITLE);

        this.setSize(DEFAULT_SIZE);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        controller = new Controller();

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        canvasPanel = new CanvasPanel(controller, this);

        leftCardLayout = new CardLayout();
        rightCardLayout = new CardLayout();
        leftPanel = new JPanel(leftCardLayout);
        rightPanel = new JPanel(rightCardLayout);

        leftPanel.setPreferredSize(SIDE_PANEL_SIZE);
        rightPanel.setPreferredSize(SIDE_PANEL_SIZE);

        cutOptionsPanel = new CutOptionsPanel(controller, this);
        selectedUnit = MeasurementUnit.METRIC;

        toolManagementPanel = new ToolManagementPanel(controller, this);
        projectPanel = new ProjectPanel(controller,this);

        bottomPanel = new BottomPanel(this);

        cutTypeMenu = new JPopupMenu();

        addDefaultTool();

        setupPanels();
        setupLayout();
        setupMenuBar();
        initializeToolbar();

        selectCut(null);

        toolManagementPanel.refreshToolsList();

        updateBottomPanelDimensions();
    }

    public void display() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void setupPanels() {
        leftPanel.add(toolManagementPanel, "TOOL_MANAGEMENT");
        leftPanel.add(projectPanel, "CREATE_PANEL");

        rightPanel.add(cutOptionsPanel, "CUT_OPTIONS");
    }

    private void setupLayout() {
        this.setLayout(new BorderLayout());

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(canvasPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        this.add(mainPanel, BorderLayout.CENTER);
    }

    private void initializeToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setPreferredSize(new Dimension(DEFAULT_SIZE.width, TOOLBAR_HEIGHT));
        toolbar.add(createToolbarButton("Panel", e -> leftCardLayout.show(leftPanel, "CREATE_PANEL")));

        toolbar.addSeparator();

        toolbar.add(createToolbarButton("Tools", e -> leftCardLayout.show(leftPanel, "TOOL_MANAGEMENT")));

        JButton newCutButton = createToolbarButton("New Cut", null);
        toolbar.add(newCutButton);

        cutTypeMenu.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton verticalCutButton = new JButton("Vertical");
        JButton horizontalCutButton = new JButton("Horizontal");
        JButton rectangleCutButton = new JButton("Rectangle");
        JButton lShapeCutButton = new JButton("L");
        JButton forbiddenZoneButton = new JButton("Forbidden Zone");
        JButton bordureCutButton = new JButton("Bordure Cut");

        cutTypeMenu.add(verticalCutButton);
        cutTypeMenu.add(horizontalCutButton);
        cutTypeMenu.add(rectangleCutButton);
        cutTypeMenu.add(lShapeCutButton);
        cutTypeMenu.add(forbiddenZoneButton);
        cutTypeMenu.add(bordureCutButton);

        verticalCutButton.addActionListener(e -> selectCutType(CutType.VERTICAL));
        horizontalCutButton.addActionListener(e -> selectCutType(CutType.HORIZONTAL));
        rectangleCutButton.addActionListener(e -> selectCutType(CutType.RECTANGULAR));
        lShapeCutButton.addActionListener(e -> selectCutType(CutType.L));
        forbiddenZoneButton.addActionListener(e -> selectCutType(CutType.FORBIDDEN_ZONE));
        bordureCutButton.addActionListener(e -> selectCutType(CutType.BORDER_CUT));

        newCutButton.addActionListener(e -> cutTypeMenu.show(newCutButton, 0, newCutButton.getHeight()));

        toolbar.addSeparator();

        toolbar.add(createToolbarButton("Undo", e -> {
            controller.undo();
            selectCut(selectedCut);
        }));

        toolbar.add(createToolbarButton("Redo", e -> {
            controller.redo();
            selectCut(selectedCut);
        }));

        toolbar.add(Box.createHorizontalGlue());

        add(toolbar, BorderLayout.NORTH);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "New Project", e -> createNewProject());
        addMenuItem(fileMenu, "Open Project...", e -> openProject());
        addMenuItem(fileMenu, "Save Project", e -> saveProject());
        addMenuItem(fileMenu, "Save Project As...", e -> saveProjectAs());
        addMenuItem(fileMenu, "Export GCODE File...", e -> exportGCodeFile());
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit", e -> System.exit(0));
        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");

        addMenuItem(viewMenu, "Reset View", e -> resetView());

        viewMenu.addSeparator();

        JCheckBoxMenuItem showGridButton = new JCheckBoxMenuItem("Show Grid", canvasPanel.isShowGrid());
        showGridButton.addItemListener(e -> canvasPanel.setShowGrid(showGridButton.isSelected()));
        viewMenu.add(showGridButton);

        JCheckBoxMenuItem magneticGridButton = new JCheckBoxMenuItem("Magnetic Grid", canvasPanel.isMagneticGrid());
        magneticGridButton.addItemListener(e -> canvasPanel.setMagneticGrid(magneticGridButton.isSelected()));
        viewMenu.add(magneticGridButton);

        JLabel gridSizeText = new JLabel("Grid size");
        JSpinner gridSizeField = new JSpinner(new SpinnerNumberModel(convertToUnit(canvasPanel.getGridSize(), getSelectedUnit()), 1, 1000, 1));

        gridSizeField.addChangeListener(e -> {
            double gridSize = getSelectedUnit().convertToMetric((double) gridSizeField.getValue());

            if (gridSize >= 10) {
                canvasPanel.setGridSize(gridSize);
            }
        });

        viewMenu.add(gridSizeText);
        viewMenu.add(gridSizeField);

        viewMenu.addSeparator();

        JCheckBoxMenuItem metricSelector = new JCheckBoxMenuItem(MeasurementUnit.METRIC.getABBREVIATION(), getSelectedUnit() == MeasurementUnit.METRIC);
        JCheckBoxMenuItem imperialSelector = new JCheckBoxMenuItem(MeasurementUnit.IMPERIAL.getABBREVIATION(), getSelectedUnit() == MeasurementUnit.IMPERIAL);

        DimensionField xPositionField = cutOptionsPanel.getXPositionField();
        DimensionField yPositionField = cutOptionsPanel.getYPositionField();

        metricSelector.addActionListener(e -> {
            if (metricSelector.isSelected()) {
                if (getSelectedUnit() == MeasurementUnit.IMPERIAL) {
                    xPositionField.setValue(MeasurementUnit.IMPERIAL.convertToMetric(xPositionField.getValue()));
                    yPositionField.setValue(MeasurementUnit.IMPERIAL.convertToMetric(yPositionField.getValue()));
                    cutOptionsPanel.setMeasurementUnit(MeasurementUnit.METRIC);

                    selectedUnit = MeasurementUnit.METRIC;
                    imperialSelector.setSelected(false);

                    gridSizeField.setValue(MeasurementUnit.IMPERIAL.convertToMetric((double) gridSizeField.getValue()));

                    toolManagementPanel.refreshToolsList();
                    updateBottomPanelDimensions();
                }
            }
            else {
                metricSelector.setSelected(true);
            }
        });

        imperialSelector.addActionListener(e -> {
            if (imperialSelector.isSelected()) {
                if (getSelectedUnit() == MeasurementUnit.METRIC) {
                    xPositionField.setValue(MeasurementUnit.METRIC.convertToImperial(xPositionField.getValue()));
                    yPositionField.setValue(MeasurementUnit.METRIC.convertToImperial(yPositionField.getValue()));
                    cutOptionsPanel.setMeasurementUnit(MeasurementUnit.IMPERIAL);

                    selectedUnit = MeasurementUnit.IMPERIAL;
                    metricSelector.setSelected(false);

                    gridSizeField.setValue(MeasurementUnit.METRIC.convertToImperial((double) gridSizeField.getValue()));

                    toolManagementPanel.refreshToolsList();
                    updateBottomPanelDimensions();
                }
            }
            else {
                imperialSelector.setSelected(true);
            }
        });

        viewMenu.add(metricSelector);
        viewMenu.add(imperialSelector);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);
    }

    private void resetView() {
        canvasPanel.resetView();
        bottomPanel.updateZoomPercentage(canvasPanel.getZoomPercentage());
    }

    private void updateBottomPanelDimensions() {
        PanneauDTO p = controller.getPanneau();

        bottomPanel.updatePanelDimensions(p.longueur, p.largeur, getSelectedUnit());
    }

    private void addDefaultTool() {
        toolManagementPanel.addToolNoSave(new ToolDTO(DEFAULT_TOOL_NAME, getSelectedUnit().convertToMetric(DEFAULT_TOOL_WIDTH), 0));
    }

    private void addMenuItem(JMenu menu, String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);

        item.addActionListener(listener);
        menu.add(item);
    }

    private void createNewProject() {
        controller.createProject("", controller.getPanneau());
        controller.saveProjectAs(this);
    }

    private void openProject() {
        controller.openProject(this);
    }

    private void saveProject() {
        controller.saveProject(this);
    }

    private void saveProjectAs() {
        controller.saveProjectAs(this);
    }

    private void exportGCodeFile() {
        controller.exportGCodeFile();
    }

    private void selectCutType(CutType cutType) {
        cutTypeMenu.setVisible(false);
        canvasPanel.setCurrentCutType(cutType);
        canvasPanel.toggleMouseListener();
    }

    private JButton createToolbarButton(String text, ActionListener listener) {
        JButton button = new JButton(text);

        if (listener != null) {
            button.addActionListener(listener);
        }

        button.setFocusPainted(false);
        button.setBackground(ACCENT_COLOR);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(100, TOOLBAR_HEIGHT));

        return button;
    }

    public void selectCut(CutDTO cut) {
        selectedCut = cut != null ? controller.getCutDTOById(cut.id) : null;

        refreshView();
    }

    public void refreshView() {
        canvasPanel.repaint();
        cutOptionsPanel.updateCutProperties();
        toolManagementPanel.refreshToolsList();
        rightPanel.setVisible(selectedCut != null);
    }

    public void refreshTools() {
        cutOptionsPanel.refreshToolsList();
    }

    public double convertToUnit(double measurement, MeasurementUnit unit) {
        return unit == MeasurementUnit.METRIC ? unit.convertToMetric(measurement) : unit.computeMetricToImperial(measurement);
    }

    public MeasurementUnit getSelectedUnit() {
        return selectedUnit;
    }

    public String getNoToolText() {
        return cutOptionsPanel.NO_TOOL_TEXT;
    }

    public CutDTO getSelectedCut() {
        return selectedCut;
    }

    public CanvasPanel getCanvasPanel() {
        return canvasPanel;
    }

    public BottomPanel getBottomPanel() {
        return bottomPanel;
    }
}
