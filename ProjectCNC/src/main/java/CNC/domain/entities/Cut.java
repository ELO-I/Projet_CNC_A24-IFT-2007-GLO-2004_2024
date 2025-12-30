package CNC.domain.entities;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class Cut {
    private UUID idCoupe;
    private String name;
    private CutType type;
    private double depth;
    private String toolName;
    private List<Point> points;
    private List<String> children;
    private boolean valid;

    public Cut(CutType type, String name, double depth, String toolName, List<Point> points, List<String> children, boolean valid) {
        setIdCoupe();
        setName(name);
        setType(type);
        setDepth(depth);
        setTool(toolName);
        setPoints(points);
        setChildren(children);
        setValid(valid);
    }

    public UUID getIdCoupe() {
        return idCoupe;
    }

    private void setIdCoupe() {
        this.idCoupe = UUID.randomUUID();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public CutType getType() {
        return type;
    }

    public void setType(CutType type) {
        this.type = type;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public String getTool() {
        return toolName;
    }

    public void setTool(String toolName) {
        this.toolName = toolName;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
