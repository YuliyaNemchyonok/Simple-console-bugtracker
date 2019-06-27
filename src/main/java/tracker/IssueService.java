package tracker;

import java.time.LocalDateTime;
import java.util.*;

public interface IssueService {
    ArrayList<Issue> getListOfIssues();

    Issue findIssueById(int id);

    Issue findIssueByTitle(String title);

    Issue addIssue(String title, Project project, User owner, User assigner, String description, LocalDateTime creationTime, Status status);

    boolean removeIssue(Issue issue);

    List<Issue> getIssuesForProject(Project project);

    List<Issue> getOwnedIssuesForUser(User user);

    List<Issue> getAssignedIssuesForUser(User user);

    void clearIssueList();

    int countIssues();
}
