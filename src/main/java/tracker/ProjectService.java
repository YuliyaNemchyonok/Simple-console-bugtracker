package tracker;

import java.util.ArrayList;

public interface ProjectService {
    ArrayList<Project> getListOfProjects();

    Project addProject(String name, String description, User member, ArrayList<User> members);

    Project findProjectByName(String name);

    Project findProjectById(int id);

    boolean removeProject(Project project);

    void clearProjectList();

    int countProjects();
}
