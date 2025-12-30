package CNC.view.dto;

public class PanneauDTO {
    public final double longueur;
    public final double largeur;
    public final double epaisseur;

    public PanneauDTO(double longueur, double largeur, double epaisseur) {
        this.longueur = longueur;
        this.largeur = largeur;
        this.epaisseur = epaisseur;
    }
}
