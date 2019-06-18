package tracker;

public class ProjectUserRelationRow {
    int userId;
    int projectId;

    public ProjectUserRelationRow(int userID, int projectID) {
        userId = userID;
        projectId = projectID;
    }

    public int getUserId() {
        return userId;
    }

    public int getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return userId + ", " + projectId;
    }
}
