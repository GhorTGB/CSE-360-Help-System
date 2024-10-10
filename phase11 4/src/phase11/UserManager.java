package phase11;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class UserManager {
    private List<User> users = new ArrayList<>();
    private User loggedInUser = null;
    private final String filePath = "user1.txt";

    public UserManager() {
        loadUsersFromFile();
    }

    public boolean createUser(String username, String password, String email, List<String> roles) {
        if (findUser(username) != null) {
            System.out.println("Username already taken.");
            return false;
        }
        User newUser = new User(username, password, email);
        for (String role : roles) {
            newUser.addRole(role);
        }
        users.add(newUser);
        saveUsersToFile();
        System.out.println("New user created with username: " + username + ", email: " + email + ", and roles: " + roles);
        return true;
    }

    public boolean login(String username, String password) {
        User user = findUser(username);
        if (user != null && user.getPassword().equals(password)) {
            loggedInUser = user;
            return true;
        }
        return false;
    }

    public void generateOtpForUser(String username) {
        User user = findUser(username);
        if (user != null) {
            String otp = generateOtp();
            Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
            user.setOtp(otp, expiration);
            System.out.println("Generated OTP for user " + username + ": " + otp + " (valid for 5 minutes)");
        } else {
            System.out.println("User not found.");
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public boolean validateOtpForUser(String username, String enteredOtp) {
        User user = findUser(username);
        if (user != null && user.isOtpValid(enteredOtp)) {
            System.out.println("OTP is valid for user " + username);
            loggedInUser = user;
            return true;
        } else {
            System.out.println("Invalid or expired OTP.");
            return false;
        }
    }

    public boolean loginWithOtp(String username, String enteredOtp) {
        return validateOtpForUser(username, enteredOtp);
    }

    public boolean resetUserAccount(String username) {
        User user = findUser(username);
        if (user != null) {
            String otp = generateOtp();
            Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
            user.setOtp(otp, expiration);
            saveUsersToFile();
            System.out.println("User account reset. OTP: " + otp);
            return true;
        }
        return false;
    }

    public boolean deleteUser(String username) {
        User user = findUser(username);
        if (user != null) {
            users.remove(user);
            saveUsersToFile();
            System.out.println("Deleted user: " + username);
            return true;
        }
        return false;
    }

    public boolean addRoleToUser(String username, String role) {
        User user = findUser(username);
        if (user != null) {
            user.addRole(role);
            saveUsersToFile();
            System.out.println("Role added: " + role + " for user: " + username);
            return true;
        }
        return false;
    }

    public boolean removeRoleFromUser(String username, String role) {
        User user = findUser(username);
        if (user != null) {
            user.removeRole(role);
            saveUsersToFile();
            System.out.println("Role removed: " + role + " for user: " + username);
            return true;
        }
        return false;
    }

    public void listUsers() {
        for (User user : users) {
            System.out.println("Username: " + user.getUsername() + ", Email: " + user.getEmail() + ", Roles: " + user.getRoles());
        }
    }

    private void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (User user : users) {
                writer.println(user.getUsername() + "," + user.getPassword() + "," + user.getEmail() + "," + String.join(";", user.getRoles()));
            }
        } catch (IOException e) {
            System.out.println("Error saving users to file: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No user data found. Starting with an empty user list.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.out.println("Skipping invalid user data: " + line);
                    continue;
                }
                String username = parts[0];
                String password = parts[1];
                String email = parts[2];
                String[] rolesArray = parts[3].split(";");
                User user = new User(username, password, email);
                for (String role : rolesArray) {
                    user.addRole(role);
                }
                users.add(user);
            }
        } catch (IOException e) {
            System.out.println("Error loading users from file: " + e.getMessage());
        }
    }

    public User findUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void logout() {
        loggedInUser = null;
    }
}
