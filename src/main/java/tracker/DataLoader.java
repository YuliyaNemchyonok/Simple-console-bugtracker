package tracker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

class DataLoader {
    private static final Logger log = LogManager.getLogger(DataLoader.class.getName());

    ArrayList<User> users = new ArrayList<>();
    ArrayList<Project> projects = new ArrayList<>();
    ArrayList<Issue> issues = new ArrayList<>();

    private HashMap<Integer,User> userHashMap = new HashMap<>();
    private HashMap<Integer,Project> projectHashMap =  new HashMap<>();

    DataLoader(String fileForUsers, String fileForProjects, String fileForIssues, String fileForProjectUserRelation) {
        log.info("Start reading data from files '" + fileForUsers + "', '" + fileForProjects + "', '" + fileForIssues + "', '" + fileForProjectUserRelation + "'");
        log.debug("Start constructor");
        try {
            Reader in = new FileReader(fileForUsers);
            log.debug("Start reading file '" + fileForUsers + "'");
            Iterable<CSVRecord> userRecords = CSVFormat.DEFAULT.withHeader("id","name","login","password").parse(in);
            userRecords.iterator().next();
            for (CSVRecord record : userRecords) {
                int id = Integer.parseInt(record.get("id"));
                String name = record.get("name");
                String login = record.get("login");
                String password = record.get("password");
                users.add(new User(id, name, login, password));
                log.debug("Add User with id '" + id + "', name '" + name + "', login '" + login + "', password");
            }
            log.debug("Reading file '" + fileForUsers + "' completed");
        } catch (FileNotFoundException fnfe) {
            log.error("File '" + fileForUsers + "'not found");
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            log.error("IOExeption with file '" + fileForUsers + "'");
            ioe.printStackTrace();
        }

        ArrayList<ProjectRow> projectRows = new ArrayList<>();
        log.debug("Create ArrayList<ProjectRow> projectRows");
        try {
            Reader in = new FileReader(fileForProjects);
            log.debug("Start reading file '" + fileForProjects + "'");
            Iterable<CSVRecord> projectRecords = CSVFormat.DEFAULT.withHeader("id","name","description","ownerId").parse(in);
            projectRecords.iterator().next();
            for (CSVRecord record : projectRecords) {
                int ID = Integer.parseInt(record.get("id"));
                String name = record.get("name");
                String description = record.get("description");
                int ownerId = Integer.parseInt(record.get("ownerId"));
                projectRows.add(new ProjectRow(ID, name, description, ownerId));
                log.debug("Add ProjectRow with id '" + ID + "', name '" + name + "', description '" + description + "', ownerId '" + ownerId + "'");
            }
            log.debug("Reading file '" + fileForProjects + "' completed");
        } catch (FileNotFoundException fnfe) {
            log.error("File '" + fileForProjects + "'not found");
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            log.error("IOExeption with file '" + fileForProjects + "'");
            ioe.printStackTrace();
        }

        try {
            Reader in = new FileReader(fileForProjectUserRelation);
            log.debug("Start reading file '" + fileForProjectUserRelation + "'");
            Iterable<CSVRecord> projectUserRelationRecords = CSVFormat.DEFAULT.withHeader("userId","projectId").parse(in);
            projectUserRelationRecords.iterator().next();
            ArrayList<ProjectUserRelationRow> projectUserRelationRows = new ArrayList<>();
            log.debug("Create ArrayList<ProjectUserRelationRow> projectUserRelationRows");
            for (CSVRecord record : projectUserRelationRecords) {
                int userId = Integer.parseInt(record.get("userId"));
                int projectId = Integer.parseInt(record.get("projectId"));
                projectUserRelationRows.add(new ProjectUserRelationRow(userId,projectId));
                log.debug("Add ProjectUserRelationRow with userId '" + userId + "', projectId '" + projectId + "'");
            }
            log.debug("Reading file '" + fileForProjectUserRelation + "' completed");
            addMembersToProjects(users, projectRows, projectUserRelationRows);
        } catch (FileNotFoundException fnfe) {
            log.error("File '" + fileForProjectUserRelation + "'not found");
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            log.error("IOExeption with file '" + fileForProjectUserRelation + "'");
            ioe.printStackTrace();
        }

        try {
            Reader in = new FileReader(fileForIssues);
            log.debug("Start reading file '" + fileForIssues + "'");
            Iterable<CSVRecord> issueRecords = CSVFormat.DEFAULT.withHeader("id","title","projectId","ownerId","assignerId","description","creationTime","status").parse(in);
            issueRecords.iterator().next();
            for (CSVRecord record : issueRecords) {
                int ID = Integer.parseInt(record.get("id"));
                String title = record.get("title");
                int projectId = Integer.parseInt(record.get("projectId"));
                int ownerId = Integer.parseInt(record.get("ownerId"));
                int assignerId = Integer.parseInt(record.get("assignerId"));
                String description = record.get("description");
                LocalDateTime creationTime = LocalDateTime.parse(record.get("creationTime"));
                Status status = Status.valueOf(record.get("status"));
                issues.add(new Issue(ID,title, projectHashMap.get(projectId),userHashMap.get(ownerId),userHashMap.get(assignerId),description,creationTime,status));
                log.debug("Add Issue with id '" + ID + "', title '" + title + "', projectId '" + projectId + "', ownerId '" + ownerId + "', assignerId '" + assignerId + "', status '" + status.toString() + "'");
            }
            log.debug("Reading file '" + fileForIssues + "' completed");
        } catch (FileNotFoundException fnfe) {
            log.error("File '" + fileForIssues + "'not found");
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            log.error("IOExeption with file '" + fileForIssues + "'");
            ioe.printStackTrace();
        }
        log.info("Data from files read");
    }

    private void addMembersToProjects(ArrayList<User> users, ArrayList<ProjectRow> projectRows, ArrayList<ProjectUserRelationRow> projectUserRelationRows) {
        log.debug("Method addMembersToProjects invoked");
        for (User user : users) {
            userHashMap.put(user.getID(), user);
            log.debug("Put to userHashMap user with id '" + user.getID() + "'");
        }
        for (ProjectRow projectRow : projectRows) {
            Project project = new Project(projectRow.getID(),projectRow.getName(), projectRow.getDescription(), userHashMap.get(projectRow.getOwnerId()), new ArrayList<>());
            projectHashMap.put(projectRow.getID(), project);
            log.debug("Put to projectHashMap project with id '" + project.getID() + "'");
            projects.add(project);
            log.debug("Add Project with id '" + project.getID() + "', name" + project.getName() + "', description '" + project.getDescription() + "', owner login '" + project.getOwnerLogin() + "'");
        }
        for (ProjectUserRelationRow projectUserRelationRow : projectUserRelationRows) {
            User user = userHashMap.get(projectUserRelationRow.getUserId());
            if (user==null) {
                log.warn("In projectUserRelation file invalid userId. There is no user with id '" + projectUserRelationRow.getUserId() + "'");
                continue;
            }
            log.debug("Get from userHashMap user with id '" + user.getID() + "'");

            Project project = projectHashMap.get(projectUserRelationRow.getProjectId());
            if (project==null) {
                log.warn("In projectUserRelation file invalid projectId. There is no project with id '" + projectUserRelationRow.getProjectId() + "'");
                continue;
            }
            log.debug("Get from projectHashMap project with id '" + project.getID() + "'");

            project.addMember(user);
            log.debug("Add to the project " + project.getID() + " - '" + project.getName() + "' member " + user.getID() + " - '" + user.getLogin() + "'");

        }
        log.debug("Method addMembersToProjects completed");
    }

}
