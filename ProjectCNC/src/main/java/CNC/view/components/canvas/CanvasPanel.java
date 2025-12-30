package CNC.view.components.canvas;

import CNC.controller.Controller;
import CNC.domain.entities.CutType;
import CNC.view.MainView;
import CNC.view.dto.CutDTO;
import CNC.view.dto.PanneauDTO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CanvasPanel extends JPanel {
    protected final Controller controller;
    protected final MainView mainView;

    protected final Point panelOffset;
    private final double INITIAL_ZOOM_FACTOR = 0.47D;
    protected double zoomFactor;
    protected boolean showGrid;
    protected boolean magneticGrid;
    protected double gridSize;
    protected Point dragStart;
    protected CutType currentCutType;
    protected CutDTO previewCut = null;

    protected Point firstPointForLCut = null;
    protected MouseAdapter mouseAdapter;
    protected Point endPoint;
    protected Point startPoint;

    protected final double CLICK_DISTANCE_TOLERANCE = 5;

    protected List<Integer> horizontalLines = new ArrayList<>();
    protected List<Integer> verticalLines = new ArrayList<>();

    protected String lastUsedTool;

    protected CanvasRenderer canvasRenderer;
    protected CanvasMouseHandler canvasMouseHandler;
    protected Point corner = new Point();

    public CanvasPanel(Controller controller, MainView mainView) {
        this.controller = controller;
        this.mainView = mainView;

        setLastUsedTool(getMainView().DEFAULT_TOOL_NAME);

        this.setBackground(Color.WHITE);

        panelOffset = new Point(0, 0);
        zoomFactor = INITIAL_ZOOM_FACTOR;
        showGrid = true;
        magneticGrid = false;
        gridSize = 50;

        this.setLayout(new BorderLayout());

        this.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                Point mousePos = e.getPoint();
                double worldX = (mousePos.x - panelOffset.x) / zoomFactor;
                double worldY = (mousePos.y - panelOffset.y) / zoomFactor;

                if (e.getWheelRotation() < 0) {
                    zoomIn();
                }
                else {
                    zoomOut();
                }

                panelOffset.x = (int) (mousePos.x - worldX * zoomFactor);
                panelOffset.y = (int) (mousePos.y - worldY * zoomFactor);
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Comportement de drag du panneau uniquement si on n'est pas en mode ctrl-drag coupe
                // et pas en mode création
                if (!e.isControlDown() && currentCutType == null) {
                    dragStart = e.getPoint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                getCanvasMouseHandler().handleMouseClick(e);
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // On ne déplace le panneau que si on n'est pas en train de drag une coupe
                // et que ctrl n'est pas pressé
                if (dragStart != null && currentCutType == null && !e.isControlDown()) {
                    Point current = e.getPoint();

                    panelOffset.x += (current.x - dragStart.x);
                    panelOffset.y += (current.y - dragStart.y);
                    dragStart = current;

                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentCutType != null) {
                    updatePreviewCut();
                }

                getCanvasMouseHandler().updateCursorPosition(e);
            }
        });

        // Activation de l'affichage d'un tooltip (en hover)
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        getCanvasRenderer().paintCanvas((Graphics2D) g);
    }

    public void toggleMouseListener() {
        getCanvasMouseHandler().toggleMouseListener();
    }

    public void centerPanel() {
        calculateInitialZoomFactor();

        PanneauDTO panneau = controller.getPanneau();

        panelOffset.x = (getWidth() - (int) (panneau.longueur * zoomFactor)) / 2;
        panelOffset.y = (getHeight() - (int) (panneau.largeur * zoomFactor)) / 2;

        this.repaint();

        if (mainView.getBottomPanel() != null) {
            mainView.getBottomPanel().updateZoomPercentage(calculateZoomPercentage());
        }
    }

    private void calculateInitialZoomFactor() {
        PanneauDTO panneau = controller.getPanneau();

        double panelWidth = panneau.longueur;
        double panelHeight = panneau.largeur;

        double canvasWidth = getWidth();
        double canvasHeight = getHeight();

        double zoomFactorX = (canvasWidth / panelWidth) / 1.5D;
        double zoomFactorY = (canvasHeight / panelHeight) / 1.5D;

        zoomFactor = Math.min(zoomFactorX, zoomFactorY);
    }

    public void setCurrentCutType(CutType currentCutType) {
        this.currentCutType = currentCutType;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        this.repaint();
    }

    public void setMagneticGrid(boolean magneticGrid) {
        this.magneticGrid = magneticGrid;
    }

    public void setGridSize(double gridSize) {
        this.gridSize = gridSize;
        this.repaint();
    }

    public void resetView() {
        zoomFactor = INITIAL_ZOOM_FACTOR;
        centerPanel();
    }

    private void zoomIn() {
        zoomFactor *= 1.1;

        firePropertyChange("zoomFactor", null, zoomFactor);
        repaint();
        mainView.getBottomPanel().updateZoomPercentage(calculateZoomPercentage());
    }

    private void zoomOut() {
        zoomFactor /= 1.1;

        repaint();
        mainView.getBottomPanel().updateZoomPercentage(calculateZoomPercentage());
    }

    public void setZoomPercentage(int percent) {
        zoomFactor = INITIAL_ZOOM_FACTOR * (percent / 100.0);

        repaint();
        mainView.getBottomPanel().updateZoomPercentage(calculateZoomPercentage());
    }

    private int calculateZoomPercentage() {
        return (int) Math.round((zoomFactor / INITIAL_ZOOM_FACTOR) * 100);
    }

    public int getZoomPercentage() {
        return calculateZoomPercentage();
    }

    public Controller getController() {
        return controller;
    }

    public MainView getMainView() {
        return mainView;
    }

    public Point getPanelOffset() {
        return panelOffset;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public boolean isMagneticGrid() {
        return magneticGrid;
    }

    public double getGridSize() {
        return gridSize;
    }

    public CutType getCurrentCutType() {
        return currentCutType;
    }

    public void setPreviewCut(CutDTO previewCut) {
        this.previewCut = previewCut;
    }

    public CutDTO getPreviewCut() {
        return previewCut;
    }

    public double getClickDistanceTolerance() {
        return CLICK_DISTANCE_TOLERANCE;
    }

    public Point getFirstPointForLCut() {
        return firstPointForLCut;
    }

    public void setFirstPointForLCut(Point p) {
        this.firstPointForLCut = p;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Point p) {
        this.endPoint = p;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point p) {
        this.startPoint = p;
    }

    public Point getCorner() {
        return corner;
    }

    public void setCorner(Point c) {
        this.corner = c;
    }

    public MouseAdapter getMouseAdapterInstance() {
        return mouseAdapter;
    }

    public void setMouseAdapterInstance(MouseAdapter adapter) {
        this.mouseAdapter = adapter;
    }

    protected CanvasRenderer getCanvasRenderer() {
        if (canvasRenderer == null) {
            canvasRenderer = new CanvasRenderer(this);
        }

        return canvasRenderer;
    }

    protected CanvasMouseHandler getCanvasMouseHandler() {
        if (canvasMouseHandler == null) {
            canvasMouseHandler = new CanvasMouseHandler(this);
        }

        return canvasMouseHandler;
    }

    protected Integer findClosestLine(int position, List<Integer> lines) {
        Integer closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (int line : lines) {
            int distance = Math.abs(line - position);

            if (distance < minDistance && distance <= getClickDistanceTolerance() / zoomFactor * gridSize / 50) {
                closest = line;
                minDistance = distance;
            }
        }

        return closest;
    }

    public void setLastUsedTool(String toolName) {
        lastUsedTool = toolName;
    }

    public void updatePreviewCut() {
        if (getCurrentCutType() == null) {
            setPreviewCut(null);
            return;
        }

        CutDTO preview = new CutDTO();
        preview.type = getCurrentCutType();

        setPreviewCut(preview);
        repaint();
    }
}
