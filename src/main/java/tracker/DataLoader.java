package tracker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class DataLoader {
    ArrayList<ProjectRow> projectRows = new ArrayList<>();
    ArrayList<ProjectUserRelationRow> projectUserRelationRows = new ArrayList<>();

    ArrayList<User> users = new ArrayList<>();
    ArrayList<Project> projects = new ArrayList<>();
    ArrayList<Issue> issues = new ArrayList<>();

    HashMap<Integer,User> userHashMap = new HashMap<>();
    HashMap<Integer,Project> projectHashMap =  new HashMap<>();

    DataLoader(String fileForUsers, String fileForProjects, String fileForIssues, String fileForProjectUserRelation) {
        try {
            Reader in = new FileReader(fileForUsers);
            Iterable<CSVRecord> userRecords = CSVFormat.DEFAULT.withHeader("id","name","login","password").parse(in);
            userRecords.iterator().next();
            for (CSVRecord record : userRecords) {
                int id = Integer.parseInt(record.get("id"));
                String name = record.get("name");
                String login = record.get("login");
                String password = record.get("password");
                users.add(new User(id, name, login, password));
            }

            in = new FileReader(fileForProjects);
            Iterable<CSVRecord> projectRecords = CSVFormat.DEFAULT.withHeader("id","name","description","ownerId").parse(in);
            projectRecords.iterator().next();
            for (CSVRecord record : projectRecords) {
                int ID = Integer.parseInt(record.get("id"));
                String name = record.get("name");
                String description = record.get("description");
                int ownerId = Integer.parseInt(record.get("ownerId"));
                projectRows.add(new ProjectRow(ID, name, description, ownerId));
            }

            in = new FileReader(fileForProjectUserRelation);
            Iterable<CSVRecord> projectUserRelationRecords = CSVFormat.DEFAULT.withHeader("userId","projectId").parse(in);
            projectUserRelationRecords.iterator().next();
            PrintWriter pw = new PrintWriter("RecordsForRelations.log", "UTF-8");
            for (CSVRecord record : projectUserRelationRecords) {
                pw.println("String: userId '" + record.get("userId") + "', projectId '" + record.get("projectId") + "'");
                int userId = Integer.parseInt(record.get("userId"));
                int projectId = Integer.parseInt(record.get("projectId"));
                pw.println("Int: userId '" + userId + "', projectId '" + projectId + "'");
                ProjectUserRelationRow purr = new ProjectUserRelationRow(userId,projectId);
                projectUserRelationRows.add(purr);
                pw.println("Purr: userId '" + purr.userId + "', projectId '" + purr.projectId + "'");
                pw.println("Purr getter: userId '" + purr.getUserId() + "', projectId '" + purr.getProjectId() + "'");
            }
            pw.close();
            addMembersToProjects(users, projectRows, projectUserRelationRows);

            in = new FileReader(fileForIssues);
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
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void addMembersToProjects(ArrayList<User> users, ArrayList<ProjectRow> projectRows, ArrayList<ProjectUserRelationRow> projectUserRelationRows) {
        try {
            PrintWriter printWriter = new PrintWriter("Errors.log","UTF-8");
            int count = 0;
            printWriter.println(count + ": users '" + users.size() + "', projectRows '" + projectRows.size() + "', projectUserRelationRows '" + projectUserRelationRows.size() + "'");
            printWriter.println(projectUserRelationRows);

        for (User user : users) {
            userHashMap.put(user.getID(), user);
            printWriter.println(count + ": add user to userHM: '" + user.getID() + "'");
            count++;
        }
        for (ProjectRow projectRow : projectRows) {
            Project project = new Project(projectRow.getID(),projectRow.getName(), projectRow.getDescription(), userHashMap.get(projectRow.getOwnerId()), new ArrayList<>());
            projectHashMap.put(projectRow.getID(), project);
            projects.add(project);
            printWriter.println(count + ": add project to projectHM and projects: '" + project.getID() + "'");
            count++;
        }
        for (ProjectUserRelationRow projectUserRelationRow : projectUserRelationRows) {
            User user = userHashMap.get(projectUserRelationRow.getUserId());
            if (user!= null) {
                printWriter.println(count + ": find user in userHM. userId from rows: '" + projectUserRelationRow.getUserId() + "' userId from found user: '" + user.getID() + "'");
                count++;
            } else {
                printWriter.println(count + ": user not found in userHM. UserId from rows: " + projectUserRelationRow.userId);
                count++;
            }
            Project project = projectHashMap.get(projectUserRelationRow.getProjectId());
            if (project!= null) {
                printWriter.println(count + ": find project in projectHM. projectId from rows: '" + projectUserRelationRow.projectId + "' projectId for found project: '" + project.getID() + "'");
                count++;
            } else {
                printWriter.println(count + ": project not found in projectHM. projectId from rows: '" + projectUserRelationRow.projectId + "'");
            }
            if (user!=null) {
                project.addMember(user);
            }

            printWriter.close();
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


}
