package CNC.controller.converter;

import CNC.domain.entities.Panneau;
import CNC.view.dto.PanneauDTO;

public class PanneauConverter {
    public PanneauDTO ConvertToDTO(Panneau panel) {
        return new PanneauDTO(panel.getLongueur(), panel.getLargeur(), panel.getEpaisseur());
    }

    public Panneau ConvertToEntity(PanneauDTO dto){
        return new Panneau(dto.longueur, dto.largeur, dto.epaisseur);
    }
}
