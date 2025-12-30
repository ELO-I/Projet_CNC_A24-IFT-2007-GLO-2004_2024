package CNC.view.dto;

public class ToolDTO {
    public String name;
    public double cutWidth;
    public int storePosition;

    public ToolDTO(String name, double cutWidth, int storePosition) {
        this.name = name;
        this.cutWidth = cutWidth;
        this.storePosition = storePosition;
    }
}
