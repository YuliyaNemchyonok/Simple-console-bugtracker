package tracker;

import java.io.*;
import java.util.ArrayList;
import org.apache.commons.csv.*;

public class Users implements UserService, Serializable {
    private ArrayList<User> users;
    private int count;

    Users(ArrayList<User> users) {
        this.users = users;
        int max = 0;
        for (User u : users) {
            if (u.getID()>max) {
                max = u.getID();
            }
        }
        this.count = max;
    }

    @Override
    public User addUser(String name, String login, String password) {
        User user = new User(++count,name, login, password);
        users.add(user);
        return user;
    }

    @Override
    public ArrayList<User> getListOfUsers() {
        return users;
    }

    @Override
    public User findUserById(int id) {
        for (User user : users) {
            if (user.getID()==id) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findUserByLogin(String login) {
        for (User user: users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findUserByName(String name) {
        for (User user : users) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public boolean removeProject(User user) {
        if (users.contains(user)) {
            users.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public void clearUserList() {
        users.clear();
    }

    @Override
    public int countUsers() {
        return count;
    }
}
