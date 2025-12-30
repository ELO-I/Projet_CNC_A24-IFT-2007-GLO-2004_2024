package CNC.controller;

import CNC.domain.entities.Cut;
import CNC.domain.entities.Panneau;
import CNC.domain.entities.Tool;

import java.util.List;
import java.util.UUID;

public class CNCFileData {
    private final UUID ID;
    private String name;
    private Panneau panneau;
    private List<Cut> cuts;
    private final Tool[] tools;
    public static final String EXTENSION = "CNC";

    public CNCFileData(UUID id, String name, Panneau panneau, List<Cut> cuts, Tool[] tools) {
        this.ID = id;
        this.name = name;
        this.panneau = panneau;
        this.cuts = cuts;
        this.tools = tools;
    }

    public UUID getId() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Panneau getPanneau() {
        return panneau;
    }

    public List<Cut> getCuts() {
        return cuts;
    }

    public Tool[] getTools() {
        return tools;
    }
}
