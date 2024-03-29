package tracker;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class BugTracker{
    private static final Logger log = LogManager.getLogger(BugTracker.class.getName());
    private UserService userService;
    private ProjectService projectService;
    private IssueService issueService;

    private BugTracker(UserService userService, ProjectService projectService, IssueService issueService) {
        log.debug("Start constructor");

        this.userService = userService;
        this.projectService = projectService;
        this.issueService = issueService;

        log.debug("Constructor complete");
    }


    public static void main(String[] args) {
        log.info("Method main start");
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

        DataWriter dataWriter = new DataWriter(users.getListOfUsers(),projects.getListOfProjects(),issues.getListOfIssues(), fileForUsers, fileForProjects, fileForIssues, fileForProjectUserRelation);
        dataWriter.writeDataToFiles();
    }

    private void runTracker() {
        log.info("Method runTracker invoked");
        User user = null;
        boolean quit = false;

        Console console = System.console();
        console.printf("Hello, my dear friend and welcome to the BugTracking system!\n");
        while (!quit) {
            while (user == null & !quit) {
                console.printf("Choose option:\n");
                console.printf("%-5s %-5s %-12s\n", "", "r", "registration");
                console.printf("%-5s %-5s %-12s\n", "", "li", "log in");
                console.printf("%-5s %-5s %-12s\n", "", "q", "quit");
                String command = console.readLine("> ");
                switch (command) {
                    case "q":
                        console.printf("Goodbye!\n");
                        quit = true;
                        break;
                    case "r":
                        user = registration();
                        if (user==null) {
                            System.console().printf("Registration failed.\n");
                        } else {
                            System.console().printf("Now you logged in by %s. Your id is %s\n",user.getLogin(),user.getID());
                        }
                        break;
                    case "li":
                        user = logIn();
                        break;
                    default:
                        console.printf("Unknown command. Try again.\n");
                        break;

                }
            }
            if (user != null) {
                log.info("User '" + user.getID() + " - " + user.getLogin() + "' logged in the system");
            }

            printHelp();

            while (user != null & !quit) {
                String command = console.readLine("> ");
                switch (command) {
                    case "help":
                        printHelp();
                        break;
                    case "q":
                        console.printf("Goodbye, %s!\n",user.getName());
                        quit = true;
                        break;
                    case "lo":
                        console.printf("Goodbye, %s!\n", user.getName());
                        user = null;
                        break;
                    case "au":
                        boolean endLoop = false;
                        while (!endLoop) {
                            User userToAdd = registration();
                            if (userToAdd!=null) {
                                console.printf("User added successfully.\nAdd another one? (y|n)\n");
                            } else {
                                console.printf("Attempt to create user failed.\nTry again? (y|n)\n");
                            }
                            String again = console.readLine("> ");
                            if (!again.equals("y")) {
                                endLoop = true;
                            }
                        }
                        console.printf("To add users to the project type 'aup'\n");
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
                        if (!changeStatusOfIssue(user)) {
                            console.printf("Something went wrong\n");
                        }
                        break;
                    case "sp":
                        showProjects();
                        break;
                    case "su":
                        showUsers();
                        break;
                    case "si":
                        showIssues();
                        break;
                        default:
                            console.printf("Unknown command. For list of commands type 'help'.\n");
                            break;
                }

            }
        }
        log.info("Method runTracker completed");
    }


    private User registration() {
        log.info("Registration of new user begin");
        String name = System.console().readLine("Name: ");
        String login = System.console().readLine("Login: ");
        while (userService.findUserByLogin(login)!=null) {
            log.debug("Login '" + login + "' already occupied.");
            System.console().printf("Sorry, login '%s' occupied. Try again? (y|n)\n",login);
            String tryAgain = System.console().readLine("> ");
            if (tryAgain.equals("y")) {
                login = System.console().readLine("Login: ");
            } else {
                log.info("Registration failed. Login chosen for new user already occupied, user prefer to terminate registration.");
                return null;
            }
        }
        String password = String.valueOf(System.console().readPassword("Password: "));
        User user = userService.addUser(name,login,password);

        log.info("Registration completed successfully. New user number " + user.getID() + " with login '" + user.getLogin() + "' registered.");
        return user;
    }

    private User logIn() {
        log.info("Authorization begin");

        User user = null;

        while (user == null) {
            String userLogin = System.console().readLine("Login: ");
            String userPassword = String.valueOf(System.console().readPassword("Password: "));
            user = userService.findUserByLogin(userLogin);
            if (user == null || !user.checkPassword(userPassword)) {
                if (user==null) {
                    log.debug("User with login '" + userLogin + "' not found in the system");
                } else {
                    log.debug("Entered wrong password for user '" + userLogin + "'");
                    user = null;
                }
                System.console().printf("Sorry, there is no user with this login and password. Try again?(y|n)\n");
                String tryAgain = System.console().readLine("> ");
                if (!tryAgain.equals("y")) {
                    System.console().printf("Authorization failed.\n");
                    log.info("Authorization failed. No match for login-password in the system, user prefer to terminate authorization.");
                    return null;
                }
            }
        }
        System.console().printf("Hello, %s! Your id is %s.\n", user.getName(), user.getID());
        log.info("Successful authorization. User number " + user.getID() + " with login '" + user.getLogin() + "' logged in.");
        return user;
    }

    private void printHelp() {
        Console console = System.console();
        console.printf("List of commands for BugTracker:\n");
        console.printf("%-5s %-5s %-20s %-20s\n", "", "q", "quit", "you can always leave any dialog with this command");
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
        log.info("Print help");
    }

    private User chooseUser()  {
        log.debug("Method chooseUser invoked");
        User user = null;
        boolean endLoop = false;
        while (!endLoop) {
            System.console().printf("Find user by id, name or login? (id|name|login)\n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    log.debug("Find user by id");
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id) <= userService.countUsers()) {
                        user = userService.findUserById(Integer.parseInt(id));
                        if (user != null) {
                            log.debug("Method chooseUser completed. User with id "+ id + "found.");
                            return user;
                        }
                    } else {
                        log.debug("User with id " + id + " not found");
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    log.debug("Find by name");
                    String name = System.console().readLine("Name: ");
                    user = userService.findUserByName(name);
                    if (user != null) {
                        log.debug("Method chooseUser completed. User with name '"+ name + "' found.");
                        return user;
                    } else {
                        log.debug("User with name '" + name + "' not found");
                        System.console().printf("Wrong name.\n");
                    }
                    break;
                case "login":
                    log.debug("Find by login");
                    String login = System.console().readLine("Login: ");
                    user = userService.findUserByLogin(login);
                    if (user != null) {
                        log.debug("User with login '" + login + "' found");
                        return user;
                    }else {
                        log.debug("User with login '" + login + "' not found");
                        System.console().printf("Wrong login.\n");
                    }
                    break;
                case "q":
                    log.debug("Quit find user loop by type 'q'");
                    endLoop = true;
                    break;
                default:
                    log.debug("Unknown operation for find user '" + choice + "'");
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }

            log.debug("User = null");
            System.console().printf("User not found. Try again? (y|n)");
            String again = System.console().readLine("> ");
            log.debug("Try again answer '" + again +"'");
            endLoop = !again.equals("y");
        }

        log.debug("Method chooseUser completed without finding user and return null.");
        return null;
    }

    private boolean createProject(User user) {
        log.info("Creating project begin");
        String name = System.console().readLine("Name: ");
        while (projectService.findProjectByName(name) != null) {
            log.debug("Project with name '" + name + "' already existed");
            System.console().printf("Sorry, name '%s' occupied. Try again? (y|n)\n",name);
            String tryAgain = System.console().readLine("> ");
            if (tryAgain.equals("y")) {
                name = System.console().readLine("Name: ");
            } else {
                log.info("Creating project failed. Name chosen for new project already occupied, user prefer to terminate registration.");
                return false;
            }
        }
        String description = System.console().readLine("Description: ");
        Project project = projectService.addProject(name,description,user,new ArrayList<>());
        project.addMembers(user);

        log.info("Creating project completed. New project number " + project.getID() + " with name '" + project.getName() + "' and owner '" + project.getOwnerName() + "' created");
        return true;
    }

    private Project chooseProject() {
        log.debug("Method ChooseProject invoked");
        Project project = null;

        System.console().printf("Show list of all projects? (y|n)\n");
        String show = System.console().readLine("> ");
        if (show.equals("y")) showProjects();

        boolean endLoop = false;
        while (!endLoop) {
            System.console().printf("Choose project by id or by name? (id|name) \n");
            String choice = System.console().readLine("> ");
            switch (choice) {
                case "id":
                    log.debug("Find project by id");
                    String id = System.console().readLine("id: ");
                    if (id.matches("\\d+") && Integer.parseInt(id)<=projectService.countProjects()) {
                        project = projectService.findProjectById(Integer.parseInt(id));
                        log.debug("In the list of projects search by id " + id);
                    } else {
                        log.debug("Project with id " + id + " not found");
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "name":
                    log.debug("Find by name");
                    String name = System.console().readLine("Name: ");
                    project = projectService.findProjectByName(name);
                    log.debug("In the list of projects search by name '" + name + "'");
                    break;
                case "q":
                    log.debug("Quit find project loop by type 'q'");
                    endLoop = true;
                    break;
                default:
                    System.console().printf("Unknown operation. Try again.\n");
                    break;
            }
            if (project==null) {
                log.debug("Project = null");
                System.console().printf("Project not found. Try again? (y|n)\n");
                String again = System.console().readLine("> ");
                log.debug("Try again answer '" + again + "'");
                endLoop = !again.equals("y");
            } else {
                endLoop = true;
            }
        }

        if (project == null) {
            log.debug("Method chooseProject return null");
        } else {
            log.debug("Method chooseProject return project: " + project.toString());
        }

        return project;
    }

    private boolean addMembersToProject() {
        log.info("Start adding members to project");
        Project project = chooseProject();
        if (project==null) {
            log.info("Adding members failed. Project not found.");
            System.console().printf("You can add members only for existing project.\n");
            return false;
        }

        log.info("Members will be added to the project " + project.getID() + " '" + project.getName() + "'");
        System.console().printf("Show members for this project? (y|n)\n");
        String show = System.console().readLine(">");
        if (show.equals("y")) {
            showUsers(project);
        }

        boolean endLoop = false;
        while (!endLoop) {
            log.info("Start adding member");
            User user = chooseUser();
            if (user == null) {
                log.info("Member to add not found");
                System.console().printf("User not found. Try again? (y|n)\n");
                String again = System.console().readLine("> ");
                endLoop = !again.equals("y");
                log.debug("Try again answer '" + again + "'");
            } else {
                project.addMember(user);
                log.info("Found member to add. User number " + user.getID() + " added as member to the project");
                System.console().printf("User added successfully. Add another one? (y|n)\n");
                String anotherOne = System.console().readLine("> ");
                log.debug("Add one more member answer '" + anotherOne + "'");
                endLoop = !anotherOne.equals("y");
            }
        }
        log.info("Adding members ended");
        return true;
    }

    private boolean createIssue(User user) {
        Project project = chooseProject();
        if (project==null) {
            System.console().printf("You can create new issue only for existing project.\n");
            return false;
        }
        String title = System.console().readLine("Issue title: ");

        String description = System.console().readLine("Description: ");

        System.console().printf("Choose assigner for the issue.\n");
        User assigner = null;
        boolean endLoop = false;
        while (!endLoop) {
            assigner = chooseUser();
            if (assigner == null) {
                System.console().printf("You can't create issue without assigner. Try again? (y|n)\n");
                String again = System.console().readLine("> ");
                endLoop = !again.equals("y");
            } else {
                System.console().printf("Issue assigned to the '%s'.\n",assigner.getLogin());
                endLoop = true;
            }
        }
        if (assigner == null) {
            return false;
        }
        issueService.addIssue(title,project,user,assigner,description, LocalDateTime.now(),Status.TODO);
        return true;
    }

    private Issue chooseIssue() {
        Issue issue = null;
        boolean endLoop = false;
        while(!endLoop) {
            System.console().printf("Choose issue from list of issues, project or user? (issue|project|user)\n");
            String choice = System.console().readLine("> ");
            String issueId;

            switch (choice) {
                case "issue":
                    System.console().printf("Show list of all issues? (y|n)\n");
                    String show = System.console().readLine("> ");
                    if (show.equals("y")) showIssues();

                    issueId = System.console().readLine("Issue id: ");
                    if (issueId.matches("\\d+") && Integer.parseInt(issueId) <= issueService.countIssues()) {
                        issue = issueService.findIssueById(Integer.parseInt(issueId));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "project":
                    Project project = chooseProject();
                    if (project==null) {
                        break;
                    }

                    System.console().printf("List of issues for project %s - '%s':\n",project.getID(),project.getName());
                    showIssues(project);

                    issueId = System.console().readLine("Issue id: ");
                    if (issueId.matches("\\d+") && Integer.parseInt(issueId) <= issueService.countIssues()) {
                        int id = Integer.parseInt(issueId);
                        for (Issue i : issueService.getIssuesForProject(project)) {
                            if (i.getID() == id) {
                                issue = i;
                                break;
                            }
                        }
                        System.console().printf("Project %s - '%s' has no such issue.\n", project.getID(),project.getName());
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "user":
                    User user = chooseUser();
                    if (user==null) {
                        break;
                    }

                    System.console().printf("List of issues for user %s - '%s':\n",user.getID(),user.getName());
                    showIssues(user);

                    issueId = System.console().readLine("Issue id: ");
                    if (issueId.matches("\\d+") && Integer.parseInt(issueId) <= issueService.countIssues()) {
                        int id = Integer.parseInt(issueId);
                        for (Issue i : issueService.getOwnedIssuesForUser(user)) {
                            if (i.getID() == id) {
                                issue = i;
                                break;
                            }
                        }
                        for (Issue i : issueService.getAssignedIssuesForUser(user)) {
                            if (i.getID() == id) {
                                issue = i;
                                break;
                            }
                        }
                        System.console().printf("User %s - '%s' has no such issue.\n",user.getID(),user.getLogin());
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
                endLoop = !again.equals("y");
            } else {
                endLoop = true;
            }
        }
        return issue;
    }

    private boolean changeAssigner(User user) {
        Issue issue = chooseIssue();

        if (issue==null) {
            return false;
        }

        Project project = issue.getProject();

        boolean access = false;
        for (User u : project.getMembers()) {
            if (u == user) {
                access = true;
            }
        }
        if (!access) {
            System.console().printf("Sorry, only members of project %s - '%s' can change assigners for its issues.\n", project.getID(), project.getName());
            return false;
        }

        System.console().printf("Choose new assigner for issue %s - '%s' instead of '%s'\n",issue.getID(),issue.getTitle(),issue.getAssigner().getLogin());
        User assigner;
        boolean endLoop = false;
        while (!endLoop) {
            assigner = chooseUser();
            if (assigner == null) {
                System.console().printf("Leave '%s' assigner of issue %s - '%s'? (y|n)\n",issue.getAssigner().getLogin(),issue.getID(),issue.getTitle());
                String leave = System.console().readLine("> ");
                endLoop = leave.equals("y");
            } else {
                System.console().printf("Issue %s - '%s' of project '%s' assigned to the '%s' by '%s'.\n",issue.getID(), issue.getTitle(),project.getName(),assigner.getLogin(),user.getLogin());
                endLoop = true;
            }
        }
        return true;
    }

    private boolean changeStatusOfIssue(User user) {
        boolean endLoop = false;
        Issue issue = null;
        while(!endLoop) {
            System.console().printf("Choose issue from project of from list of issues? (project|list)\n");
            String choice = System.console().readLine("> ");
            String issueId;
            switch (choice) {
                case "project":
                    Project project = null;
                    showProjects();
                    boolean endProjectLoop = false;
                    while (!endProjectLoop) {
                        String projectId = System.console().readLine("Project id: ");
                        if (projectId.matches("\\d+") && Integer.parseInt(projectId) <= projectService.countProjects()) {
                            project = projectService.findProjectById(Integer.parseInt(projectId));
                        } else {
                            System.console().printf("Wrong id.\n");
                        }
                        if (project == null) {
                            System.console().printf("Project not found. Try again? (y|n)\n");
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
                    showIssues(project);
                    issueId = System.console().readLine("Issue id: ");
                    if (issueId.matches("\\d+") && Integer.parseInt(issueId) <= issueService.countIssues()) {
                        issue = issueService.findIssueById(Integer.parseInt(issueId));
                    } else {
                        System.console().printf("Wrong id.\n");
                    }
                    break;
                case "list":
                    showIssues();
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
        if (!access) {
            System.console().printf("Sorry, only members of project '%s' can change statuses for its issues.\n", project.getName());
            return false;
        }

        endLoop = false;
        while (!endLoop) {
            System.console().printf("Choose new status for issue.\n" +
                    "1 -> TODO\n" +
                    "2 -> IN_PROGRESS\n" +
                    "3 -> DONE\n");
            String strNumOfStatus = System.console().readLine("> ");
            if (strNumOfStatus.matches("\\d+")) {
                int numOfStatus = Integer.parseInt(strNumOfStatus);
                switch (numOfStatus) {
                    case 1:
                        if (issue.getStatus()==Status.TODO) {
                            System.console().printf("Issue '%s' already in %s status.\n",issue.getTitle(),issue.getStatus());
                            endLoop =true;
                            break;
                        }
                        issue.setStatus(Status.TODO);
                        endLoop = true;
                        break;
                    case 2:
                        if (issue.getStatus()==Status.IN_PROGRESS) {
                            System.console().printf("Issue '%s' already in %s status.\n",issue.getTitle(),issue.getStatus());
                            endLoop =true;
                            break;
                        }
                        issue.setStatus(Status.IN_PROGRESS);
                        endLoop = true;
                        break;
                    case 3:
                        if (issue.getStatus()==Status.DONE) {
                            System.console().printf("Issue '%s' already in %s status.\n",issue.getTitle(),issue.getStatus());
                            endLoop =true;
                            break;
                        }
                        issue.setStatus(Status.DONE);
                        endLoop = true;
                        break;
                        default:
                            System.console().printf("There is no option %s. Try again? (y|n)\n", numOfStatus);
                            String again = System.console().readLine("> ");
                            if (!again.matches("y")) {
                                return false;
                            } else {
                                endLoop = false;
                            }
                            break;
                }
            } else {
                System.console().printf("There is no option %s. Try again? (y|n)\n", strNumOfStatus);
                String again = System.console().readLine("> ");
                if (!again.matches("y")) {
                    return false;
                } else {
                    endLoop = false;
                }
            }
        }
        return true;
    }

    private void showProjects() {

        System.console().printf("|%-5s |%-20s |%-40s |%-30s|\n","ID", "Name", "Description", "Owner name (login)");
        System.console().printf("--------------------------------------------------------------------------------------------------\n");
        for (Project project : projectService.getListOfProjects()) {
            System.console().printf("|%-5d |%-20s |%-40s |%-30s|\n",project.getID(), project.getName(),project.getDescription() + " (" + project.getMembers().size() + ")",project.getOwnerName() + " (" + project.getOwnerLogin() + ")");
            System.console().printf("|------|---------------------|-----------------------------------------|------------------------------|\n");
        }
    }

    private void showUsers() {
        System.console().printf("|%-5s |%-30s |%-20s |\n","ID", "Name", "login");
        System.console().printf("--------------------------------------------------------------\n");
        for (User user : userService.getListOfUsers()) {
            System.console().printf("|%-5d |%-30s |%-20s |\n",user.getID(), user.getName(),user.getLogin());
            System.console().printf("|------|-------------------------------|---------------------|\n");
        }
    }

    private void showUsers(Project project) {
        System.console().printf("|%-5s |%-30s |%-20s |\n","ID", "Name", "login");
        System.console().printf("--------------------------------------------------------------\n");
        for (User user : project.getMembers()) {
            System.console().printf("|%-5d |%-30s |%-20s |\n",user.getID(), user.getName(),user.getLogin());
            System.console().printf("|------|-------------------------------|---------------------|\n");
        }
    }

    private void showIssues() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");
        System.console().printf("|%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-13s |%-11s |\n","ID","Project","Owner","Title","Description","Assigner","Creation time","Status");
        System.console().printf("-------------------------------------------------------------------------------------------------------------------------------------------------------\n");
        for (Issue issue : issueService.getListOfIssues()) {
            System.console().printf("|%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-13s|%-11s |\n",
                    issue.getID(), " " + issue.getProjectId() + " - " + issue.getProject().getName(), issue.getOwner().getLogin(), issue.getTitle(), issue.getDescription(),
                    issue.getAssigner().getLogin(),issue.getCreationTime().format(dateTimeFormatter),issue.getStatus());
            System.console().printf("|------|---------------------|----------------|----------------|-----------------------------------------|----------------|--------------|------------|\n");
        }
    }

    private void showIssues(Project project) {
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

    private void showIssues(User user) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm");
        System.console().printf("|%-9s |%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-12s|%-10s |\n","User role","ID","Project","Owner","Title","Description","Assigner","Creation time","Status");
        System.console().printf("-----------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
        for (Issue issue : issueService.getOwnedIssuesForUser(user)) {
            System.console().printf("|%-9s |%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-12s|%-10s |\n",
                    "Owner", issue.getID(), " " + issue.getProjectId() + " " + issue.getProject().getName(), issue.getOwner().getLogin(), issue.getTitle(),
                    issue.getDescription(),                    issue.getAssigner().getLogin(),issue.getCreationTime().format(dateTimeFormatter),issue.getStatus());
            System.console().printf("|----------|------|---------------------|----------------|----------------|-----------------------------------------|----------------|--------------|-----------|\n");
        }
        for (Issue issue : issueService.getAssignedIssuesForUser(user)) {
            System.console().printf("|%-9s |%-5s |%-20s |%-15s |%-15s |%-40s |%-15s |%-12s|%-10s |\n",
                    "Assigner", issue.getID(), " " + issue.getProjectId() + " " + issue.getProject().getName(), issue.getOwner().getLogin(), issue.getTitle(),
                    issue.getDescription(),                    issue.getAssigner().getLogin(),issue.getCreationTime().format(dateTimeFormatter),issue.getStatus());
            System.console().printf("|----------|------|---------------------|----------------|----------------|-----------------------------------------|----------------|--------------|-----------|\n");
        }

    }
}
