package tracker;

import java.time.LocalDateTime;
import java.util.ArrayList;

public interface IssueService {
    ArrayList<Issue> getListOfIssues();

    Issue findIssueByTitle(String title);

    Issue addIssue(String title, Project project, User owner, User assigner, String description, LocalDateTime creationTime, Status status);

    boolean removeIssue(Issue issue);

    void clearIssueList();
}
