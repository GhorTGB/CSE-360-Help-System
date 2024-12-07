package phase3;

import java.util.HashSet;
import java.util.Set;

public class SpecialAccessGroup {
    private String groupName;
    private Set<User> admins;
    private Set<User> usersWithViewAccess;

    public SpecialAccessGroup(String groupName) {
        this.groupName = groupName;
        this.admins = new HashSet<>();
        this.usersWithViewAccess = new HashSet<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public Set<User> getAdmins() {
        return admins;
    }

    public Set<User> getUsersWithViewAccess() {
        return usersWithViewAccess;
    }

    public void addAdmin(User user) {
        admins.add(user);
    }

    public void addUserWithViewAccess(User user) {
        usersWithViewAccess.add(user);
    }
}