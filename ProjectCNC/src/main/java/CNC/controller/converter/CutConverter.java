package CNC.controller.converter;

import CNC.domain.entities.Cut;
import CNC.view.dto.CutDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CutConverter {
    public Cut convertToEntity(CutDTO dto) {
        return new Cut(
                dto.type,
                dto.name,
                dto.depth,
                dto.toolName,
                dto.points,
                dto.children,
                dto.valid
        );
    }

    public CutDTO convertToDTO(Cut cut) {
        return new CutDTO(
                cut.getIdCoupe(),
                cut.getName(),
                cut.getType(),
                cut.getDepth(),
                cut.getTool(),
                cut.getPoints(),
                cut.getChildren(),
                cut.isValid()
        );
    }

    public List<CutDTO> convertToDTOList(List<Cut> cuts) {
        if (cuts == null) {
            return new ArrayList<>();
        }

        return cuts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
