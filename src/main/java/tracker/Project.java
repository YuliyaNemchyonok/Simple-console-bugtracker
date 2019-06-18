package tracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Project implements Serializable {
    private final int ID;
    private String name;
    private String description;
    private User owner;
    private List<User> members;

    Project(int id, String name, String description, User owner, ArrayList<User> members) {
        this.ID = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.members = members;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getOwnerId() {
        return owner.getID();
    }

    public String getOwnerName() {
        return owner.getName();
    }

    public String getOwnerLogin() {
        return owner.getLogin();
    }

    public void addMember(User user) {
        this.members.add(user);
    }

    public void setMembers(List<User> users) {
        this.members = users;
    }

    public void addMembers(User... users) {
        this.members.addAll(Arrays.asList(users));
    }

    public void addMembers(List<User> users) {
        this.members.addAll(users);
    }

    public List<User> getMembers() {
        return this.members;
    }

    public boolean removeMember(User user) {
        if (members.contains(user)) {
            members.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.ID + "," + this.name + "," + this.description + "," + this.owner.getID();
    }
}
