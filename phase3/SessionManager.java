package phase3;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private Map<String, User> activeSessions;

    public SessionManager() {
        activeSessions = new HashMap<>();
    }

    public void loginUser(String username, User user) {
        activeSessions.put(username, user);
    }

    public void logoutUser(String username) {
        activeSessions.remove(username);
    }

    public boolean isUserLoggedIn(String username) {
        return activeSessions.containsKey(username);
    }

    public User getUser(String username) {
        return activeSessions.get(username);
    }
}



