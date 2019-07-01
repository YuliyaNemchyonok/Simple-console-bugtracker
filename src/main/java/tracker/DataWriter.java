package tracker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataWriter {
    private static final Logger log = LogManager.getLogger(DataLoader.class.getName());

    ArrayList<User> users;
    ArrayList<Project> projects;
    ArrayList<Issue> issues;

    String fileForUsers;
    String fileForProjects;
    String fileForIssues;
    String fileForProjectUserRelation;

    DataWriter (ArrayList<User> usersList, ArrayList<Project> projectsList, ArrayList<Issue> issuesList, String fileForUsers, String fileForProjects, String fileForIssues, String fileForProjectUserRelation) {
        log.debug("Start constructor");
        this.users = usersList;
        this.projects = projectsList;
        this.issues = issuesList;

        this.fileForUsers = fileForUsers;
        this.fileForProjects = fileForProjects;
        this.fileForIssues = fileForIssues;
        this.fileForProjectUserRelation = fileForProjectUserRelation;

        log.debug("Constructor complete");
    }


    public void writeDataToFiles () {
        log.info("Start saving data from app to the files '" + fileForUsers + "', '" + fileForProjects + "', '" + fileForProjectUserRelation + "', '" + fileForIssues + "'");
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForUsers), CSVFormat.DEFAULT.withHeader("id","name","login","password"))) {
            log.debug("Start writing in file '" + fileForUsers + "'");
            for (User user : users) {
                printer.printRecord(user.getID(),user.getName(),user.getLogin(),user.getPassword());
                log.debug("Print User with id '" + user.getID() + "', name '" + user.getName() + "', login '" + user.getLogin() + "', password");
            }
            log.debug("Writing in file '" + fileForUsers + "' complete");
        } catch (IOException ex) {
            log.error("Exception with writing list of Users into file '" + fileForUsers + "'");
            ex.printStackTrace();
        }

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForProjects), CSVFormat.DEFAULT.withHeader("id","name","description","ownerId"))) {
            log.debug("Start writing in file '" + fileForProjects + "'");
            for (Project project : projects) {
                printer.printRecord(project.getID(),project.getName(),project.getDescription(),project.getOwnerId());
                log.debug("Print Project with id '" + project.getID() + "', name '" + project.getName() + "', description, ownerId '" + project.getOwnerId() + "'");
            }
            log.debug("Writing in file '" + fileForProjects + "' complete");
        } catch (IOException ex) {
            log.error("Exception with writing list of Projects into file '" + fileForProjects + "'");
            ex.printStackTrace();
        }

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForIssues), CSVFormat.DEFAULT.withHeader("id","title","projectId","ownerId","assignerId","description","creationTime","status"))) {
            log.debug("Start writing in file '" + fileForIssues + "'");
            for (Issue issue : issues) {
                printer.print(String.valueOf(issue.getID()));
                printer.print(issue.getTitle());
                printer.print(String.valueOf(issue.getProjectId()));
                printer.print(String.valueOf(issue.getOwnerId()));
                printer.print(String.valueOf(issue.getAssignerId()));
                printer.print(issue.getDescription());
                printer.print(issue.getCreationTime());
                printer.print(issue.getStatus());
                printer.println();
                log.debug("Print Issue with id '" + issue.getID() + "', title '" + issue.getTitle() + "', projectId '" + issue.getProjectId() + "', ownerId '" + issue.getOwnerId() + "', " +
                        "assignerId '" + issue.getAssignerId() + "', description, creation time " + issue.getCreationTime() + ", status " + issue.getStatus().toString());
            }
            log.debug("Writing in file '" + fileForIssues + "' complete");
        } catch (IOException ex) {
            log.error("Exception with writing list of Issue into file '" + fileForIssues + "'");
            ex.printStackTrace();
        }

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForProjectUserRelation), CSVFormat.DEFAULT.withHeader("userId","projectId"))) {
            log.debug("Start writing in file '" + fileForProjectUserRelation + "'");
            for (Project project : projects) {
                for (User user : project.getMembers()) {
                    printer.printRecord(user.getID(),project.getID());
                    log.debug("Print ProjectUserRelationRow with userId " + user.getID() + ", projectId " + project.getID());
                }
            }
            log.debug("Writing in file '" + fileForProjectUserRelation + "' complete");
        } catch (IOException ex) {
            log.error("Exception with writing list of ProjectUserRelations into file '" + fileForProjectUserRelation + "'");
            ex.printStackTrace();
        }
        log.info("Data saved into the files.");
    }
}
