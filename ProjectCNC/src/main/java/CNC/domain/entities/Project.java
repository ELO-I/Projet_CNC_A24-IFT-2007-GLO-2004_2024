package CNC.domain.entities;

import java.util.List;
import java.util.UUID;

public class Project {
    private UUID id;
    private String name;
    private Panneau panneau;
    private List<Cut> cuts;
    private Tool[] tools;

    public Project(String name, Panneau panneau, List<Cut> cuts, Tool[] tools) {
        setProjectId(UUID.randomUUID());
        setName(name);
        setPanneau(panneau);
        setCuts(cuts);
        setTools(tools);
    }

    public Project(UUID id, String name, Panneau panneau, List<Cut> cuts, Tool[] tools) {
        setProjectId(id);
        setName(name);
        setPanneau(panneau);
        setCuts(cuts);
        setTools(tools);
    }

    public UUID getId() {
        return id;
    }

    private void setProjectId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public Panneau getPanneau() {
        return panneau;
    }

    private void setPanneau(Panneau panneau) {
        this.panneau = panneau;
    }

    public List<Cut> getCuts() {
        return cuts;
    }

    private void setCuts(List<Cut> cuts) {
        this.cuts = cuts;
    }

    public void addCut(Cut cut) {
        cuts.add(cut);
    }

    public void deleteCut(UUID uuid) {
        cuts.removeIf(cut -> cut.getIdCoupe().equals(uuid));
    }

    public Tool[] getTools() {
        return tools;
    }

    public void setTools(Tool[] tools) {
        this.tools = tools;
    }

    public void addTool(Tool tool) {
        for(int i = 0; i < tools.length; i++) {
            if(tools[i] == null) {
                tools[i] = tool;

                return;
            }
        }
    }

    public void deleteTool(int storePosition) {
        tools[storePosition] = null;
    }
}
