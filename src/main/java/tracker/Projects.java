package tracker;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Projects implements ProjectService {
    private ArrayList<Project> projects;
    private int count;

    Projects(ArrayList<Project> projects) {
        this.projects = projects;
        int max = 0;
        for (Project p : projects) {
            if (p.getID()>max) {
                max = p.getID();
            }
        }
        this.count = max;
    }


    @Override
    public ArrayList<Project> getListOfProjects() {
        return projects;
    }

    @Override
    public Project addProject(String name, String description, User owner, ArrayList<User> members) {
        int id = count++;
        Project project = new Project(id, name, description, owner, members);
        projects.add(project);
        return project;
    }

    @Override
    public Project findProjectByName(String name) {
        for (Project p : projects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Project findProjectById(int id) {
        for (Project p : projects) {
            if (p.getID()==id) {
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean removeProject(Project project) {
        if (projects.contains(project)) {
            projects.remove(project);
            return true;
        }
        return false;
    }

    @Override
    public void clearProjectList() {
        projects.clear();
    }

    @Override
    public int countProjects() {
        return projects.size();
    }

}
