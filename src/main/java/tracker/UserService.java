package tracker;

import java.util.ArrayList;

public interface UserService {
    ArrayList<User> getListOfUsers();

    User findUserById(int id);

    User findUserByLogin(String login);

    User findUserByName(String name);

    User addUser(String name, String login, String password);

    boolean removeProject(User user);

    void clearUserList();

    int countUsers();
}
