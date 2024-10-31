package phase2;

import java.sql.*;
import java.util.*;

public class AccountManager {

    private DatabaseHelper dbHelper = DatabaseHelper.getInstance();

    public User createUser(String username, String password) {
        try {
            Connection conn = dbHelper.getConnection();
            String insertUser = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertUser);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            pstmt.close();
            return new User(username, password);
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            return null;
        }
    }

    public void InvitationCode(String code, Role role) {
        try {
            Connection conn = dbHelper.getConnection();
            String insertCode = "INSERT INTO invitation_codes (code, role) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertCode);
            pstmt.setString(1, code);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error generating invitation code: " + e.getMessage());
        }
    }

    public Role RoleFromInvitationCode(String code) {
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT role FROM invitation_codes WHERE code = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Role role = Role.valueOf(rs.getString("role"));
                rs.close();
                pstmt.close();
                return role;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving role from invitation code: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteInvitationCode(String code) {
        try {
            Connection conn = dbHelper.getConnection();
            String deleteCode = "DELETE FROM invitation_codes WHERE code = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteCode);
            pstmt.setString(1, code);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting invitation code: " + e.getMessage());
            return false;
        }
    }

    public User getUser(String username) {
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(username, rs.getString("password"));
                String roleQuery = "SELECT role FROM user_roles WHERE username = ?";
                PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
                rolePstmt.setString(1, username);
                ResultSet roleRs = rolePstmt.executeQuery();
                while (roleRs.next()) {
                    user.addRole(Role.valueOf(roleRs.getString("role")));
                }
                roleRs.close();
                rolePstmt.close();
                rs.close();
                pstmt.close();
                return user;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }

    public Map<String, User> listUsers() {
        Map<String, User> users = new HashMap<>();
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT * FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String username = rs.getString("username");
                User user = new User(username, rs.getString("password"));
                String roleQuery = "SELECT role FROM user_roles WHERE username = ?";
                PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
                rolePstmt.setString(1, username);
                ResultSet roleRs = rolePstmt.executeQuery();
                while (roleRs.next()) {
                    user.addRole(Role.valueOf(roleRs.getString("role")));
                }
                roleRs.close();
                rolePstmt.close();
                users.put(username, user);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error listing users: " + e.getMessage());
        }
        return users;
    }

    public void addRole(String username, Role role) {
        try {
            Connection conn = dbHelper.getConnection();
            String insertRole = "INSERT INTO user_roles (username, role) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertRole);
            pstmt.setString(1, username);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error adding role to user: " + e.getMessage());
        }
    }

    public void removeRole(String username, Role role) {
        try {
            Connection conn = dbHelper.getConnection();
            String deleteRole = "DELETE FROM user_roles WHERE username = ? AND role = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteRole);
            pstmt.setString(1, username);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error removing role from user: " + e.getMessage());
        }
    }
}
