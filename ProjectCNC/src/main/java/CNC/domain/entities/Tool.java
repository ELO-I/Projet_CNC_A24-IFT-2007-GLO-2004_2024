package CNC.domain.entities;

public class Tool {
    private String name;
    private double cutWidth;
    private int storePosition;

    public Tool(String name, double cutWidth, int storePosition) {
        setName(name);
        setCutWidth(cutWidth);
        setStorePosition(storePosition);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public double getCutWidth() {
        return cutWidth;
    }

    private void setCutWidth(double cutWidth) {
        this.cutWidth = cutWidth;
    }

    public int getStorePosition() {
        return storePosition;
    }

    private void setStorePosition(int storePosition) {
        this.storePosition = storePosition;
    }
}
