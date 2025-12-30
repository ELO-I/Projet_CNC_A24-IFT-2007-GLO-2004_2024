package CNC.view.components.canvas;

import CNC.domain.entities.CutType;
import CNC.view.dto.CutDTO;
import CNC.view.dto.PanneauDTO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class CanvasMouseHandler {
    private final CanvasPanel panel;

    private CutDTO selectedCutForDrag = null;
    private Point initialMousePosition = null;
    private List<Point> originalCutPoints = null;

    public CanvasMouseHandler(CanvasPanel panel) {
        this.panel = panel;
    }

    public void toggleMouseListener() {
        if (panel.getMouseAdapterInstance() == null) {
            MouseAdapter mouseAdapter = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    int x = (int) ((e.getX() - panel.getPanelOffset().x) / panel.getZoomFactor());
                    int y = (int) ((e.getY() - panel.getPanelOffset().y) / panel.getZoomFactor());

                    // Ctrl + Clic sur une coupe => préparation du drag (si possible)
                    if (e.isControlDown()) {
                        CutDTO cutClicked = panel.getCanvasRenderer().getCutAtPosition(x, y);
                        if (cutClicked != null) {
                            // Vérification si c'est un BORDER_CUT (pas déplaçable)
                            if (cutClicked.type == CutType.BORDER_CUT) {
                                // On ne sélectionne pas pour drag, c'est non déplaçable
                                return;
                            }
                            selectedCutForDrag = cutClicked;
                            panel.getMainView().selectCut(cutClicked);
                            initialMousePosition = new Point(x, y);
                            // Sauvegarde des points originaux
                            originalCutPoints = new ArrayList<>();
                            for (Point p : cutClicked.points) {
                                originalCutPoints.add(new Point(p.x, p.y));
                            }
                            return;
                        }
                    }

                    // Sinon on est potentiellement en mode création de coupe
                    if (panel.getCurrentCutType() != null) {
                        if (panel.isMagneticGrid()) {
                            Integer snapX = panel.findClosestLine(x, panel.verticalLines);
                            Integer snapY = panel.findClosestLine(y, panel.horizontalLines);
                            if (snapX != null) x = snapX;
                            if (snapY != null) y = snapY;
                        }

                        panel.setStartPoint(new Point(x, y));
                        panel.setEndPoint(panel.getStartPoint());
                        panel.repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = (int) ((e.getX() - panel.getPanelOffset().x) / panel.getZoomFactor());
                    int y = (int) ((e.getY() - panel.getPanelOffset().y) / panel.getZoomFactor());

                    // Si on est en train de déplacer une coupe existante
                    if (selectedCutForDrag != null && originalCutPoints != null) {
                        int dx = x - initialMousePosition.x;
                        int dy = y - initialMousePosition.y;

                        switch (selectedCutForDrag.type) {
                            case VERTICAL -> {
                                // Déplacement uniquement horizontal
                                for (int i = 0; i < selectedCutForDrag.points.size(); i++) {
                                    Point orig = originalCutPoints.get(i);
                                    selectedCutForDrag.points.get(i).x = orig.x + dx;
                                    selectedCutForDrag.points.get(i).y = orig.y; // inchange
                                }
                                // Snap magnétique sur l’axe X
                                if (panel.isMagneticGrid()) {
                                    int currentX = selectedCutForDrag.points.getFirst().x;
                                    Integer snapX = panel.findClosestLine(currentX, panel.verticalLines);
                                    if (snapX != null) {
                                        int diff = snapX - currentX;
                                        for (Point p : selectedCutForDrag.points) {
                                            p.x += diff;
                                        }
                                    }
                                }
                            }
                            case HORIZONTAL -> {
                                // Déplacement uniquement vertical
                                for (int i = 0; i < selectedCutForDrag.points.size(); i++) {
                                    Point orig = originalCutPoints.get(i);
                                    selectedCutForDrag.points.get(i).y = orig.y + dy;
                                    selectedCutForDrag.points.get(i).x = orig.x; // inchange
                                }
                                // Snap magnétique sur l’axe Y
                                if (panel.isMagneticGrid()) {
                                    int currentY = selectedCutForDrag.points.getFirst().y;
                                    Integer snapY = panel.findClosestLine(currentY, panel.horizontalLines);
                                    if (snapY != null) {
                                        int diff = snapY - currentY;
                                        for (Point p : selectedCutForDrag.points) {
                                            p.y += diff;
                                        }
                                    }
                                }
                            }
                            case RECTANGULAR, FORBIDDEN_ZONE, L -> {
                                // Déplacement libre
                                for (int i = 0; i < selectedCutForDrag.points.size(); i++) {
                                    Point orig = originalCutPoints.get(i);
                                    selectedCutForDrag.points.get(i).x = orig.x + dx;
                                    selectedCutForDrag.points.get(i).y = orig.y + dy;
                                }
                                // Snap magnétique sur X et Y
                                if (panel.isMagneticGrid() && !selectedCutForDrag.points.isEmpty()) {
                                    // On prend le premier point comme référence
                                    int refX = selectedCutForDrag.points.getFirst().x;
                                    int refY = selectedCutForDrag.points.getFirst().y;
                                    Integer snapX = panel.findClosestLine(refX, panel.verticalLines);
                                    Integer snapY = panel.findClosestLine(refY, panel.horizontalLines);

                                    int diffX = (snapX != null) ? (snapX - refX) : 0;
                                    int diffY = (snapY != null) ? (snapY - refY) : 0;

                                    if (diffX != 0 || diffY != 0) {
                                        for (Point p : selectedCutForDrag.points) {
                                            p.x += diffX;
                                            p.y += diffY;
                                        }
                                    }
                                }
                            }
                            case BORDER_CUT -> {
                                // Ne pas déplacer
                            }
                        }

                        panel.repaint();
                        return;
                    }

                    // Sinon, mode création de coupe
                    if (panel.getCurrentCutType() != null) {
                        if (panel.isMagneticGrid()) {
                            Integer snapX = panel.findClosestLine(x, panel.verticalLines);
                            Integer snapY = panel.findClosestLine(y, panel.horizontalLines);
                            if (snapX != null) x = snapX;
                            if (snapY != null) y = snapY;
                        }

                        panel.setEndPoint(new Point(x, y));
                        panel.repaint();
                    }//oui
                }


                @Override
                public void mouseReleased(MouseEvent e) {
                    // Fin du déplacement de coupe
                    if (selectedCutForDrag != null) {
                        // Sauvegarde l'état après le déplacement
                        panel.getController().updateCut(selectedCutForDrag); // ou panel.getController().saveState();

                        selectedCutForDrag = null;
                        originalCutPoints = null;
                        initialMousePosition = null;
                        panel.getMainView().refreshView();
                        panel.repaint();
                        return;
                    }

                    // Création de coupe si on était en mode création
                    if (panel.getCurrentCutType() != null) {
                        CutType current = panel.getCurrentCutType();
                        int x = (int) ((e.getX() - panel.getPanelOffset().x) / panel.getZoomFactor());
                        int y = (int) ((e.getY() - panel.getPanelOffset().y) / panel.getZoomFactor());

                        if (panel.isMagneticGrid()) {
                            Integer snapX = panel.findClosestLine(x, panel.verticalLines);
                            Integer snapY = panel.findClosestLine(y, panel.horizontalLines);

                            if (snapX != null) x = snapX;
                            if (snapY != null) y = snapY;
                        }

                        panel.setEndPoint(new Point(x, y));

                        List<Point> points = new ArrayList<>();
                        PanneauDTO p = panel.getController().getPanneau();

                        switch (current) {
                            case RECTANGULAR, FORBIDDEN_ZONE -> {
                                int x1 = Math.min(panel.getStartPoint().x, panel.getEndPoint().x);
                                int y1 = Math.min(panel.getStartPoint().y, panel.getEndPoint().y);
                                int width = Math.abs(panel.getEndPoint().x - panel.getStartPoint().x);
                                int height = Math.abs(panel.getEndPoint().y - panel.getStartPoint().y);

                                points.add(new Point(x1, y1));
                                points.add(new Point(x1 + width, y1));
                                points.add(new Point(x1 + width, y1 + height));
                                points.add(new Point(x1, y1 + height));
                            }
                            case BORDER_CUT -> {
                                int w = Math.abs(panel.getEndPoint().x - panel.getStartPoint().x);
                                int h = Math.abs(panel.getEndPoint().y - panel.getStartPoint().y);
                                int x1 = (int) ((p.longueur - w) / 2);
                                int y1 = (int) ((p.largeur - h) / 2);

                                points.add(new Point(x1, y1));
                                points.add(new Point(x1 + w, y1));
                                points.add(new Point(x1 + w, y1 + h));
                                points.add(new Point(x1, y1 + h));
                            }
                            case VERTICAL -> {
                                int lineX = panel.getEndPoint().x;
                                points.addAll(panel.getCanvasRenderer().computeVerticalCutPoints(lineX));
                            }
                            case HORIZONTAL -> {
                                int lineY = panel.getEndPoint().y;
                                points.addAll(panel.getCanvasRenderer().computeHorizontalCutPoints(lineY));
                            }
                            case L -> {
                                points.add(panel.getCorner());
                                analyseEndPointsForL(points);
                                panel.setCorner(new Point());
                            }
                        }

                        CutDTO newCut = new CutDTO(
                                UUID.randomUUID(),
                                "",
                                panel.getCurrentCutType(),
                                0,
                                panel.lastUsedTool,
                                points,
                                null,
                                true
                        );
                        panel.controller.createCut(newCut);

                        // Sauvegarde de l'état après création de la coupe
                        panel.getController().updateCut(newCut); // ou panel.getController().saveState();

                        panel.setEndPoint(null);
                        panel.setStartPoint(null);
                        panel.setCurrentCutType(null);
                        panel.getMainView().refreshView();
                        panel.repaint();
                    }
                }


                @Override
                public void mouseMoved(MouseEvent e) {
                    // Mise à jour du tooltip
                    double worldX = (e.getX() - panel.getPanelOffset().x) / panel.getZoomFactor();
                    double worldY = (e.getY() - panel.getPanelOffset().y) / panel.getZoomFactor();
                    CutDTO hoveredCut = panel.getCanvasRenderer().getCutAtPosition(worldX, worldY);

                    if (hoveredCut != null) {
                        panel.setToolTipText(getTooltipForCut(hoveredCut));
                    } else {
                        panel.setToolTipText(null);
                    }
                }
            };

            panel.addMouseListener(mouseAdapter);
            panel.addMouseMotionListener(mouseAdapter);
            panel.setMouseAdapterInstance(mouseAdapter);
        }
    }

    public void handleMouseClick(MouseEvent e) {
        Point clickPoint = e.getPoint();
        double worldX = (clickPoint.x - panel.getPanelOffset().x) / panel.getZoomFactor();
        double worldY = (clickPoint.y - panel.getPanelOffset().y) / panel.getZoomFactor();

        CutType ctype = panel.getCurrentCutType();

        if (ctype == CutType.L) {
            return;
        }

        if (ctype == CutType.VERTICAL || ctype == CutType.HORIZONTAL ||
                ctype == CutType.RECTANGULAR || ctype == CutType.FORBIDDEN_ZONE || ctype == CutType.BORDER_CUT) {
            return;
        }

        CutDTO clickedCut = panel.getCanvasRenderer().getCutAtPosition(worldX, worldY);
        panel.getMainView().selectCut(clickedCut);
    }

    public void updateCursorPosition(MouseEvent e) {
        double worldX = (e.getX() - panel.getPanelOffset().x) / panel.getZoomFactor();
        double worldY = (panel.getController().getPanneau().largeur - (e.getY() - panel.getPanelOffset().y) / panel.getZoomFactor());
        panel.getMainView().getBottomPanel().updateCursorPosition(worldX, worldY, panel.getMainView().getSelectedUnit());
    }

    private void analyseEndPointsForL(List<Point> listPoints){
        // Code inchangé
        int width = (panel.getEndPoint().x - panel.getStartPoint().x);
        int height = (panel.getEndPoint().y - panel.getStartPoint().y);
        double closestAbove = Double.MAX_VALUE;
        double closestSide = Double.MAX_VALUE;
        double sideReference = panel.getCorner().x - width ;
        double heightReference = panel.getCorner().y - height;
        List<CutDTO> cuts = panel.controller.getCuts();
        Point sidePoint = null;
        Point topPoint = null;
        for (CutDTO cut : cuts){
            if (cut.type == CutType.VERTICAL){
                if (panel.getStartPoint().x <= cut.points.getFirst().x && Math.abs(cut.points.getFirst().x - panel.getStartPoint().x) < closestSide && width < 0){
                    sidePoint = new Point(cut.points.getFirst().x, panel.getCorner().y);
                    closestSide = Math.abs(cut.points.getFirst().x - panel.getStartPoint().x);
                }
                else if (panel.getStartPoint().x > cut.points.getFirst().x && Math.abs(panel.getStartPoint().x - cut.points.getFirst().x) < closestSide && width > 0) {
                    sidePoint = new Point(cut.points.getFirst().x, panel.getCorner().y);
                    closestSide = Math.abs(panel.getStartPoint().x - cut.points.getFirst().x);
                }
            }
            else if (cut.type == CutType.HORIZONTAL){
                if (panel.getStartPoint().y <= cut.points.getFirst().y && Math.abs(cut.points.getFirst().y - panel.getStartPoint().y) < closestAbove && height < 0) {
                    topPoint = new Point(panel.getCorner().x, cut.points.getFirst().y);
                    closestAbove = Math.abs(cut.points.getFirst().y - panel.getStartPoint().y);
                }
                else if (panel.getStartPoint().y > cut.points.getFirst().y && Math.abs(panel.getStartPoint().y - cut.points.getFirst().y) < closestAbove && height > 0) {
                    topPoint = new Point(panel.getCorner().x, cut.points.getFirst().y);
                    closestAbove = Math.abs(panel.getStartPoint().y - cut.points.getFirst().y);
                }
            }
            else if (cut.type == CutType.RECTANGULAR || cut.type == CutType.BORDER_CUT) {
                for (int i = 0; i < cut.points.size(); i++ ){
                    List<Point> points = cut.points;
                    if (sideReference <= points.get(i).x && points.get(i).x < closestSide && width < 0){
                        if (panel.getEndPoint().y > points.get(0).y && panel.getEndPoint().y < points.get(3).y){
                            sidePoint = new Point( points.get(i).x, panel.getCorner().y);
                            closestSide = Math.abs(points.get(i).x - sideReference);
                        }
                    } else if (sideReference > points.get(i).x && Math.abs(sideReference - points.get(i).x) < closestSide && width > 0) {
                        if (panel.getEndPoint().y > points.get(1).y && panel.getEndPoint().y < points.get(2).y) {
                            sidePoint = new Point(points.get(i).x, panel.getCorner().y);
                            closestSide = Math.abs(sideReference - points.get(i).x);
                        }
                    }
                    if (heightReference <= points.get(i).y && points.get(i).y < closestAbove && height < 0) {
                        if (panel.getEndPoint().x > points.get(3).x && panel.getEndPoint().x < points.get(2).x) {
                            topPoint = new Point(panel.getCorner().x,points.get(i).y);
                            closestAbove = points.get(i).y;
                        }
                    } else if (heightReference > points.get(i).y && Math.abs(heightReference - points.get(i).y) < closestAbove && height > 0) {
                        if (panel.getEndPoint().x > points.get(0).x && panel.getEndPoint().x < points.get(1).x) {
                            topPoint = new Point(panel.getCorner().x, points.get(i).y);
                            closestAbove = Math.abs(heightReference - points.get(i).y);
                        }
                    }
                }
            }
        }

        if (sidePoint == null){
            if (sideReference <= panel.getCorner().x) {
                listPoints.add(new Point(0, panel.getCorner().y));
            }
            else {
                listPoints.add(new Point((int) (0 + panel.controller.getPanneau().longueur), panel.getCorner().y));
            }
        }
        else {
            listPoints.add(sidePoint);
        }

        if (topPoint == null){
            if (heightReference <= panel.getCorner().y) {
                listPoints.add(new Point(panel.getCorner().x, 0));
            }
            else {
                listPoints.add(new Point(panel.getCorner().x, (int) (0 + panel.controller.getPanneau().largeur)));
            }
        }
        else {
            listPoints.add(topPoint);
        }
    }

    private String getTooltipForCut(CutDTO cut) {
        switch (cut.type) {
            case VERTICAL -> {
                String refName = getReferenceNameForCut(cut);
                int posX = cut.points.getFirst().x;
                return "Coupe verticale - Référence: " + refName + " - Position X: " + posX;
            }
            case HORIZONTAL -> {
                String refName = getReferenceNameForCut(cut);
                int posY = cut.points.getFirst().y;
                return "Coupe horizontale - Référence: " + refName + " - Position Y: " + posY;
            }
            case RECTANGULAR, FORBIDDEN_ZONE, BORDER_CUT -> {
                int width = Math.abs(cut.points.get(1).x - cut.points.get(0).x);
                int height = Math.abs(cut.points.get(3).y - cut.points.get(0).y);
                return "Rectangle - L: " + width + " / H: " + height;
            }
            case L -> {
                int minX = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;
                for (Point p : cut.points) {
                    if (p.x < minX) minX = p.x;
                    if (p.x > maxX) maxX = p.x;
                    if (p.y < minY) minY = p.y;
                    if (p.y > maxY) maxY = p.y;
                }
                int w = maxX - minX;
                int h = maxY - minY;
                return "Coupe en L - Taille approx : " + w + "x" + h;
            }
        }
        return "";
    }

    // Méthode fictive pour obtenir le nom de la référence
    // À adapter selon votre logique interne (par ex. depuis le controller, un champ dans cutDTO, etc.)
    private String getReferenceNameForCut(CutDTO cut) {
        // Logique custom: par exemple, si vous stockez la référence dans cut.label ou autre
        // Ici on renvoie juste une valeur d'exemple
        return "Ref_A";//test
    }
}
