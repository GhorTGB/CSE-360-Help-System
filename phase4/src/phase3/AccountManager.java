package phase3;

import java.sql.*;
import java.util.*;

public class AccountManager {
    private DatabaseHelper dbHelper;

    public AccountManager() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public User createUser(String username, String password) {
        try {
            Connection conn = dbHelper.getConnection();
            String hashedPassword = PasswordUtils.hashPassword(password);
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            pstmt.close();
            return new User(username, hashedPassword);
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }

    public User authenticateUser(String username, String password) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT password FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (PasswordUtils.verifyPassword(password, storedPassword)) {
                    User user = new User(username, storedPassword);
                    user.setRoles(getUserRoles(username));
                    rs.close();
                    pstmt.close();
                    return user;
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error authenticating user: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error verifying password: " + e.getMessage());
        }
        return null;
    }

    public boolean addRoleToUser(String username, Role role) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO user_roles (username, role) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            pstmt.close();
            return true;  
        } catch (SQLException e) {
            System.out.println("Error adding role to user: " + e.getMessage());
            return false; 
        }
    }

    

    public boolean removeRoleFromUser(String username, Role role) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM user_roles WHERE username = ? AND role = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error removing role from user: " + e.getMessage());
        }
		return false;
    }

    public Role getRoleFromInvitationCode(String code) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT role FROM invitation_codes WHERE code = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String roleStr = rs.getString("role");
                rs.close();
                pstmt.close();
                return Role.valueOf(roleStr);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving role from invitation code: " + e.getMessage());
        }
        return null;
    }

    public boolean generateInvitationCode(String code, Role role) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO invitation_codes (code, role) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            pstmt.setString(2, role.name());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error generating invitation code: " + e.getMessage());
        }
		return false;
    }

    public void deleteInvitationCode(String code) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM invitation_codes WHERE code = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error deleting invitation code: " + e.getMessage());
        }
    }

    public boolean createSpecialAccessGroup(String groupName, String adminUsername) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO special_access_groups (groupName) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
            pstmt.close();
            return addUserToGroup(groupName, adminUsername, "Admin Access");
        } catch (SQLException e) {
            System.out.println("Error creating special access group: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserToGroup(String groupName, String username, String accessLevel) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO group_users (groupName, username, accessLevel) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(2, username);
            pstmt.setString(3, accessLevel);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding user to group: " + e.getMessage());
            return false;  
        }
    }


    public boolean removeUserFromGroup(String groupName, String username) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM group_users WHERE groupName = ? AND username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error removing user from group: " + e.getMessage());
            return false;
        }
    }

    public String getGroupDetails(String groupName) {
        try {
            Connection conn = dbHelper.getConnection();
            String checkGroupSql = "SELECT groupName FROM special_access_groups WHERE groupName = ?";
            PreparedStatement checkGroupStmt = conn.prepareStatement(checkGroupSql);
            checkGroupStmt.setString(1, groupName);
            ResultSet groupRs = checkGroupStmt.executeQuery();

            if (!groupRs.next()) {
                groupRs.close();
                checkGroupStmt.close();
                return "Group '" + groupName + "' does not exist.";  
            }

            StringBuilder details = new StringBuilder();
            String sql = "SELECT username, accessLevel FROM group_users WHERE groupName = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            details.append("Group: ").append(groupName).append("\n");
            
            while (rs.next()) {
                details.append("Username: ").append(rs.getString("username"))
                       .append(", Access Level: ").append(rs.getString("accessLevel")).append("\n");
            }
            
            rs.close();
            pstmt.close();
            return details.toString();
            
        } catch (SQLException e) {
            System.out.println("Error retrieving group details: " + e.getMessage());
            return null; 
        }
    }


    public Map<String, User> listUsers() {
        Map<String, User> users = new HashMap<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT username, password FROM users";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                User user = new User(username, password);
                user.setRoles(getUserRoles(username));
                users.put(username, user);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error listing users: " + e.getMessage());
        }
        return users;
    }

    Set<Role> getUserRoles(String username) {
        Set<Role> roles = new HashSet<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT role FROM user_roles WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                roles.add(Role.valueOf(rs.getString("role")));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving user roles: " + e.getMessage());
        }
        return roles;
    }

    public User getUser(String username) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT password FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String password = rs.getString("password");
                User user = new User(username, password);
                user.setRoles(getUserRoles(username));
                return user;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving user: " + e.getMessage());
        }
        return null;
    }


    public void saveGenericMessage(String username, String message) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO messages (username, messageType, message) VALUES (?, 'Generic', ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error saving generic message: " + e.getMessage());
        }
    }

    public void saveSpecificMessage(String username, String message, List<String> searchHistory) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO messages (username, messageType, message) VALUES (?, 'Specific', ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            String history = String.join(", ", searchHistory);
            String fullMessage = message + "\nSearch History: " + history;
            pstmt.setString(1, username);
            pstmt.setString(2, fullMessage);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error saving specific message: " + e.getMessage());
        }
    }

    public int countAdmins() {
        int adminCount = 0;
        for (User user : listUsers().values()) {
            if (user.getRoles().contains(Role.ADMIN)) {
                adminCount++;
            }
        }
        return adminCount;
    }
    
    public boolean deleteSpecialAccessGroup(String groupName) {
        Connection conn = null;
        try {
            conn = dbHelper.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Delete from special_access_groups
            String sql1 = "DELETE FROM special_access_groups WHERE groupName = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, groupName);
            int rowsAffected1 = pstmt1.executeUpdate();
            pstmt1.close();

            if (rowsAffected1 == 0) {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }

            // Delete from group_users
            String sql2 = "DELETE FROM group_users WHERE groupName = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, groupName);
            pstmt2.executeUpdate();
            pstmt2.close();

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            System.out.println("Error deleting special access group: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.out.println("Error during rollback: " + ex.getMessage());
                }
            }
            return false;
        }
    }
    
    public boolean renameSpecialAccessGroup(String oldGroupName, String newGroupName) {
        Connection conn = null;
        try {
            conn = dbHelper.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Check if new group name already exists
            String checkSql = "SELECT groupName FROM special_access_groups WHERE groupName = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, newGroupName);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next()) {
                // New group name already exists
                checkRs.close();
                checkStmt.close();
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }
            checkRs.close();
            checkStmt.close();

            // Update special_access_groups table
            String sql1 = "UPDATE special_access_groups SET groupName = ? WHERE groupName = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, newGroupName);
            pstmt1.setString(2, oldGroupName);
            int rowsAffected1 = pstmt1.executeUpdate();
            pstmt1.close();

            if (rowsAffected1 == 0) {
                conn.rollback();
                conn.setAutoCommit(true);
                return false;
            }

            // Update group_users table
            String sql2 = "UPDATE group_users SET groupName = ? WHERE groupName = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, newGroupName);
            pstmt2.setString(2, oldGroupName);
            pstmt2.executeUpdate();
            pstmt2.close();

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            System.out.println("Error renaming special access group: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.out.println("Error during rollback: " + ex.getMessage());
                }
            }
            return false;
        }
    }
    
    public boolean addStudentToGroup(String groupName, String username) {
        return addUserToGroup(groupName, username, "View Access");
    }
    
    public Set<String> viewStudentsInGroup(String groupName) {
        Set<String> students = new HashSet<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT username FROM group_users WHERE groupName = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                students.add(rs.getString("username"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error viewing students in group: " + e.getMessage());
        }
        return students;
    }

    public boolean removeStudentFromGroup(String groupName, String username) {
        return removeUserFromGroup(groupName, username);
    }
    
    
    public boolean createGeneralGroup(String groupName, String adminUsername) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO general_groups (groupName) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            pstmt.executeUpdate();
            pstmt.close();
            return addUserToGroup(groupName, adminUsername, "Admin Access");
        } catch (SQLException e) {
            System.out.println("Error creating general group: " + e.getMessage());
            return false;
        }
    }
    public boolean userHasGroupRights(String username, String groupName) {
        User user = getUser(username);
        if (user == null) return false;
        return user.getGroups().contains(groupName) && user.getRoles().contains(Role.INSTRUCTOR);
    }

    public Set<String> getStudentsInGroup(String groupName) {
        
        Map<String, Set<String>> groups = new HashMap<>(); 
        if (groups.containsKey(groupName)) {
            return groups.get(groupName);
        } else {
            return Collections.emptySet(); 
        }
    }
    public List<String> getHelpMessages(String username) {
        List<String> messages = new ArrayList<>();
        try {
            Connection conn = DatabaseHelper.getInstance().getConnection();
            
            // Retrieve generic messages
            String genericSql = "SELECT message FROM help_messages WHERE username = ? AND type = 'generic'";
            PreparedStatement genericStmt = conn.prepareStatement(genericSql);
            genericStmt.setString(1, username);
            ResultSet genericRs = genericStmt.executeQuery();
            while (genericRs.next()) {
                messages.add(genericRs.getString("message"));
            }
            genericRs.close();
            genericStmt.close();
            
            // Retrieve specific messages
            String specificSql = "SELECT message FROM help_messages WHERE username = ? AND type = 'specific'";
            PreparedStatement specificStmt = conn.prepareStatement(specificSql);
            specificStmt.setString(1, username);
            ResultSet specificRs = specificStmt.executeQuery();
            while (specificRs.next()) {
                messages.add(specificRs.getString("message"));
            }
            specificRs.close();
            specificStmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving help messages: " + e.getMessage());
        }
        return messages;
    }


}