package phase2;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private Map<String, User> sessions = new HashMap<>();

    public void loginUser(String username, User user) {
        sessions.put(username, user);
    }

    public void logoutUser(String username) {
        sessions.remove(username);
    }

    public User getActiveUser(String username) {
        return sessions.get(username);
    }
}
