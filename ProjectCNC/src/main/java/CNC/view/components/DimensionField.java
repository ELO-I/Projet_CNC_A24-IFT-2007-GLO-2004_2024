package CNC.view.components;

import javax.swing.*;
import java.awt.*;

public class DimensionField extends JPanel {
    private final JTextField textField;

    public DimensionField() {
        this.setLayout(new BorderLayout());

        textField = new JTextField();

        this.add(textField);
    }

    public double getValue() {
        return handleTextFieldFormat();
    }

    public void setValue(double value) {
        textField.setText(String.valueOf(value));
    }

    private double handleTextFieldFormat() {
        String text = textField.getText();
        double value;

        try {
            // Si les symboles ne sont pas présents
            if (text.indexOf('/') == -1 && text.indexOf('+') == -1) {
                value = Double.parseDouble(text);
            }
            // S'il n'y a pas de +
            else if (text.indexOf('+') == -1) {
                value = Double.parseDouble(text.substring(0, text.indexOf('/'))) / Double.parseDouble(text.substring(text.indexOf('/') + 1));
            }
            // S'il n'y a pas de /
            else if (text.indexOf('/') == -1) {
                value = Double.parseDouble(text.substring(0, text.indexOf('+'))) + Double.parseDouble(text.substring(text.indexOf('+') + 1));
            }
            // Si les symboles ne sont pas dans le bon ordre, throw
            else if (text.indexOf('/') < text.indexOf('+')) {
                value = Double.parseDouble(text);
            }
            // Si il y a un + et un /
            else {
                value = Double.parseDouble(text.substring(0, text.indexOf('+'))) + Double.parseDouble(text.substring(text.indexOf('+') + 1, text.indexOf('/'))) / Double.parseDouble(text.substring(text.indexOf('/') + 1));
            }
        }
        catch (NumberFormatException ignored) {
            value = -1;
        }

        return value;
    }
}
