package tracker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class BugTracker implements Serializable{
    private UserService userService;
    private ProjectService projectService;
    private IssueService issueService;

    BugTracker(UserService userService, ProjectService projectService, IssueService issueService) {
        this.userService = userService;
        this.projectService = projectService;
        this.issueService = issueService;
    }


    public static void main(String[] args) {
        String fileForUsers = "Users.csv";
        String fileForProjects = "Projects.csv";
        String fileForIssues = "Issues.csv";
        String fileForProjectUserRelation = "ProjectUserRelation.csv";

        DataLoader dataLoader = new DataLoader(fileForUsers, fileForProjects, fileForIssues, fileForProjectUserRelation);

        Users users = new Users(dataLoader.users);
        Projects projects = new Projects(dataLoader.projects);
        Issues issues = new Issues(dataLoader.issues);

        BugTracker bugTracker = new BugTracker(users, projects, issues);
        bugTracker.runTracker();
        bugTracker.close(users.getListOfUsers(),projects.getListOfProjects(),issues.getListOfIssues(), fileForUsers, fileForProjects, fileForIssues, fileForProjectUserRelation);

    }

    public void runTracker() {
        User user = null;
        Boolean quit = false;

        Console console = System.console();
        while (!quit) {
            console.printf("Hello, my dear friend and welcome to the BugTracking system!\nChoose option:\n");
            console.printf("%-5s %-5s %-12s\n", "", "r", "registration");
            console.printf("%-5s %-5s %-12s\n", "", "li", "log in");
            console.printf("%-5s %-5s %-12s\n", "", "q", "quit");

            String command = "";
            while (user == null & !quit) {
                command = console.readLine("> ");
                switch (command) {
                    case "q":
                        console.printf("Goodbye!\n");
                        quit = true;
                        break;
                    case "r":
                        user = registration(user);
                        break;
                    case "li":
                        user = logIn(user);
                        break;
                    default:
                        console.printf("Unknown command. Try again.\n");
                        break;

                }
            }

            printHelp();
            while (user != null & !quit) {
                command = console.readLine("> ");
                switch (command) {
                    case "help":
                        printHelp();
                        break;
                    case "q":
                        console.printf("Goodbye!\n");
                        quit = true;
                        break;
                    case "lo":
                        console.printf("Goodbye, %s!\n", user.getName());
                        user = null;
                        break;
                    case "au":
                        if (createUser()) {
                            console.printf("User added to the system successfully. To add user to the project type 'aup'.\n");
                        } else {
                            console.printf("Attempt to create user failed.\n");
                        }
                        break;
                    case "ap":
                        if (createProject(user)) {
                            console.printf("Project added to the system successfully. Owner of the project - %s (%s).\nTo add members to the project type 'aup'.\n", user.getName(), user.getLogin());
                        } else {
                            console.printf("Attempt to create project failed.\n");
                        }
                        break;
                    case "aup":
                        if (!addMembersToProject()) {
                            console.printf("Something went wrong\n");
                        }
                        break;
                    case "ai":
                        if (createIssue(user)) {
                            console.printf("Issue added to the project.\n");
                        } else {
                            console.printf("Attempt to create failed.\n");
                        }
                        break;
                    case "cia":
                        if (!changeAssigner(user)) {
                            console.printf("Something went wrong\n");
                        }
                        break;
                    case "cis":
                        break;
                    case "sp":
                        showAllProjects();
                        break;
                    case "su":
                        showAllUsers();
                        break;
                    case "si":
                        showAllIssues();
                        break;
                        default:
                            console.printf("Unknown command. For list of commands type 'help'.\n");
                            break;
                }

            }
        }

    }


    public User registration(User user) {
        if (user!=null) {
            String lo = System.console().readLine("Logged in by \"%s\". Do you want to log out? (y|n)\n", user.getLogin());
            if (lo.equals("y")) {
                System.console().printf("Now you logged out.\n");
                user =null;
            } else {
                return user;
            }
        }
        String name = System.console().readLine("Name: ");
        String login = System.console().readLine("Login: ");
        while (userService.findUserByLogin(login)!=null) {
            System.console().printf("Sorry, login \"%s\" occupied. Try again? (y|n)\n",login);
            String tryAgain = System.console().readLine("> ");
            if (tryAgain.equals("y")) {
                login = System.console().readLine("Login: ");
            } else {
                System.console().printf("Registration failed. Choose option:\n");
                System.console().printf("%-5s %-5s %-12s\n", "", "r", "registration");
                System.console().printf("%-5s %-5s %-12s\n", "", "li", "log in");
                System.console().printf("%-5s %-5s %-12s\n", "", "q", "quit");
                return null;
            }
        }
        String password = String.valueOf(System.console().readPassword("Password: "));
        user = userService.addUser(name,login,password);
        System.console().printf("Now you logged in by %s. Your id is %s\n",user.getLogin(),user.getID());
        return user;
    }

    public User logIn(User user) {
        if (user != null) {
            String lo = System.console().readLine("Logged in by \"%s\". Do you want to log out? (y|n)\n", user.getLogin());
            if (lo.equals("y")) {
                System.console().printf("Now you logged out.\n");
                user = null;
            } else {
                return user;
            }
        }

        while (user == null) {
            String userLogin = System.console().readLine("Login: ");
            String userPassword = String.valueOf(System.console().readPassword("Password: "));
            user = userService.findUserByLogin(userLogin);
            if (user != null && user.checkPassword(userPassword)) {
                System.console().printf("Hello, %s! Your id is %s.\n", user.getName(), user.getID());
                return user;
            } else {
                System.console().printf("Sorry, there is no user with this login and password. Try again?(y|n)\n");
                String tryAgain = System.console().readLine("> ");
                if (!tryAgain.equals("y")) {
                    System.console().printf("Attempt to log in failed. Choose option:\n");
                    System.console().printf("%-5s %-5s %-12s\n", "", "r", "registration");
                    System.console().printf("%-5s %-5s %-12s\n", "", "li", "log in");
                    System.console().printf("%-5s %-5s %-12s\n", "", "q", "quit");
                    return null;
                }
                user = null;
            }
        }
        return user;
    }

    public void printHelp() {
        Console console = System.console();
        console.printf("List of commands for BugTracker:\n");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "q", "quit", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "lo", "log out", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "au", "add new user", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "ap", "add new project", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "aup", "add user to the project", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "ai", "add new issue", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "cia", "change assigner of issue", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "cis", "change status of issue", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "sp", "show list of all projects", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "su", "show list of all users", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "si", "show list of all issues", "");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "help", "show list commands", "");

    }

    public boolean createUser() {
        String name = System.console().readLine("Name: ");
        String login = System.console().readLine("Login: ");
        while (userService.findUserByLogin(login)!=null) {
            String tryAgain = System.console().readLine("Sorry, this login \"%s\" occupied. Try again? (y|n) \n", login);
            if (!tryAgain.equals("y")) {
                return false;
            }
            login = System.console().readLine("Login: ");
        }
        String password = String.valueOf(System.console().readPassword("Password: "));
        userService.addUser(name,login,password);
        return true;
    }

    public boolean createProject(User user) {
        String name = System.console().readLine("Name: ");
        while (projectService.findProjectByName(name) != null) {
            System.console().printf("Sorry, name \"%s\" occupied. Try again? (y|n)\n",name);
            String tryAgain = System.console().readLine("> ");
            if (tryAgain.equals("y")) {
                name = System.console().readLine("Name: ");
            } else {
                return false;
            }
        }
        String description = System.console().readLine("Description: ");
        Project project = projectService.addProject(name,description,user,new ArrayList<>());
        project.addMembers(user);
        return true;
    }

    public boolean addMembersToProject() {
        Project project = null;
        boolean endLoop = false;
        while (!endLoop) {
            System.console().printf("Choose project. Find project by id or by name? (id|name) \n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id)<=projectService.countProjects()) {
                        project = projectService.findProjectById(Integer.parseInt(id));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    String name = System.console().readLine("Name: ");
                    project = projectService.findProjectByName(name);
                    break;
                case "q":
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (project==null) {
                System.console().printf("Project not found.\n");
                return false;
            } else {
                System.console().printf("Project %s \"%s\" chosen.\n", project.getID(), project.getName());
                endLoop = true;
            }
        }
        endLoop = false;
        User user = null;
        while (!endLoop) {
            user = null;
            System.console().printf("Choose user to add. Find user by id, name or login? (id|name|login)\n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id) <= userService.countUsers()) {
                        user = userService.findUserById(Integer.parseInt(id));
                        if (user != null) {
                            project.addMember(user);
                        }
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    String name = System.console().readLine("Name: ");
                    user = userService.findUserByName(name);
                    if (user != null) {
                        project.addMember(user);
                    }
                    break;
                case "login":
                    String login = System.console().readLine("Login: ");
                    user = userService.findUserByLogin(login);
                    if (user != null) {
                        project.addMember(user);
                    }
                    break;
                case "q":
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (user == null) {
                System.console().printf("User not found. Try again? (y|n)");
                String again = System.console().readLine("> ");
                if (!again.equals("y")) {
                    endLoop = true;
                } else {
                    endLoop = false;
                }
            } else {
                System.console().printf("User added successfully. Add another one? (y|n)");
                String anotherOne = System.console().readLine("> ");
                if (!anotherOne.equals("y")) {
                    endLoop = true;
                } else {
                    endLoop = false;
                }
            }
        }
        return true;
    }

    public boolean createIssue(User user) {
        Project project = null;
        boolean endLoop = false;
        while (!endLoop) {
            System.console().printf("Choose project. Find project by id or by name? (id|name) \n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id)<=projectService.countProjects()) {
                        project = projectService.findProjectById(Integer.parseInt(id));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    String name = System.console().readLine("Name: ");
                    project = projectService.findProjectByName(name);
                    break;
                case "q":
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (project==null) {
                System.console().printf("Project not found.\n");
                return false;
            } else {
                System.console().printf("Project №%s '%s' chosen.\n", project.getID(), project.getName());
                endLoop = true;
            }
        }
        String title = System.console().readLine("Issue title: ");
        String description = System.console().readLine("Description: ");
        endLoop = false;
        User assigner = null;
        while (!endLoop) {
            assigner = null;
            System.console().printf("Choose assigner for the issue. Find user by id, name or login? (id|name|login)\n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id) <= userService.countUsers()) {
                        assigner = userService.findUserById(Integer.parseInt(id));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    String name = System.console().readLine("Name: ");
                    assigner = userService.findUserByName(name);
                    break;
                case "login":
                    String login = System.console().readLine("Login: ");
                    assigner = userService.findUserByLogin(login);
                    break;
                case "q":
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (assigner == null) {
                System.console().printf("User not found. Try again? (y|n)");
                String again = System.console().readLine("> ");
                if (!again.equals("y")) {
                    return false;
                } else {
                    endLoop = false;
                }
            } else {
                System.console().printf("Issue assigned to the '%s'.\n",assigner.getLogin());
                endLoop = true;
            }
        }
        issueService.addIssue(title,project,user,assigner,description, LocalDateTime.now(),Status.TODO);
        return true;
    }

    public boolean changeAssigner(User user) {
        boolean endLoop = false;
        Issue issue = null;
        while(!endLoop) {
            System.console().printf("Choose issue from project of from list of issues? (project|list)\n");
            String choice = System.console().readLine("> ");
            String issueId;
            switch (choice) {
                case "project":
                    Project project = null;
                    showAllProjects();
                    boolean endProjectLoop = false;
                    while (!endProjectLoop) {
                        String projectId = System.console().readLine("Project id: ");
                        if (projectId.matches("\\d+") && Integer.parseInt(projectId) <= projectService.countProjects()) {
                            project = projectService.findProjectById(Integer.parseInt(projectId));
                        } else {
                            System.console().printf("Wrong id.\n");
                        }
                        if (project == null) {
                            System.console().printf("Project not found. Try again? (y|n)");
                            String again = System.console().readLine("> ");
                            if (!again.equals("y")) {
                                break;
                            } else {
                                endProjectLoop = false;
                            }
                        } else {
                            endProjectLoop = true;
                        }
                    }
                    showIssuesForProject(project);
                    issueId = System.console().readLine("Issue id: ");
                    if (issueId.matches("\\d+") && Integer.parseInt(issueId) <= issueService.countIssues()) {
                        issue = issueService.findIssueById(Integer.parseInt(issueId));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "list":
                    showAllIssues();
                    issueId = System.console().readLine("Issue id: ");
                    if (issueId.matches("\\d+") && Integer.parseInt(issueId) <= issueService.countIssues()) {
                        issue = issueService.findIssueById(Integer.parseInt(issueId));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "q":
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (issue == null) {
                System.console().printf("Issue not found. Try again? (y|n)\n");
                String again = System.console().readLine("> ");
                if (!again.equals("y")) {
                    return false;
                } else {
                    endLoop = false;
                }
            } else {
                endLoop = true;
            }
        }
        Project project = issue.getProject();
        boolean access = false;
        for (User u : project.getMembers()) {
            if (u == user) {
                access = true;
            }
        }
        if (access == false) {
            System.console().printf("Sorry, only members of project '%s' can change assigners for its issues.\n", project.getName());
            return false;
        }

        endLoop = false;
        User assigner = null;
        while (!endLoop) {
            assigner = null;
            System.console().printf("Choose assigner for the issue. Find user by id, name or login? (id|name|login)\n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id) <= userService.countUsers()) {
                        assigner = userService.findUserById(Integer.parseInt(id));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    String name = System.console().readLine("Name: ");
                    assigner = userService.findUserByName(name);
                    break;
                case "login":
                    String login = System.console().readLine("Login: ");
                    assigner = userService.findUserByLogin(login);
                    break;
                case "q":
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (assigner == null) {
                System.console().printf("User not found. Try again? (y|n)\n");
                String again = System.console().readLine("> ");
                if (!again.equals("y")) {
                    return false;
                } else {
                    endLoop = false;
                }
            } else {
                System.console().printf("Issue '%s' of project '%s' assigned to the '%s' by '%s'.\n",issue.getTitle(),project.getName(),assigner.getLogin(),user.getLogin());
                return true;
            }
        }
        return false;
    }

    public void showAllProjects() {

        System.console().printf("|%-5s |%-20s |%-40s |%-30s|\n","ID", "Name", "Description", "Owner name (login)");
        System.console().printf("--------------------------------------------------------------------------------------------------\n");
        for (Project project : projectService.getListOfProjects()) {
            System.console().printf("|%-5d |%-20s |%-40s |%-30s|\n",project.getID(), project.getName(),project.getDescription() + " (" + project.getMembers().size() + ")",project.getOwnerName() + " (" + project.getOwnerLogin() + ")");
            System.console().printf("|------|---------------------|-----------------------------------------|------------------------------|\n");
        }
    }

    public void showAllUsers() {
        System.console().printf("|%-5s |%-30s |%-20s |\n","ID", "Name", "login");
        System.console().printf("--------------------------------------------------------------\n");
        for (User user : userService.getListOfUsers()) {
            System.console().printf("|%-5d |%-30s |%-20s |\n",user.getID(), user.getName(),user.getLogin());
            System.console().printf("|------|-------------------------------|---------------------|\n");
        }
    }

    public void showAllIssues() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");
        System.console().printf("|%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-13s |%-10s |\n","ID","Project","Owner","Title","Description","Assigner","Creation time","Status");
        System.console().printf("-----------------------------------------------------------------------------------------------------------------------------------------\n");
        for (Issue issue : issueService.getListOfIssues()) {
            System.console().printf("|%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-13s |%-10s |\n",
                    issue.getID(), "№" + issue.getProjectId() + " " + issue.getProject().getName(), issue.getOwner().getLogin(), issue.getTitle(), issue.getDescription(),
                    issue.getAssigner().getLogin(),issue.getCreationTime().format(dateTimeFormatter),issue.getStatus());
            System.console().printf("|------|---------------------|----------------|----------------|-----------------------------------------|----------------|--------------|-----------|\n");
        }
    }

    public void showIssuesForProject(Project project) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");
        System.console().printf("|%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-12s|%-10s |\n","ID","Project","Owner","Title","Description","Assigner","Creation time","Status");
        System.console().printf("------------------------------------------------------------------------------------------------------------------------------------------------------\n");
        for (Issue issue : issueService.getIssuesForProject(project)) {
            System.console().printf("|%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-12s|%-10s |\n",
                    issue.getID(), " " + issue.getProjectId() + " " + issue.getProject().getName(), issue.getOwner().getLogin(), issue.getTitle(), issue.getDescription(),
                    issue.getAssigner().getLogin(),issue.getCreationTime().format(dateTimeFormatter),issue.getStatus());
            System.console().printf("|------|---------------------|----------------|----------------|-----------------------------------------|----------------|--------------|-----------|\n");
        }
    }

    public void close(ArrayList<User> users, ArrayList<Project> projects, ArrayList<Issue> issues, String fileForUsers, String fileForProjects, String fileForIssues, String fileForProjectUserRelation) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForUsers), CSVFormat.DEFAULT.withHeader("id","name","login","password"))) {
            for (User user : users) {
                printer.printRecord(user.getID(),user.getName(),user.getLogin(),user.getPassword());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForProjects), CSVFormat.DEFAULT.withHeader("id","name","description","ownerId"))) {
            for (Project project : projects) {
                printer.printRecord(project.getID(),project.getName(),project.getDescription(),project.getOwnerId());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForIssues), CSVFormat.DEFAULT.withHeader("id","title","projectId","ownerId","assignerId","description","creationTime","status"))) {
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
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileForProjectUserRelation), CSVFormat.DEFAULT.withHeader("userId","projectId"))) {
            for (Project project : projects) {
                for (User user : project.getMembers()) {
                    printer.printRecord(user.getID(),project.getID());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
