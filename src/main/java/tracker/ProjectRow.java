package tracker;

import java.util.ArrayList;
import java.util.List;

public class ProjectRow {
    private int ID;
    private String name;
    private String description;
    private int ownerId;


    public ProjectRow(int ID, String name, String description, int ownerId) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
