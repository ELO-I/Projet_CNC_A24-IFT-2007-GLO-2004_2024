package CNC.view.Enum;

public enum MeasurementUnit {
    METRIC("mm"),
    IMPERIAL("in");

    private final double DIFFERENCE = 25.4D;
    private final String ABBREVIATION;

    MeasurementUnit(String abbreviation) {
        this.ABBREVIATION = abbreviation;
    }

    public double convertToMetric(double measurement) {
        return switch (this) {
            case METRIC -> measurement;
            case IMPERIAL -> computeImperialToMetric(measurement);
        };
    }

    public double convertToImperial(double measurement) {
        return switch (this) {
            case METRIC -> computeMetricToImperial(measurement);
            case IMPERIAL -> measurement;
        };
    }

    public double computeMetricToImperial(double measurement) {
        return measurement / DIFFERENCE;
    }

    public double computeImperialToMetric(double measurement) {
        return measurement * DIFFERENCE;
    }

    public String getABBREVIATION() {
        return ABBREVIATION;
    }

    public static MeasurementUnit getUnitFromAbbreviation(String abbreviation) {
        return switch (abbreviation) {
            case "mm" -> METRIC;
            case "in" -> IMPERIAL;
            default -> null;
        };
    }
}
