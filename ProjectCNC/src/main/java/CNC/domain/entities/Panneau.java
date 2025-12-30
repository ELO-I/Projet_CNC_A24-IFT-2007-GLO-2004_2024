package CNC.domain.entities;

public class Panneau {
    private double longueur;
    private double largeur;
    private double epaisseur;

    public static Panneau empty(){
        return new Panneau(914.4,1129.2, 0);
    }

    public Panneau(double longueur, double largeur, double epaisseur) {
        setLongueur(longueur);
        setLargeur(largeur);
        setEpaisseur(epaisseur);
    }

    public double getLongueur() {
        return longueur;
    }

    private void setLongueur(double longueur) {
        this.longueur = longueur;
    }

    public double getLargeur() {
        return largeur;
    }

    private void setLargeur(double largeur) {
        this.largeur = largeur;
    }

    public double getEpaisseur() {
        return epaisseur;
    }

    private void setEpaisseur(double epaisseur) {
        this.epaisseur = epaisseur;
    }
}
