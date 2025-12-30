package CNC.controller.converter;

import CNC.domain.entities.Tool;
import CNC.view.dto.ToolDTO;

public class ToolConverter {
    public Tool convertToEntity(ToolDTO dto){
        return new Tool(
                dto.name,
                dto.cutWidth,
                dto.storePosition
        );
    }

    public ToolDTO convertToDTO(Tool tool){
        return new ToolDTO(
                tool.getName(),
                tool.getCutWidth(),
                tool.getStorePosition()
        );
    }

    public ToolDTO[] convertToDTOArray(Tool[] tools) {
        ToolDTO[] toolsDTO = new ToolDTO[12];

        for (int i = 0; i < tools.length; i++) {
            if (tools[i] != null) {
                toolsDTO[i] = (convertToDTO(tools[i]));
            }
        }

        return toolsDTO;
    }
}
