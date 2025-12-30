package CNC.view.components.canvas;

import CNC.domain.entities.CutType;
import CNC.view.dto.CutDTO;
import CNC.view.dto.PanneauDTO;
import CNC.view.dto.ToolDTO;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CanvasRenderer {
    private final CanvasPanel panel;

    public CanvasRenderer(CanvasPanel panel) {
        this.panel = panel;
    }

    public void paintCanvas(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.translate(panel.getPanelOffset().x, panel.getPanelOffset().y);
        g2d.scale(panel.getZoomFactor(), panel.getZoomFactor());

        if (panel.isShowGrid()) {
            drawGrid(g2d);
        }

        drawPanneau(g2d);

        List<CutDTO> cuts = panel.getController().getCuts();

        for (CutDTO c : cuts) {
            updateCutValidity(c);

            panel.controller.updateCutNoSaveState(c);
        }

        for (CutDTO c : cuts) {
            drawCut(g2d, c);
        }

        if (panel.getPreviewCut() != null && panel.getCurrentCutType() != null) {
            drawPreviewCut(g2d);
        }

        drawAllIntersectionPoints(g2d);
    }

    private void drawPanneau(Graphics2D g2d) {
        PanneauDTO p = panel.getController().getPanneau();

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect(0, 0, (int) p.longueur, (int) p.largeur);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(115, 115, 115));
        g2d.setStroke(new BasicStroke(1.0f));

        PanneauDTO panneau = panel.getController().getPanneau();
        double gridSize = panel.getGridSize();

        panel.horizontalLines.clear();
        panel.verticalLines.clear();

        double curr = gridSize;

        while (curr < panneau.largeur) {
            int y = (int) curr;

            g2d.drawLine(0, y, (int) panneau.longueur, y);
            panel.horizontalLines.add(y);

            curr += gridSize;
        }

        curr = gridSize;

        while (curr < panneau.longueur) {
            int x = (int) curr;

            g2d.drawLine(x, 0, x, (int) panneau.largeur);
            panel.verticalLines.add(x);

            curr += gridSize;
        }
    }

    private void drawCut(Graphics2D g2d, CutDTO cut) {
        ToolDTO cutTool = null;

        for (ToolDTO tool : panel.getController().getTools()) {
            if (tool != null && tool.name.equals(cut.toolName)) {
                cutTool = tool;

                break;
            }
        }

        drawCut(g2d, cut, cutTool);
    }

    private void drawCut(Graphics2D g2d, CutDTO cut, ToolDTO tool) {
        float cutWidth = 1.0f;

        if (tool != null) {
            cutWidth = (float)tool.cutWidth;
        }

        Color baseColor = new Color(90,60,0);

        if (cut.type == CutType.FORBIDDEN_ZONE) {
            g2d.setColor(new Color(255,0,0,80));
            fillPolygon(g2d, cut.points);

            baseColor = Color.RED.darker();
        }

        if (!cut.valid) {
            baseColor = Color.RED.darker();
        }

        if (panel.getMainView().getSelectedCut() != null && cut.id.equals(panel.getMainView().getSelectedCut().id)) {
            if (cut.valid) {
                baseColor = Color.GREEN;
            }
            else {
                baseColor = Color.RED;
            }
        }

        g2d.setStroke(new BasicStroke(cutWidth));
        g2d.setColor(baseColor);
        if (cut.type == CutType.L) {
            List<Point> cutPoints = cut.points;
            if (cutPoints.get(0).x < cutPoints.get(1).x) {
                g2d.drawLine(
                        cutPoints.get(0).x,
                        cutPoints.get(0).y,
                        (int) (cutPoints.get(1).x - cutWidth * 0.5D),
                        cutPoints.get(1).y
                );
            } else {
                g2d.drawLine(
                        cutPoints.get(0).x,
                        cutPoints.get(0).y,
                        (int) (cutPoints.get(1).x + cutWidth * 0.5D),
                        cutPoints.get(1).y
                );
            }

            if (cutPoints.get(0).y < cutPoints.get(2).y) {
                g2d.drawLine(
                        cutPoints.get(0).x,
                        cutPoints.get(0).y,
                        cutPoints.get(2).x,
                        (int) (cutPoints.get(2).y - cutWidth * 0.5D)
                );
            } else {
                g2d.drawLine(
                        cutPoints.get(0).x,
                        cutPoints.get(0).y,
                        cutPoints.get(2).x,
                        (int) (cutPoints.get(2).y + cutWidth * 0.5D)
                );
            }
        } else {
            for (int i = 0; i < cut.points.size(); i++) {
                Point p1 = cut.points.get(i);
                Point p2 = cut.points.get((i + 1) % cut.points.size());

                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private Point findMinXY(List<Point> pts) {
        Point minP = pts.getFirst();

        for (Point p : pts) {
            if (p.x < minP.x || (p.x == minP.x && p.y < minP.y)) {
                minP = p;
            }
        }

        return minP;
    }

    private void fillPolygon(Graphics2D g2d, List<Point> pts) {
        int[] xs = new int[pts.size()];
        int[] ys = new int[pts.size()];

        for (int i = 0; i < pts.size(); i++) {
            xs[i] = pts.get(i).x;
            ys[i] = pts.get(i).y;
        }

        g2d.fillPolygon(xs, ys, pts.size());
    }

    private void drawPreviewCut(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);
        g2d.setStroke(new BasicStroke(2));

        CutType type = panel.getCurrentCutType();

        if (type == CutType.FORBIDDEN_ZONE) {
            g2d.setColor(Color.RED);
        }

        Point startPoint = panel.getStartPoint();
        Point endPoint = panel.getEndPoint();

        if (type == null || startPoint == null || endPoint == null) {
            return;
        }

        switch (type) {
            case RECTANGULAR, FORBIDDEN_ZONE, BORDER_CUT -> {
                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int w = Math.abs(endPoint.x - startPoint.x);
                int h = Math.abs(endPoint.y - startPoint.y);

                g2d.drawRect(x, y, w, h);
            }
            case VERTICAL -> {
                int lineX = endPoint.x;
                g2d.drawLine(lineX, 0, lineX, (int) panel.getController().getPanneau().largeur);
            }
            case HORIZONTAL -> {
                int lineY = endPoint.y;
                g2d.drawLine(0, lineY, (int) panel.getController().getPanneau().longueur, lineY);
            }
            case L -> {
                if (panel.getStartPoint() != null && panel.getEndPoint() != null) {
                    panel.getCorner().x = panel.getEndPoint().x;
                    panel.getCorner().y = panel.getEndPoint().y;
                    int width = (panel.getEndPoint().x - panel.getStartPoint().x);
                    int height = (panel.getEndPoint().y - panel.getStartPoint().y);

                    g2d.drawLine(panel.getCorner().x, panel.getCorner().y, panel.getCorner().x - width, panel.getCorner().y);
                    g2d.drawLine(panel.getCorner().x, panel.getCorner().y, panel.getCorner().x, panel.getCorner().y - height);
                }

            }
        }
    }

    public CutDTO getCutAtPosition(double x, double y) {
        List<CutDTO> cuts = panel.getController().getCuts();

        for (CutDTO cut : cuts) {
            if (isPointOnCut(x, y, cut, panel.getClickDistanceTolerance() / panel.getZoomFactor())) {
                return cut;
            }
        }

        return null;
    }

    private boolean isPointOnCut(double x, double y, CutDTO cut, double tol) {
        if (cut.points == null || cut.points.isEmpty()) {
            return false;
        }

        List<Point> segments = getCutSegmentsForHitTest(cut);

        for (int i = 0; i < segments.size(); i += 2) {
            Point A = segments.get(i);
            Point B = segments.get(i + 1);

            if (distancePointToSegment(x,y,A,B)<=tol) {
                return true;
            }
        }

        return false;
    }

    // Retourne la liste des segments effectivement dessinés pour la coupe
    private List<Point> getCutSegmentsForHitTest(CutDTO cut) {
        List<Point> points = new ArrayList<>();

        if (cut.type == CutType.L && cut.points.size() == 4) {
            Point minXY = findMinXY(cut.points);
            Point horiz = null, vert = null;

            for (Point p : cut.points) {
                if (p.equals(minXY)) {
                    continue;
                }

                if (p.y == minXY.y) {
                    horiz = p;
                }

                if (p.x == minXY.x) {
                    vert = p;
                }
            }

            if (horiz != null) {
                points.add(minXY);
                points.add(horiz);
            }

            if (vert != null) {
                points.add(minXY);
                points.add(vert);
            }
        }
        else {
            for (int i = 0; i < cut.points.size(); i++) {
                points.add(cut.points.get(i));
                points.add(cut.points.get((i + 1) % cut.points.size()));
            }
        }

        return points;
    }

    private double distancePointToSegment(double px, double py, Point A, Point B) {
        double x1 = A.x;
        double y1 = A.y;
        double x2 = B.x;
        double y2 = B.y;
        double dx = x2 - x1;
        double dy = y2 - y1;

        double hypo = Math.hypot(px - x1, py - y1);

        if ((dx == 0) && (dy == 0)) {
            return hypo;
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);

        if (t < 0) {
            return hypo;
        }
        else if (t > 1) {
            return Math.hypot(px - x2, py - y2);
        }

        return Math.hypot(px - (x1+t*dx), py - (y1+t*dy));
    }

    private void updateCutValidity(CutDTO cut) {
        cut.valid = true;

        PanneauDTO p = panel.getController().getPanneau();

        for (Point pt : cut.points) {
            if (pt.x < 0 || pt.y < 0 || pt.x > p.longueur || pt.y > p.largeur) {
                cut.valid = false;

                return;
            }
        }

        // zones interdites
        List<CutDTO> forbidden = getForbiddenZones();

        for (CutDTO fz : forbidden) {
            if (polygonsIntersect(cut.points, fz.points)) {
                cut.valid = false;

                return;
            }
        }
    }

    private CutDTO getBordureCut() {
        for (CutDTO c : panel.getController().getCuts()) {
            if (c.type == CutType.BORDER_CUT) {
                return c;
            }
        }

        return null;
    }

    private List<CutDTO> getForbiddenZones() {
        List<CutDTO> res = new ArrayList<>();

        for (CutDTO c : panel.getController().getCuts()) {
            if (c.type == CutType.FORBIDDEN_ZONE) {
                res.add(c);
            }
        }

        return res;
    }

    private boolean allPointsInsidePolygon(List<Point> pts, List<Point> poly) {
        for (Point p : pts) {
            if (!pointInPolygon(p,poly)) {
                return false;
            }
        }

        return true;
    }

    private boolean pointInPolygon(Point p, List<Point> polygon) {
        int count = 0;

        for (int i= 0 ; i < polygon.size(); i++) {
            Point A = polygon.get(i);
            Point B = polygon.get((i + 1) % polygon.size());

            if (((A.y <= p.y && p.y < B.y) || (B.y <= p.y && p.y < A.y)) &&
                    (p.x < (B.x - A.x) * (p.y - A.y) / (B.y - A.y) + A.x)) {
                count++;
            }
        }

        return count % 2 == 1;
    }

    private boolean polygonsIntersect(List<Point> poly1, List<Point> poly2) {
        for (int i = 0; i < poly1.size(); i++) {
            Point A1 = poly1.get(i);
            Point A2 = poly1.get((i + 1) % poly1.size());

            for (int j = 0; j < poly2.size(); j++) {
                Point B1 = poly2.get(j);
                Point B2 = poly2.get((j + 1) % poly2.size());

                if (segmentsIntersect(A1, A2, B1, B2)) {
                    return true;
                }
            }
        }

        // Si les bords ne se croisent pas, vérifie si un polygone est entièrement à l'intérieur de l'autre
        return isInsidePolygon(poly1, poly2);// Pas d'intersection
    }

    private boolean isInsidePolygon(List<Point> poly1, List<Point> poly2) {
        for (Point p : poly1) {
            if (pointInPolygon(p, poly2)) {
                return true;
            }
        }

        return false;
    }

    private boolean segmentsIntersect(Point p1, Point p2, Point p3, Point p4) {
        return ccw(p1, p3, p4) != ccw(p2, p3, p4) && ccw(p1, p2, p3) != ccw(p1, p2, p4);
    }

    private boolean ccw(Point A, Point B, Point C) {
        return (C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x);
    }

    public List<Point> computeVerticalCutPoints(int lineX) {
        return intersectPolygonWithVerticalLine(getWorkAreaPolygon(), lineX);
    }

    public List<Point> computeHorizontalCutPoints(int lineY) {
        return intersectPolygonWithHorizontalLine(getWorkAreaPolygon(),lineY);
    }

    private List<Point> getWorkAreaPolygon() {
        CutDTO bordure = getBordureCut();

        if (bordure != null) {
            return bordure.points;
        }

        PanneauDTO p = panel.getController().getPanneau();
        List<Point> poly = new ArrayList<>();

        poly.add(new Point(0, 0));
        poly.add(new Point((int) p.longueur, 0));
        poly.add(new Point((int) p.longueur, (int) p.largeur));
        poly.add(new Point(0, (int) p.largeur));

        return poly;
    }

    private List<Point> intersectPolygonWithVerticalLine(List<Point> poly, int x) {
        List<Point> intersections = new ArrayList<>();

        for (int i = 0; i < poly.size(); i++) {
            Point A = poly.get(i);
            Point B = poly.get((i + 1) % poly.size());

            if ((A.x <= x && B.x >= x) || (B.x <= x && A.x >= x)) {
                if (A.x == B.x) {
                    intersections.add(A);

                    if (!A.equals(B)) {
                        intersections.add(B);
                    }
                }
                else {
                    double t = (double) (x - A.x) / (B.x - A.x);
                    double Y = A.y + t * (B.y - A.y);

                    intersections.add(new Point(x, (int) Math.round(Y)));
                }
            }
        }

        intersections = removeDuplicates(intersections);

        intersections.sort(Comparator.comparingInt(p -> p.y));

        if (intersections.size() > 2) {
            return List.of(intersections.getFirst(), intersections.getLast());
        }

        return intersections;
    }

    private List<Point> intersectPolygonWithHorizontalLine(List<Point> poly, int y) {
        List<Point> intersections = new ArrayList<>();

        for (int i = 0; i < poly.size(); i++) {
            Point A = poly.get(i);
            Point B = poly.get((i + 1) % poly.size());

            if ((A.y <= y && B.y >= y) || (B.y <= y && A.y >= y)) {
                if (A.y == B.y) {
                    intersections.add(A);

                    if (!A.equals(B)) {
                        intersections.add(B);
                    }
                }
                else {
                    double t = (double) (y - A.y) / (B.y - A.y);
                    double X = A.x + t * (B.x - A.x);

                    intersections.add(new Point((int) Math.round(X), y));
                }
            }
        }

        intersections = removeDuplicates(intersections);

        intersections.sort(Comparator.comparingInt(p -> p.x));

        if (intersections.size() > 2) {
            return List.of(intersections.getFirst(), intersections.getLast());
        }

        return intersections;
    }

    private List<Point> removeDuplicates(List<Point> pts) {
        List<Point> result = new ArrayList<>();

        for (Point p : pts) {
            if (!containsPoint(result, p)) {
                result.add(p);
            }
        }

        return result;
    }

    private boolean containsPoint(List<Point> list, Point p) {
        for (Point pp : list) {
            if (pp.x == p.x && pp.y == p.y) {
                return true;
            }
        }

        return false;
    }

    private void drawAllIntersectionPoints(Graphics2D g2d) {
        List<Point> intersectionPoints = getAllIntersectionPoints();
        float maxToolWidth = getMaxToolWidth();
        int size = (int) Math.max(8, maxToolWidth * 2);

        for (Point pt : intersectionPoints) {
            g2d.setColor(Color.BLUE);
            g2d.fillOval(pt.x - size / 2, pt.y - size / 2, size, size);
        }
    }

    private float getMaxToolWidth() {
        float maxW = 1.0f;

        for (ToolDTO t : panel.getController().getTools()) {
            if (t != null && t.cutWidth > maxW) {
                maxW = (float) t.cutWidth;
            }
        }

        return maxW;
    }

    public Point getIntersectionPointNear(double x, double y) {
        List<Point> pts = getAllIntersectionPoints();
        double tol = panel.getClickDistanceTolerance() / panel.getZoomFactor();

        for (Point p : pts) {
            if (Math.hypot(x - p.x, y - p.y) < tol) {
                return p;
            }
        }

        return null;
    }

    private List<Point> getAllIntersectionPoints() {
        List<Point> result = new ArrayList<>();

        for (CutDTO c : panel.getController().getCuts()) {
            for (Point p : c.points) {
                if (!containsPoint(result, p)) {
                    result.add(new Point(p.x, p.y));
                }
            }
        }

        // Intersection entre coupes
        List<CutDTO> cuts = panel.getController().getCuts();

        for (int i = 0; i < cuts.size(); i++) {
            for (int j = i + 1; j < cuts.size(); j++) {
                List<Point> inters = intersectionBetweenTwoCuts(cuts.get(i), cuts.get(j));

                for (Point pi : inters) {
                    if (!containsPoint(result, pi)) {
                        result.add(pi);
                    }
                }
            }
        }

        // Ajout des coins du panneau
        PanneauDTO p = panel.getController().getPanneau();
        Point PA = new Point(0, 0);
        Point PB = new Point((int) p.longueur, 0);
        Point PC = new Point((int) p.longueur, (int) p.largeur);
        Point PD = new Point(0, (int) p.largeur);

        if (!containsPoint(result, PA)) {
            result.add(PA);
        }

        if (!containsPoint(result, PB)) {
            result.add(PB);
        }

        if (!containsPoint(result, PC)) {
            result.add(PC);
        }

        if (!containsPoint(result, PD)) {
            result.add(PD);
        }

        return result;
    }

    private List<Point> intersectionBetweenTwoCuts(CutDTO c1, CutDTO c2) {
        List<Point> inters = new ArrayList<>();
        List<Point> s1 = getCutSegmentsForHitTest(c1);
        List<Point> s2 = getCutSegmentsForHitTest(c2);

        for (int i = 0; i < s1.size(); i += 2) {
            Point A = s1.get(i);
            Point B = s1.get(i + 1);

            for (int j=0;j<s2.size();j+=2) {
                Point C = s2.get(j);
                Point D = s2.get(j + 1);
                Point I = segmentIntersection(A, B, C, D);

                if (I!=null && !containsPoint(inters,I)) {
                    inters.add(I);
                }
            }
        }
        return inters;
    }

    private Point segmentIntersection(Point A, Point B, Point C, Point D) {
        double denom = (D.y - C.y) * (B.x - A.x) - (D.x - C.x) * (B.y - A.y);

        if (denom == 0) {
            return null;
        }

        double ua = ((D.x - C.x) * (A.y - C.y) - (D.y - C.y) * (A.x - C.x)) / denom;
        double ub = ((B.x - A.x) * (A.y - C.y) - (B.y - A.y) * (A.x - C.x)) / denom;

        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            double x = A.x + ua * (B.x - A.x);
            double y = A.y + ua * (B.y - A.y);

            return new Point((int) Math.round(x), (int) Math.round(y));
        }

        return null;
    }
}
