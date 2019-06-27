package tracker;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class Issues implements IssueService {
    private ArrayList<Issue> issues;
    private int count;

    Issues(ArrayList<Issue> issues) {
        this.issues =issues;
        int max = 0;
        for (Issue i : issues) {
            if (i.getID()>max) {
                max = i.getID();
            }
        }
        this.count = max;
    }

    @Override
    public ArrayList<Issue> getListOfIssues() {
        return this.issues;
    }

    @Override
    public Issue findIssueById(int id) {
        for (Issue issue : issues) {
            if (issue.getID()==id) {
                return issue;
            }
        }
        return null;
    }

    @Override
    public Issue findIssueByTitle(String title) {
        for (Issue issue : issues) {
            if (issue.getTitle().equals(title)) {
                return issue;
            }
        }
        return null;
    }

    @Override
    public Issue addIssue(String title, Project project, User owner, User assigner, String description, LocalDateTime creationTime, Status status) {
        Issue issue = new Issue(++count, title, project, owner, assigner, description, creationTime, status);
        issues.add(issue);
        return issue;
    }

    @Override
    public boolean removeIssue(Issue issue) {
        if (issues.contains(issue)) {
            issues.remove(issue);
            return true;
        }
        return false;
    }

    @Override
    public List<Issue> getIssuesForProject(Project project) {
        List<Issue> projectIssues = new ArrayList<>();
        for (Issue issue : issues) {
            if (issue.getProject()==project) {
                projectIssues.add(issue);
            }
        }
        return projectIssues;
    }

    @Override
    public List<Issue> getAssignedIssuesForUser(User user) {
        List<Issue> userIssues = new ArrayList<>();
        for (Issue issue : issues) {
            if (issue.getAssigner()==user) {
                userIssues.add(issue);
            }
        }
        return userIssues;
    }

    @Override
    public List<Issue> getOwnedIssuesForUser(User user) {
        List<Issue> userIssues = new ArrayList<>();
        for (Issue issue : issues) {
            if (issue.getOwner()==user) {
                userIssues.add(issue);
            }
        }
        return userIssues;
    }

    @Override
    public void clearIssueList() {
        issues.clear();
    }

    @Override
    public int countIssues() {
        return count;
    }


}
