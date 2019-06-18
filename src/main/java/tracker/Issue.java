package tracker;

import java.time.LocalDateTime;

enum Status {
    TODO, IN_PROGRESS, DONE
}

public class Issue {
    private final int ID;
    private String title;
    private Project project;
    private User owner;
    private User assigner;
    private String description;
    private final LocalDateTime creationTime;
    private Status status;

    public Issue(int ID, String title, Project project, User owner, User assigner, String description, LocalDateTime creationTime, Status status) {
        this.ID = ID;
        this.title = title;
        this.project = project;
        this.owner = owner;
        this.assigner = assigner;
        this.description = description;
        this.creationTime = creationTime;
        this.status = status;
    }

    public int getID() {
        return this.ID;
    }

    public String getTitle() {
        return this.title;
    }

    public int getProjectId() {
        return project.getID();
    }

    public Project getProject() {
        return project;
    }

    public int getOwnerId() {
        return owner.getID();
    }

    public User getOwner() {
        return owner;
    }

    public int getAssignerId() {
        return assigner.getID();
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setAssigner(User assigner) {
        this.assigner = assigner;
    }

    public User getAssigner() {
        return assigner;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return this.ID + "," + this.title + "," + this.project.getID() + "," + this.owner.getID() + "," + this.assigner.getID() + "," + this.description + "," + this.creationTime.toString() + "," + this.status;
    }
}
