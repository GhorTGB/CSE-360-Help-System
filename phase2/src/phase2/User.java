package phase2;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;
    private String password;
    private Set<Role> roles;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }
    public Set<Role> getRoles() { return roles; }

    public void addRole(Role role) { roles.add(role); }
    public void removeRole(Role role) { roles.remove(role); }
}
