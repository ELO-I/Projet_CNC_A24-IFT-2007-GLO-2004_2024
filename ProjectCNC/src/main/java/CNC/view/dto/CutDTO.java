package CNC.view.dto;

import CNC.domain.entities.CutType;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class CutDTO {
    public UUID id;
    public String name;
    public CutType type;
    public double depth;
    public String toolName;
    public List<Point> points;
    public List<String> children;
    public boolean valid;

    public CutDTO(UUID id, String name, CutType type, double depth, String toolName, List<Point> points, List<String> children, boolean valid) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.depth = depth;
        this.toolName = toolName;
        this.points = points;
        this.children = children;
        this.valid = valid;
    }

    public CutDTO() {

    }
}
