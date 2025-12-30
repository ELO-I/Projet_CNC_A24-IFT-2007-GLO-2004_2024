package CNC.controller;

import CNC.controller.converter.CutConverter;
import CNC.controller.converter.PanneauConverter;
import CNC.controller.converter.ToolConverter;
import CNC.domain.Memento;
import CNC.domain.Snapshot;
import CNC.domain.entities.*;
import CNC.view.MainView;
import CNC.view.dto.CutDTO;
import CNC.view.dto.PanneauDTO;
import CNC.view.dto.ToolDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Controller {
    private final CutConverter cutConverter;
    private final ToolConverter toolConverter;
    private final PanneauConverter panneauConverter;
    private Project project;
    private final Stack<Memento> undoStack = new Stack<>();
    private final Stack<Memento> redoStack = new Stack<>();
    private File currentFile;

    public Controller() {
        cutConverter = new CutConverter();
        toolConverter = new ToolConverter();
        panneauConverter = new PanneauConverter();

        createProject("", panneauConverter.ConvertToDTO(Panneau.empty()));
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(new Memento(createSnapshot()));
            restoreState(undoStack.pop());
        }
    }

    public void saveState() {
        undoStack.push(new Memento(createSnapshot()));
        redoStack.clear();
    }

    public void restoreState(Memento memento) {
        this.project = parseSnapshot(memento.getState()).getProject();
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(new Memento(createSnapshot()));
            restoreState(redoStack.pop());
        }
    }

    private String createSnapshot() {
        return Snapshot.serialize(project);
    }

    private Snapshot parseSnapshot(String state) {
        return Snapshot.deserialize(state);
    }

    public PanneauDTO getPanneau() {
        return panneauConverter.ConvertToDTO(project.getPanneau() == null ? Panneau.empty() : project.getPanneau());
    }

    public void deleteCut(UUID id) {
        saveState();

        project.deleteCut(id);
    }

    public List<CutDTO> getCuts() {
        return cutConverter.convertToDTOList(project.getCuts());
    }

    public void createCut(CutDTO dto) {
        saveState();

        // Donner un nom unique en fonction du type
        dto.name = createCutName(dto.type);

        project.addCut(cutConverter.convertToEntity(dto));
    }

    private String createCutName(CNC.domain.entities.CutType type) {
        int count = 0;

        for (Cut c : project.getCuts()) {
            if (c.getType() == type) {
                count++;
            }
        }

        return type + " " + (count + 1);
    }

    public CutDTO getCutDTOById(UUID id) {
        return getCutById(id) != null ? cutConverter.convertToDTO(Objects.requireNonNull(getCutById(id))) : null;
    }

    public CutDTO getCutDTOByName(String name) {
        return getCutByName(name) != null ? cutConverter.convertToDTO(Objects.requireNonNull(getCutByName(name))) : null;
    }

    private Cut getCutById(UUID id) {
        for (Cut cut : project.getCuts()) {
            if (cut.getIdCoupe().equals(id)) {
                return cut;
            }
        }

        return null;
    }

    private Cut getCutByName(String name) {
        for (Cut cut : project.getCuts()) {
            if (cut.getName().equals(name)) {
                return cut;
            }
        }

        return null;
    }

    public void updateCut(CutDTO dto) {
        saveState();
        updateCutNoSaveState(dto);
    }

    public void updateCutNoSaveState(CutDTO dto) {
        Cut cut = getCutById(dto.id);

        if (cut != null) {
            // Vérifie si le nouveau nom est déjà pris par une autre coupe
            for (CutDTO existingCutDTO : getCuts()) {
                if (dto.name.equals(existingCutDTO.name) && !existingCutDTO.id.equals(dto.id)) {
                    dto.name = cut.getName();
                    break;
                }
            }

            cut.setName(dto.name);
            cut.setType(dto.type);
            cut.setDepth(dto.depth);
            cut.setTool(dto.toolName);
            cut.setPoints(dto.points);
            cut.setChildren(dto.children);
            cut.setValid(dto.valid);
        }
    }

    public ToolDTO[] getTools() {
        return toolConverter.convertToDTOArray(project.getTools());
    }

    public ToolDTO getToolByName(String name) {
        for (ToolDTO tool : getTools()) {
            if (tool.name.equals(name)) {
                return tool;
            }
        }

        return null;
    }

    public void addTool(ToolDTO tool) {
        saveState();
        addToolNoSave(tool);
    }

    public void addToolNoSave(ToolDTO tool) {
        for (ToolDTO toolDTO : getTools()) {
            if (toolDTO != null && tool.name.equals(toolDTO.name)) {
                return;
            }
        }

        int storePosition = 0;

        for (int i = 0; i < getTools().length; i++) {
            if (getTools()[i] == null) {
                storePosition = i + 1;
                break;
            }
        }

        tool.storePosition = storePosition;

        project.addTool(toolConverter.convertToEntity(tool));
    }

    public void deleteTool(int storePosition) {
        saveState();

        project.deleteTool(storePosition - 1);
    }

    public void createProject(String name, PanneauDTO panelDto) {
        List<Cut> emptyList = new ArrayList<>();
        Tool[] emptyArray = new Tool[12];

        if (panelDto.longueur == 0 && panelDto.largeur == 0) {
            project = new Project(name, Panneau.empty(), emptyList, emptyArray);
        }
        else {
            project = new Project(name, panneauConverter.ConvertToEntity(panelDto), emptyList, emptyArray);
        }
    }

    public void openProject(MainView view) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open CNC File");
        int userSelection = fileChooser.showOpenDialog(view);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                Gson gson = new Gson();
                CNCFileData fileData = gson.fromJson(reader, CNCFileData.class);

                createProjectFromFileData(fileData);
                view.refreshView();
                JOptionPane.showMessageDialog(view, "Project loaded successfully!");
            }
            catch (IOException | JsonSyntaxException ex) {
                JOptionPane.showMessageDialog(
                        view,
                        "Error loading file:\n" + ex.getMessage(),
                        "Load Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void createProjectFromFileData(CNCFileData fileData) {
        project = new Project(
                fileData.getId(),
                fileData.getName(),
                fileData.getPanneau(),
                fileData.getCuts(),
                fileData.getTools()
        );
    }

    public void saveProject(MainView view) {
        if (currentFile != null) {
            saveToFile(view, currentFile);
        }
        else {
            saveProjectAs(view);
        }
    }

    public void saveProjectAs(MainView view) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CNC File");
        fileChooser.setSelectedFile(new File("project." + CNCFileData.EXTENSION.toLowerCase()));

        if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            currentFile = ensureFileExtension(fileChooser.getSelectedFile());
            saveToFile(view, currentFile);
        }
    }

    public void exportGCodeFile() {
        if(!isProjectReadyForGCodeExport())
            JOptionPane.showMessageDialog(null, "Unable to export GCode file:\nProject contains invalid cuts", "Error", JOptionPane.ERROR_MESSAGE);
        else {
            StringBuilder gCode = new StringBuilder();

            gCode.append("G21 ; Set units to millimeters\n");
            gCode.append("G90 ; Absolute positioning\n\n");

            for (Tool tool : project.getTools()) {
                if (tool != null) {
                    gCode.append("; --- Using Tool: ").append(tool.getName()).append(" ---\n");
                    gCode.append("M6 T").append(tool.getStorePosition()).append(" ; Tool Change\n");
                    gCode.append("G0 Z5 ; Lift tool to safe height\n\n");

                    for (Cut cut : project.getCuts()) {
                        if (cut.getPoints().size() < 2) {
                            gCode.append("; Skipped cut due to insufficient points\n");
                            continue;
                        }

                        Point start = cut.getPoints().get(0);
                        Point end = cut.getPoints().get(1);

                        if (cut.getType() == CutType.HORIZONTAL || cut.getType() == CutType.VERTICAL) {
                            gCode.append("G0 X").append(start.getX()).append(" Y").append(start.getY())
                                    .append(" ; Move to start position\n");
                            gCode.append("G1 Z-1.0 F100 ; Lower tool to cutting depth\n");
                            gCode.append("G1 X").append(end.getX()).append(" Y").append(end.getY())
                                    .append(" F300 ; Perform cut\n");
                            gCode.append("G0 Z5 ; Lift tool to safe height\n\n");
                        } else if (cut.getType() == CutType.L) {
                            if (cut.getPoints().size() == 3) {
                                Point intersection = cut.getPoints().get(0);
                                Point horizontalEnd = cut.getPoints().get(1);
                                Point verticalEnd = cut.getPoints().get(2);

                                gCode.append("G0 X").append(intersection.getX()).append(" Y").append(intersection.getY())
                                        .append(" ; Move to intersection point\n");
                                gCode.append("G1 Z-1.0 F100 ; Lower tool to cutting depth\n");
                                gCode.append("G1 X").append(horizontalEnd.getX()).append(" Y").append(horizontalEnd.getY())
                                        .append(" F300 ; Perform horizontal cut\n");
                                gCode.append("G0 Z5 ; Lift tool to safe height\n\n");

                                gCode.append("G0 X").append(intersection.getX()).append(" Y").append(intersection.getY())
                                        .append(" ; Return to intersection point\n");
                                gCode.append("G1 Z-1.0 F100 ; Lower tool to cutting depth\n");
                                gCode.append("G1 X").append(verticalEnd.getX()).append(" Y").append(verticalEnd.getY())
                                        .append(" F300 ; Perform vertical cut\n");
                                gCode.append("G0 Z5 ; Lift tool to safe height\n\n");
                            }
                        } else if (cut.getType() == CutType.RECTANGULAR || cut.getType() == CutType.BORDER_CUT) {
                            if (cut.getPoints().size() == 4) {
                                Point upperLeft = cut.getPoints().get(0);
                                Point upperRight = cut.getPoints().get(1);
                                Point lowerRight = cut.getPoints().get(2);
                                Point lowerLeft = cut.getPoints().get(3);

                                gCode.append("G0 X").append(upperLeft.getX()).append(" Y").append(upperLeft.getY())
                                        .append(" ; Move to upper-left corner\n");
                                gCode.append("G1 Z-1.0 F100 ; Lower tool to cutting depth\n");

                                gCode.append("G1 X").append(upperRight.getX()).append(" Y").append(upperRight.getY())
                                        .append(" F300 ; Cut to upper-right corner\n");
                                gCode.append("G1 X").append(lowerRight.getX()).append(" Y").append(lowerRight.getY())
                                        .append(" F300 ; Cut to lower-right corner\n");
                                gCode.append("G1 X").append(lowerLeft.getX()).append(" Y").append(lowerLeft.getY())
                                        .append(" F300 ; Cut to lower-left corner\n");
                                gCode.append("G1 X").append(upperLeft.getX()).append(" Y").append(upperLeft.getY())
                                        .append(" F300 ; Return to upper-left corner\n");

                                gCode.append("G0 Z5 ; Lift tool to safe height\n\n");
                            }
                        }
                    }
                }
            }

            gCode.append("M30 ; End of program\n");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save G-code File");
            fileChooser.setSelectedFile(new File("instructions.gcode"));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                try (FileWriter writer = new FileWriter(fileToSave)) {
                    writer.write(gCode.toString());
                    JOptionPane.showMessageDialog(null, "G-code file saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error saving G-code file:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void saveToFile(MainView view, File file) {
        CNCFileData fileData = new CNCFileData(
                project.getId(),
                project.getName(),
                project.getPanneau(),
                project.getCuts(),
                project.getTools()
        );

        try (FileWriter writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            gson.toJson(fileData, writer);
            JOptionPane.showMessageDialog(view, "File saved successfully:\n" + file.getAbsolutePath());
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    view,
                    "Error saving file:\n" + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private File ensureFileExtension(File file) {
        String lowerCaseExtension = "." + CNCFileData.EXTENSION.toLowerCase();

        if (!file.getName().toLowerCase().endsWith(lowerCaseExtension)) {
            return new File(file.getAbsolutePath() + lowerCaseExtension);
        }

        return file;
    }

    private boolean isProjectReadyForGCodeExport() {
        for (Cut cut : project.getCuts())
            if(!cut.isValid())
                return false;
        return true;
    }
}
