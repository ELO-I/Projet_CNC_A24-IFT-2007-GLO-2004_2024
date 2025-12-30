package CNC.domain;

import CNC.domain.entities.Project;
import com.google.gson.Gson;

public class Snapshot {
    private final Project project;

    public Snapshot(Project project) {
        this.project = deepCopy(project); // Faire une copie profonde
    }

    public Project getProject() {
        return project;
    }

    public static String serialize(Project project) {
        return new Gson().toJson(project);
    }

    public static Snapshot deserialize(String state) {
        Project project = new Gson().fromJson(state, Project.class);
        return new Snapshot(project);
    }

    private Project deepCopy(Project project) {
        String serialized = serialize(project);
        return new Gson().fromJson(serialized, Project.class);
    }
}

