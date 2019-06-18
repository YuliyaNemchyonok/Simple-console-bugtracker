package tracker;

import java.io.Serializable;
import java.util.List;


public class User implements Serializable {
    private final int ID;
    private String name;
    private String login;
    private String password;

    User(int id, String name, String login, String password) {
        this.ID = id;
        this.name = name;
        this.login = login;
        this.password = password;
    }

    int getID() {
        return this.ID;
    }

    String getLogin() {
        return this.login;
    }

    String getName() {
        return this.name;
    }

    public String getPassword() {
        return password;
    }

    boolean checkPassword(String pass) {
        return password.equals(pass);
    }


    @Override
    public String toString() {
        return this.ID + "," + this.name + "," + this.login + "," + this.password;
    }
}
