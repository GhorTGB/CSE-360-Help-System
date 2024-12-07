package phase3;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;
    private String password;
    private Set<Role> roles;
    private Set<String> groups; // Stores the groups this user belongs to

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
        this.groups = new HashSet<>(); // Initialize the groups set
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    public void removeGroup(String group) {
        groups.remove(group);
    }

    public boolean isInGroup(String group) {
        return groups.contains(group);
    }
}
