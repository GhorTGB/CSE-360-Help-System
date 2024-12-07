package phase3;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.*;

public class UserInterface extends Application {
    private User currentUser;
    private SessionManager sessionManager;
    private AccountManager accountManager;
    private ArticleManager articleManager;
    private Stage primaryStage;
    private String currentLevel = "all";
    private String currentGroup = "all";
    private List<String> searchHistory = new ArrayList<>();
    private List<Article> lastSearchResults = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    public UserInterface() {      // Constructor initializes the session, account, and article managers.

        sessionManager = new SessionManager();
        accountManager = new AccountManager();
        articleManager = new ArticleManager();
    }
//The start method sets up the initial scene based on whether any users exist.
// If no users are present, it prompts for Admin registration.

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Help System");

        // Ensure first login is an admin
        if (accountManager.listUsers().isEmpty()) {
            showAdminRegistration();
        } else {
            showMainMenu();
        }
    }
    // Displays the admin registration form for the first user
    // making sure it has atleast one admin
    private void showAdminRegistration() { //admin registration 
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        Label userLabel = new Label("Admin Username:");         // Labels and input fields for admin username and password

        TextField userField = new TextField();
        Label passLabel = new Label("Admin Password:");
        PasswordField passField = new PasswordField();
        Button registerButton = new Button("Register Admin");
        grid.add(userLabel, 0, 0);         // Add components to the grid

        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(registerButton, 1, 2);

        registerButton.setOnAction(e -> { // action for the register button
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            currentUser = accountManager.createUser(username, password); // Creating new user
            if (currentUser != null) {
                accountManager.addRoleToUser(username, Role.ADMIN); // Assigning the admin role to the new created user.
                currentUser.addRole(Role.ADMIN);
                sessionManager.loginUser(username, currentUser);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Admin registration successful.");
                successAlert.showAndWait();
                showUserMenu();                 // Navigate to the Admin Panel

            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Admin registration failed.");
                failureAlert.showAndWait();
            }
        });
        // Display the registration scene

        Scene registerScene = new Scene(grid, 400, 200);
        primaryStage.setScene(registerScene);
        primaryStage.show();
    }

    private void showMainMenu() { // Main menu with login, register and exiting application
        String[] options = {"Login", "Register", "Exit"};
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        for (String option : options) { // Main Menu option buttons
            Button button = new Button(option);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleMainOption(option));
            menu.getChildren().add(button);
        }
        Scene scene = new Scene(menu, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleMainOption(String option) {
        switch (option) {
            case "Login":
                login();
                break;
            case "Register":
                register();
                break;
            case "Exit":
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private void login() { // Login page for allowing users to authenticate
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        Label userLabel = new Label("Username:");         // Labels and input fields for username and password

        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);
        loginButton.setOnAction(e -> { // Login button to allow users in.
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            currentUser = accountManager.authenticateUser(username, password); // User Authentication for the user
            if (currentUser != null) {                 // Log the user into the session

                sessionManager.loginUser(username, currentUser);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Login successful."); // Navigation to menu based on the role.
                successAlert.showAndWait();
                showUserMenu();
            } else {
                // Show error if authentication fails
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Invalid credentials.");
                failureAlert.showAndWait();
            }
        });
        Scene loginScene = new Scene(grid, 300, 200);
        primaryStage.setScene(loginScene);
    }
    // Displays the Registration form allowing new users to create an account.
    // Users can optionally provide an invitation code to receive specific roles.

    private void register() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        Label userLabel = new Label("Username:");         // Labels and input fields for username, password, and invitation code

        TextField userField = new TextField();
        Label passLabel = new Label("Password:"); // Password field for register
        PasswordField passField = new PasswordField();
        Label codeLabel = new Label("Invitation Code (optional):"); // Invitation code to recieve user specific code
        TextField codeField = new TextField();
        Button registerButton = new Button("Register");
        grid.add(userLabel, 0, 0);         // Add components to the grid
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(codeLabel, 0, 2);
        grid.add(codeField, 1, 2);
        grid.add(registerButton, 1, 3);
        registerButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();
            String code = codeField.getText().trim();
            currentUser = accountManager.createUser(username, password);             // Attempt to create a new user
            if (currentUser != null) {
                if (!code.isEmpty()) {
                    Role role = accountManager.getRoleFromInvitationCode(code);                       // If an invitation code is provided, assign the corresponding role// If an invitation code is provided, assign the corresponding role

                    if (role != null) {
                        accountManager.addRoleToUser(username, role);
                        accountManager.deleteInvitationCode(code);
                    }
                }
                // Set the roles for the current user

                currentUser.setRoles(accountManager.getUserRoles(username));
                sessionManager.loginUser(username, currentUser);                 // Log the user into the session
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Registration successful.");
                successAlert.showAndWait();                 // Navigate to the appropriate user menu based on role
                showUserMenu();
            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Registration failed.");
                failureAlert.showAndWait();
            }
        });
        Scene registerScene = new Scene(grid, 400, 250);
        primaryStage.setScene(registerScene);
    }

    private void showUserMenu() {
        if (currentUser.getRoles().contains(Role.ADMIN)) {
            adminPanel(); // Navigate to the Admin Panel if the user has the ADMIN role
        } else if (currentUser.getRoles().contains(Role.INSTRUCTOR)) {
            instructorPanel();
        } else if (currentUser.getRoles().contains(Role.STUDENT)) {
            studentPanel();
        } else {
            guestPanel(); // Navigate to the Guest Panel if the user has no specific roles
        }
    }
    // The Admin Panel provides options for managing articles, users, groups, and logging out.
    private void adminPanel() {
        VBox adminMenu = new VBox(10);
        adminMenu.setPadding(new Insets(20));
        Label adminLabel = new Label("Admin Panel");
        Button manageArticlesButton = new Button("Manage Articles");
        Button manageUsersButton = new Button("Manage Users");
        Button manageGroupsButton = new Button("Manage Groups");
        Button logoutButton = new Button("Logout");

        manageArticlesButton.setOnAction(e -> manageArticles1());
        manageUsersButton.setOnAction(e -> manageUsers());
        manageGroupsButton.setOnAction(e -> manageGroups());
        logoutButton.setOnAction(e -> logout());

        adminMenu.getChildren().addAll(adminLabel, manageArticlesButton, manageUsersButton, manageGroupsButton, logoutButton);
        Scene adminScene = new Scene(adminMenu, 400, 400);
        primaryStage.setScene(adminScene);
    }

    // The Instructor Panel offers options for searching articles, managing articles and groups,

    private void instructorPanel() {
        VBox instructorMenu = new VBox(10);
        instructorMenu.setPadding(new Insets(20));
        Label instructorLabel = new Label("Instructor Panel");
        Button searchArticlesButton = new Button("Search Articles");
        Button manageArticlesButton = new Button("Manage Articles");
        Button manageGroupsButton = new Button("Manage Groups");
        Button manageSpecialGroupsButton = new Button("Manage Special Access Groups");
        Button setContentLevelButton = new Button("Set Content Level");
        Button setArticleGroupButton = new Button("Set Article Group");
        Button manageStudentsButton = new Button("Manage Students");
        Button backupRestoreButton = new Button("Backup/Restore Articles");
        Button logoutButton = new Button("Logout");

        searchArticlesButton.setOnAction(e -> searchArticles()); // Perform article search
        manageArticlesButton.setOnAction(e -> manageArticles());
        manageGroupsButton.setOnAction(e -> manageGeneralGroups());
        manageSpecialGroupsButton.setOnAction(e -> manageSpecialAccessGroups()); // Navigate to special access group management
        setContentLevelButton.setOnAction(e -> setContentLevel());
        setArticleGroupButton.setOnAction(e -> setArticleGroup());
        manageStudentsButton.setOnAction(e -> manageStudents());
        backupRestoreButton.setOnAction(e -> backupRestoreArticles());
        logoutButton.setOnAction(e -> logout());
        // Add all components to the instructor menu layout

        instructorMenu.getChildren().addAll(
                instructorLabel,
                searchArticlesButton,
                manageArticlesButton,
                manageGroupsButton,
                manageSpecialGroupsButton,
                setContentLevelButton,
                setArticleGroupButton,
                manageStudentsButton,
                backupRestoreButton,
                logoutButton);
        // Create a new scene with the instructor menu and set it on the primary stage

        Scene instructorScene = new Scene(instructorMenu, 400, 500);
        primaryStage.setScene(instructorScene);
    }
    // The Student Panel provides options for searching articles, setting content levels and article groups,

    private void studentPanel() {
        VBox studentMenu = new VBox(10);
        studentMenu.setPadding(new Insets(20));
        Label studentLabel = new Label("Student Panel");
        Button searchArticlesButton = new Button("Search Articles");
        Button setContentLevelButton = new Button("Set Content Level");
        Button setArticleGroupButton = new Button("Set Article Group");
        Button sendGenericMessageButton = new Button("Send Generic Message");
        Button sendSpecificMessageButton = new Button("Send Specific Message");
        Button logoutButton = new Button("Logout");
        // Set actions for each button to perform the respective functionalities

        searchArticlesButton.setOnAction(e -> searchArticles());
        setContentLevelButton.setOnAction(e -> setContentLevel());
        setArticleGroupButton.setOnAction(e -> setArticleGroup());
        sendGenericMessageButton.setOnAction(e -> sendGenericMessage());
        sendSpecificMessageButton.setOnAction(e -> sendSpecificMessage());
        logoutButton.setOnAction(e -> logout());

        studentMenu.getChildren().addAll(studentLabel, searchArticlesButton, setContentLevelButton, setArticleGroupButton, sendGenericMessageButton, sendSpecificMessageButton, logoutButton);
        Scene studentScene = new Scene(studentMenu, 400, 400);
        primaryStage.setScene(studentScene);
    }

    // The Guest Panel offers limited options such as searching articles, viewing articles by difficulty level,

    private void guestPanel() {
        String[] options = {"Search Articles", "View Articles by Difficulty Level", "Set Content Level", "Set Article Group", "Send Generic Message", "Send Specific Message", "Logout"}; // Array of options available to guest users
        VBox guestMenu = new VBox(10);
        guestMenu.setPadding(new Insets(20));
        for (String option : options) {
            Button button = new Button(option);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleGuestOption(option));
            guestMenu.getChildren().add(button);
        }
        Scene guestScene = new Scene(guestMenu, 400, 350);
        primaryStage.setScene(guestScene);
    }

    private void handleGuestOption(String option) {
        switch (option) {
            case "Search Articles":
                searchArticles();
                break;
            case "View Articles by Difficulty Level":
                viewArticlesByLevel();
                break;
            case "Set Content Level":
                setContentLevel();
                break;
            case "Set Article Group":
                setArticleGroup();
                break;
            case "Send Generic Message":
                sendGenericMessage();
                break;
            case "Send Specific Message":
                sendSpecificMessage();
                break;
            case "Logout":
                logout();
                break;
            default:
                break;
        }
    }

    private void manageGroups() { // Managing the speecial access groups.
        String[] options = {"Create Special Access Group", "Add User to Group", "Remove User from Group", "View Group Details", "Return to Previous Menu"};
        VBox groupPanel = new VBox(10);
        groupPanel.setPadding(new Insets(20));
        for (String option : options) {
            Button button = new Button(option);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleGroupOption(option));
            groupPanel.getChildren().add(button);
        }
        Scene groupScene = new Scene(groupPanel, 400, 400);
        primaryStage.setScene(groupScene);
    }

    private void handleGroupOption(String option) { // handling the groups and its functions
        switch (option) {
            case "Create Special Access Group":
                createSpecialAccessGroup();
                break;
            case "Add User to Group":
                addUserToGroup();
                break;
            case "Remove User from Group":
                removeUserFromGroup(); // Initiate the process of removing a user from a group
                break;
            case "View Group Details":
                viewGroupDetails();
                break;
            case "Return to Previous Menu":
                if (currentUser.getRoles().contains(Role.ADMIN)) {
                    adminPanel();
                } else if (currentUser.getRoles().contains(Role.INSTRUCTOR)) {
                    instructorPanel();
                }
                break;
            default:
                break;
        }
    }

    private void createSpecialAccessGroup() {//Creates a new Special Access Group by prompting the user for a group name.
        TextInputDialog groupDialog = new TextInputDialog(); // Create a dialog to input the group name
        groupDialog.setTitle("Create Special Access Group");
        groupDialog.setHeaderText("Enter Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();
        groupResult.ifPresent(groupName -> {
            boolean success = accountManager.createSpecialAccessGroup(groupName.trim(), currentUser.getUsername());
            if (success) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Special Access Group created successfully.");
                successAlert.showAndWait();
            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to create group or group already exists.");
                failureAlert.showAndWait();
            }
        });
    }

    private void addUserToGroup() {//Adds a user to a specific group by prompting for the group name, username, and access level.

        // Dialog to get the group name
        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("Add User to Group");
        groupDialog.setHeaderText("Enter Group Name:");
        
        Optional<String> groupResult = groupDialog.showAndWait();
        groupResult.ifPresent(groupName -> {
            if (groupName.trim().isEmpty()) {
                Alert groupAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
                groupAlert.showAndWait();
                return;
            }
            
            // Dialog to get the username
            TextInputDialog userDialog = new TextInputDialog();
            userDialog.setTitle("Add User to Group");
            userDialog.setHeaderText("Enter Username:");
            Optional<String> userResult = userDialog.showAndWait();
            
            userResult.ifPresent(username -> {
                if (username.trim().isEmpty()) {
                    Alert userAlert = new Alert(Alert.AlertType.WARNING, "Username cannot be empty.");
                    userAlert.showAndWait();
                    return;
                }
                
                // Dialog to select the access level for the user
                ChoiceDialog<String> roleDialog = new ChoiceDialog<>("View Access", "View Access", "Admin Access");
                roleDialog.setTitle("Select Access Level");
                roleDialog.setHeaderText("Select access level for the user:");
                Optional<String> roleResult = roleDialog.showAndWait();
                
                roleResult.ifPresent(accessLevel -> {
                    if (accessLevel.trim().isEmpty()) {
                        Alert roleAlert = new Alert(Alert.AlertType.WARNING, "Access level must be selected.");
                        roleAlert.showAndWait();
                        return;
                    }
                    
                    // Add user to the specified group with the selected role
                    boolean success = accountManager.addUserToGroup(groupName.trim(), username.trim(), accessLevel.trim());
                    if (success) {
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "User added to group successfully.");
                        successAlert.showAndWait();
                    } else {
                        Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to add user to group. Please ensure the group and user exist.");
                        failureAlert.showAndWait();
                    }
                });
            });
        });
    }

    private void removeUserFromGroup() { // Removes a user from a specific group by prompting for the group name and username.

        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("Remove User from Group");
        groupDialog.setHeaderText("Enter Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();
        groupResult.ifPresent(groupName -> {
            TextInputDialog userDialog = new TextInputDialog();
            userDialog.setTitle("Remove User from Group");
            userDialog.setHeaderText("Enter Username:");
            Optional<String> userResult = userDialog.showAndWait();
            userResult.ifPresent(username -> {
                boolean success = accountManager.removeUserFromGroup(groupName.trim(), username.trim());
                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "User removed from group successfully.");
                    successAlert.showAndWait();
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to remove user from group.");
                    failureAlert.showAndWait();
                }
            });
        });
    }

    private void viewGroupDetails() { // Views the details of a specific group by prompting the user for the group name.

        TextInputDialog groupDialog = new TextInputDialog();//Retrieves and displays group information using the AccountManager.
        groupDialog.setTitle("View Group Details");
        groupDialog.setHeaderText("Enter Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();
        groupResult.ifPresent(groupName -> {
            String details = accountManager.getGroupDetails(groupName.trim());
            if (details != null) {
                Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION, details);
                detailsAlert.showAndWait();
            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to retrieve group details.");
                failureAlert.showAndWait();
            }
        });
    }

    private void manageUsers() {//The User Management panel provides options to list users, add roles to users,
        String[] options = {"List Users", "Add Role to User", "Remove Role from User", "Generate Invitation Code", "Return to Previous Menu"};
        VBox userPanel = new VBox(10);
        userPanel.setPadding(new Insets(20));
        for (String option : options) {
            Button button = new Button(option);  // remove roles from users, generate invitation codes, and return to the previous menu.

            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleUserOption(option));
            userPanel.getChildren().add(button);
        }
        Scene userScene = new Scene(userPanel, 400, 300);
        primaryStage.setScene(userScene);
    }

    private void handleUserOption(String option) { 
        switch (option) {
            case "List Users": // list of all users
                listUsers();
                break;
            case "Add Role to User":
                addRoleToUser();
                break;
            case "Remove Role from User":
                removeRoleFromUser();
                break;
            case "Generate Invitation Code": // Generate an invitation code for a specific role
                generateInvitationCode();
                break;
            case "Return to Previous Menu":
                adminPanel();
                break;
            default:
                break;
        }
    }

    private void listUsers() {//Lists all users along with their roles in the system.
        Map<String, User> users = accountManager.listUsers();
        StringBuilder userList = new StringBuilder("Users:\n");//Retrieves user information from the AccountManager and displays it in an alert dialog.
        for (Map.Entry<String, User> entry : users.entrySet()) {
            userList.append("Username: ").append(entry.getKey()).append(", Roles: ").append(entry.getValue().getRoles()).append("\n");
        }
        Alert userAlert = new Alert(Alert.AlertType.INFORMATION, userList.toString());
        userAlert.showAndWait();
    }

    private void addRoleToUser() { // Adds a role (e.g., INSTRUCTOR, STUDENT) to a specific user by prompting for username and role.

        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Add Role to User");
        usernameDialog.setHeaderText("Enter username:");
        Optional<String> usernameResult = usernameDialog.showAndWait();
        usernameResult.ifPresent(username -> {
            ChoiceDialog<String> roleDialog = new ChoiceDialog<>("INSTRUCTOR", "STUDENT");
            roleDialog.setTitle("Select Role");
            roleDialog.setHeaderText("Select role to add:");
            Optional<String> roleResult = roleDialog.showAndWait();
            roleResult.ifPresent(roleStr -> {
                try {
                    Role role = Role.valueOf(roleStr);
                    User user = accountManager.getUser(username);
                    if (user != null) {
                        accountManager.addRoleToUser(username, role);
                        user.addRole(role);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Role added successfully.");
                        successAlert.showAndWait();
                    } else {
                        Alert userNotFoundAlert = new Alert(Alert.AlertType.ERROR, "User not found.");
                        userNotFoundAlert.showAndWait();
                    }
                } catch (IllegalArgumentException e) {
                    Alert invalidRoleAlert = new Alert(Alert.AlertType.ERROR, "Invalid role.");
                    invalidRoleAlert.showAndWait();
                }
            });
        });
    }

    private void removeRoleFromUser() { // Removes a specific role from a user by prompting for the username and the role to remove.

        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Remove Role from User");
        usernameDialog.setHeaderText("Enter username:");
        Optional<String> usernameResult = usernameDialog.showAndWait();     // Display the dialog and wait for user input

        usernameResult.ifPresent(username -> {
            ChoiceDialog<String> roleDialog = new ChoiceDialog<>("INSTRUCTOR", "STUDENT", "ADMIN");         // Create a choice dialog to select the role to remove

            roleDialog.setTitle("Select Role");
            roleDialog.setHeaderText("Select role to remove:");
            Optional<String> roleResult = roleDialog.showAndWait();
            roleResult.ifPresent(roleStr -> {
                try {
                    Role role = Role.valueOf(roleStr);
                    User user = accountManager.getUser(username);
                    if (user != null) {
                        if (role == Role.ADMIN && accountManager.countAdmins() <= 1) {                     // Check if attempting to remove the last remaining admin

                            Alert adminRemovalAlert = new Alert(Alert.AlertType.ERROR, "Cannot remove the last remaining admin.");
                            adminRemovalAlert.showAndWait();
                        } else {
                            accountManager.removeRoleFromUser(username, role);
                            user.removeRole(role);
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Role removed successfully.");
                            successAlert.showAndWait();
                        }
                    } else {
                        Alert userNotFoundAlert = new Alert(Alert.AlertType.ERROR, "User not found.");
                        userNotFoundAlert.showAndWait();
                    }
                } catch (IllegalArgumentException e) {
                    Alert invalidRoleAlert = new Alert(Alert.AlertType.ERROR, "Invalid role.");
                    invalidRoleAlert.showAndWait();
                }
            });
        });
    }

    private void generateInvitationCode() {//Generates an invitation code for a specified role by prompting the user to select a role.

        ChoiceDialog<String> roleDialog = new ChoiceDialog<>("INSTRUCTOR", "STUDENT");     // Create a choice dialog to select the role for which to generate the invitation code

        roleDialog.setTitle("Generate Invitation Code");
        roleDialog.setHeaderText("Select role for the invitation code:");
        Optional<String> roleResult = roleDialog.showAndWait();
        roleResult.ifPresent(roleStr -> {
            try {
                Role role = Role.valueOf(roleStr);             // Convert the selected role string to the Role enum

                String code = "INV-" + System.currentTimeMillis();
                accountManager.generateInvitationCode(code, role);             // Generate a unique invitation code using the current timestamp

                Alert codeAlert = new Alert(Alert.AlertType.INFORMATION, "Generated Invitation Code: " + code);
                codeAlert.showAndWait();
            } catch (IllegalArgumentException e) {             // Store the generated invitation code with the associated role in the account manager

                Alert invalidRoleAlert = new Alert(Alert.AlertType.ERROR, "Invalid role.");
                invalidRoleAlert.showAndWait();
            }
        });
    }

    private void manageArticles() {//Provides options for creating, updating, viewing, deleting articles, managing article groups,

        String[] options = {"Create Article", "Update Article", "View Article", "Delete Article", "Add Article to Group", "View Articles by Group", "Backup Articles", "Restore Articles", "List all Articles", "Return to Previous Menu"};
        VBox articlePanel = new VBox(10);
        articlePanel.setPadding(new Insets(20));
        for (String option : options) {
            Button button = new Button(option);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleArticleOption(option));
            articlePanel.getChildren().add(button);
        }
        Scene articleScene = new Scene(articlePanel, 400, 400);
        primaryStage.setScene(articleScene);
    }

    private void handleArticleOption(String option) { // Handles the actions corresponding to the selected option in the Article Management panel.

        switch (option) {
            case "Create Article":
                createArticle();
                break;
            case "Update Article":
                updateArticle();
                break;
            case "View Article":
                viewArticle(null);
                break;
            case "Delete Article":
                deleteArticle();
                break;
            case "Add Article to Group":
                addArticleToGroup();
                break;
            case "View Articles by Group":
                viewArticlesByGroup();
                break;
            case "Backup Articles":
                backupArticles();
                break;
            case "Restore Articles":
                restoreArticles();
                break;
            case "List all Articles":
                listAllArticles();
                break;
            case "Return to Previous Menu":
            	if (currentUser.getRoles().contains(Role.INSTRUCTOR)) {
                    instructorPanel();
                }
                showUserMenu();
                break;
            default:
                break;
        }
    }
    private void manageArticles1() { // Initializes and displays an alternative Article Management panel (manageArticles1).

        String[] options = {"Create Article", "Delete Article", "Add Article to Group", "View Articles by Group", "Backup Articles", "Restore Articles", "List all Articles", "Return to Previous Menu"}; // Array of options available in the alternative Article Management panel
        VBox articlePanel = new VBox(10);
        articlePanel.setPadding(new Insets(20));
        for (String option : options) {
            Button button = new Button(option);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleArticleOption(option));
            articlePanel.getChildren().add(button);
        }
        Scene articleScene = new Scene(articlePanel, 400, 400);
        primaryStage.setScene(articleScene);
    }

	@SuppressWarnings("unused")
	private void handleArticleOption1(String option) {//Handles the actions corresponding to the selected option in the alternative Article Management panel.

        switch (option) {
            case "Create Article":
                createArticle();
                break;
            case "Delete Article":
                deleteArticle();
                break;
            case "Add Article to Group":
                addArticleToGroup();
                break;
            case "View Articles by Group":
                viewArticlesByGroup();
                break;
            case "Backup Articles":
                backupArticles();
                break;
            case "Restore Articles":
                restoreArticles();
                break;
            case "List all Articles":
                listAllArticles();
                break;
            case "Return to Previous Menu":
            	if (currentUser.getRoles().contains(Role.ADMIN)) {
                    adminPanel();
                } 
                showUserMenu();
                break;
            default:
                break;
        }
    }
    
    private void createArticle() { // Creates a new article by collecting necessary information from the user through various input dialogs.

        GridPane articleGrid = new GridPane();
        articleGrid.setPadding(new Insets(20));
        articleGrid.setHgap(10);
        articleGrid.setVgap(10);
        Label levelLabel = new Label("Level:");
        TextField levelField = new TextField();//Validates the inputs and interacts with the ArticleManager to store the new article.

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField();
        Label descLabel = new Label("Short Description:");
        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        Label keywordsLabel = new Label("Keywords (comma-separated):");
        TextField keywordsField = new TextField();
        Label bodyLabel = new Label("Body:");
        TextArea bodyArea = new TextArea();
        bodyArea.setPrefRowCount(5);
        Label linksLabel = new Label("Links (comma-separated):");
        TextField linksField = new TextField();
        CheckBox sensitiveCheck = new CheckBox("Sensitive");
        Button submitButton = new Button("Submit");
        articleGrid.add(levelLabel, 0, 0);
        articleGrid.add(levelField, 1, 0);
        articleGrid.add(titleLabel, 0, 1);
        articleGrid.add(titleField, 1, 1);
        articleGrid.add(descLabel, 0, 2);
        articleGrid.add(descArea, 1, 2);
        articleGrid.add(keywordsLabel, 0, 3);
        articleGrid.add(keywordsField, 1, 3);
        articleGrid.add(bodyLabel, 0, 4);
        articleGrid.add(bodyArea, 1, 4);
        articleGrid.add(linksLabel, 0, 5);
        articleGrid.add(linksField, 1, 5);
        articleGrid.add(sensitiveCheck, 0, 6);
        articleGrid.add(submitButton, 0, 7);
        submitButton.setOnAction(e -> {
            String level = levelField.getText().trim();
            String title = titleField.getText().trim();
            String shortDescription = descArea.getText().trim();
            List<String> keywords = Arrays.asList(keywordsField.getText().trim().split(","));
            String body = bodyArea.getText().trim();
            List<String> links = Arrays.asList(linksField.getText().trim().split(","));
            boolean sensitive = sensitiveCheck.isSelected();
            if (title.isEmpty() || body.isEmpty()) {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Title and body cannot be empty.");
                emptyAlert.showAndWait();
                return;
            }
            Article article = new Article(0, level, title, shortDescription, keywords, body, links, sensitive);
            article.setAuthor(currentUser.getUsername());
            long articleId = articleManager.createArticle(article);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Article created with ID: " + articleId);
            successAlert.showAndWait();
            manageArticles();
        });
        Scene createArticleScene = new Scene(articleGrid, 600, 500);
        primaryStage.setScene(createArticleScene);
    }

    private void updateArticle() { // Initiates the article update process by prompting the user for the Article ID to update.

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Update Article");
        idDialog.setHeaderText("Enter Article ID to update:");
        Optional<String> idResult = idDialog.showAndWait();
        idResult.ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());
                Article article = articleManager.getArticle(id);
                if (article != null) {
                    showUpdateArticleForm(article);
                } else {
                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR, "No article found with ID: " + id);
                    notFoundAlert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert invalidIdAlert = new Alert(Alert.AlertType.ERROR, "Invalid Article ID.");
                invalidIdAlert.showAndWait();
            }
        });
    }

    private void showUpdateArticleForm(Article article) { // Displays the update form for a specific article, pre-populating fields with existing data.

        GridPane articleGrid = new GridPane();
        articleGrid.setPadding(new Insets(20));
        articleGrid.setHgap(10);
        articleGrid.setVgap(10);
        Label levelLabel = new Label("Level:");     // Create labels and input fields pre-populated with the article's existing details

        TextField levelField = new TextField(article.getLevel()); // Allows the user to modify article details and submit the updated information.

        Label titleLabel = new Label("Title:");
        TextField titleField = new TextField(article.getTitle());
        Label descLabel = new Label("Short Description:");
        TextArea descArea = new TextArea(article.getShortDescription());
        descArea.setPrefRowCount(3);
        Label keywordsLabel = new Label("Keywords (comma-separated):");
        TextField keywordsField = new TextField(String.join(",", article.getKeywords()));
        Label bodyLabel = new Label("Body:");
        TextArea bodyArea = new TextArea(article.getBody());
        bodyArea.setPrefRowCount(5);
        Label linksLabel = new Label("Links (comma-separated):");
        TextField linksField = new TextField(String.join(",", article.getLinks()));
        CheckBox sensitiveCheck = new CheckBox("Sensitive"); // Checkbox to mark the article as sensitive
        sensitiveCheck.setSelected(article.isSensitive());
        Button updateButton = new Button("Update Article"); // Button to submit the updated article
        
        articleGrid.add(levelLabel, 0, 0);
        articleGrid.add(levelField, 1, 0);
        articleGrid.add(titleLabel, 0, 1);
        articleGrid.add(titleField, 1, 1);
        articleGrid.add(descLabel, 0, 2);
        articleGrid.add(descArea, 1, 2);
        articleGrid.add(keywordsLabel, 0, 3);
        articleGrid.add(keywordsField, 1, 3);
        articleGrid.add(bodyLabel, 0, 4);
        articleGrid.add(bodyArea, 1, 4);
        articleGrid.add(linksLabel, 0, 5);
        articleGrid.add(linksField, 1, 5);
        articleGrid.add(sensitiveCheck, 1, 6);
        articleGrid.add(updateButton, 1, 7);
        updateButton.setOnAction(e -> {
            String level = levelField.getText().trim();
            String title = titleField.getText().trim();
            String shortDescription = descArea.getText().trim();
            List<String> keywords = Arrays.asList(keywordsField.getText().trim().split(","));
            String body = bodyArea.getText().trim();
            List<String> links = Arrays.asList(linksField.getText().trim().split(","));
            boolean sensitive = sensitiveCheck.isSelected();
            if (title.isEmpty() || body.isEmpty()) {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Title and body cannot be empty.");
                emptyAlert.showAndWait();
                return;
            }
            Article updatedArticle = new Article(article.getId(), level, title, shortDescription, keywords, body, links, sensitive);
            updatedArticle.setAuthor(article.getAuthor());
            articleManager.updateArticle(article.getId(), updatedArticle);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Article updated successfully.");
            successAlert.showAndWait();
            manageArticles();
        });
        Scene updateArticleScene = new Scene(articleGrid, 600, 500);
        primaryStage.setScene(updateArticleScene);
    }

    private void viewArticle(Article article2) { // Displays the details of a specific article by prompting for its ID.

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("View Article");
        idDialog.setHeaderText("Enter Article ID:");
        Optional<String> idResult = idDialog.showAndWait();
        idResult.ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());

                // Check if the article exists in the database
                Article article = articleManager.getArticle(id);             // Check if the article exists in the database


                // If the article is null, it means the article does not exist, or has been deleted
                if (article != null) {
                    String bodyContent = article.getBody();
                    boolean hasAccess = false;

                    // Check access based on sensitivity and user role
                    if (article.isSensitive()) {
                        if ((currentUser.getRoles().contains(Role.INSTRUCTOR) || currentUser.getRoles().contains(Role.ADMIN))
                                && articleManager.userHasAccessToArticle(currentUser.getUsername(), id)) {
                            hasAccess = true;
                        } else {
                            bodyContent = "You do not have permission to view this content.";
                        }
                    } else {
                        hasAccess = true;
                    }

                    // Show article details only if the user has access
                    if (hasAccess) {
                        StringBuilder articleDetails = new StringBuilder();
                        articleDetails.append("ID: ").append(article.getId()).append("\n");
                        articleDetails.append("Level: ").append(article.getLevel()).append("\n");
                        articleDetails.append("Title: ").append(article.getTitle()).append("\n");
                        articleDetails.append("Short Description: ").append(article.getShortDescription()).append("\n");
                        articleDetails.append("Keywords: ").append(article.getKeywords()).append("\n");
                        articleDetails.append("Body: ").append(bodyContent).append("\n");
                        articleDetails.append("Links: ").append(article.getLinks()).append("\n");
                        articleDetails.append("Groups: ").append(article.getGroups()).append("\n");
                        articleDetails.append("Sensitive: ").append(article.isSensitive()).append("\n");
                        articleDetails.append("Author: ").append(article.getAuthor()).append("\n");
                        Alert articleAlert = new Alert(Alert.AlertType.INFORMATION, articleDetails.toString());
                        articleAlert.getDialogPane().setPrefWidth(500);
                        articleAlert.showAndWait();
                    } else {
                        Alert accessDeniedAlert = new Alert(Alert.AlertType.ERROR, "You do not have permission to view this article.");
                        accessDeniedAlert.showAndWait();
                    }
                } else {
                    // Article was not found (either deleted or doesn't exist)
                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR, "No article found with ID: " + id);
                    notFoundAlert.showAndWait();
                }
            } catch (NumberFormatException e) {
                Alert invalidIdAlert = new Alert(Alert.AlertType.ERROR, "Invalid Article ID.");
                invalidIdAlert.showAndWait();
            }
        });
        promptArticleView();
    }
    
    /**
     * Deletes a specific article by prompting for its ID.
     * Validates the input and interacts with the ArticleManager to perform the deletion.
     */
    private void deleteArticle() { 
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Delete Article");
        idDialog.setHeaderText("Enter Article ID to delete:");
        Optional<String> idResult = idDialog.showAndWait();
        idResult.ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());
                articleManager.deleteArticle(id);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Article deleted successfully.");
                successAlert.showAndWait();
            } catch (NumberFormatException e) {
                Alert invalidIdAlert = new Alert(Alert.AlertType.ERROR, "Invalid Article ID.");
                invalidIdAlert.showAndWait();
            }
        });
    }

    private void addArticleToGroup() { // Adds an article to a specific group by prompting for the article ID and group name.

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Add Article to Group");
        idDialog.setHeaderText("Enter Article ID:");
        Optional<String> idResult = idDialog.showAndWait();
        idResult.ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());
                TextInputDialog groupDialog = new TextInputDialog();
                groupDialog.setTitle("Group Name");
                groupDialog.setHeaderText("Enter Group Name:");
                Optional<String> groupResult = groupDialog.showAndWait();
                groupResult.ifPresent(groupName -> {
                    articleManager.addArticleToGroup(id, groupName.trim());
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Article added to group successfully.");
                    successAlert.showAndWait();
                });
            } catch (NumberFormatException e) {
                Alert invalidIdAlert = new Alert(Alert.AlertType.ERROR, "Invalid Article ID.");
                invalidIdAlert.showAndWait();
            }
        });
    }

    private void viewArticlesByGroup() {//Views articles filtered by a specific group by prompting for the group name.

        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("View Articles by Group");
        groupDialog.setHeaderText("Enter Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();
        groupResult.ifPresent(groupName -> {
            Set<Article> articles = articleManager.getArticlesByGroup(groupName.trim());
            if (articles.isEmpty()) {
                Alert noArticlesAlert = new Alert(Alert.AlertType.INFORMATION, "No articles found in this group.");
                noArticlesAlert.showAndWait();
            } else {
                StringBuilder articleList = new StringBuilder("Articles in Group '" + groupName + "':\n");
                int index = 1;
                for (Article article : articles) {
                    articleList.append(index++).append(". ID: ").append(article.getId()).append(", Title: ").append(article.getTitle()).append("\n");
                }
                // Create and display an information alert with the list of articles

                Alert articlesAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
                articlesAlert.showAndWait();
            }
        });
    }
    
    private void listAllArticles() { // Lists all articles present in the system and displays them to the user.

        List<Article> articles = articleManager.listAllArticles();
        if (articles.isEmpty()) {
            Alert noArticlesAlert = new Alert(Alert.AlertType.INFORMATION, "No articles available.");
            noArticlesAlert.showAndWait();
        } else {
            StringBuilder articleList = new StringBuilder("All Articles:\n");
            int index = 1;
            for (Article article : articles) {
                articleList.append(index++).append(". ID: ").append(article.getId()).append(", Title: ").append(article.getTitle()).append(", Level: ").append(article.getLevel()).append("\n");
            }
            Alert articlesAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
            articlesAlert.showAndWait();
        }
    }

    private void searchArticles() { // Validates the input and retrieves the corresponding article, storing search results and history.

        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Search Articles");
        idDialog.setHeaderText("Enter the Article ID to search:");
        Optional<String> idResult = idDialog.showAndWait();

        idResult.ifPresent(idStr -> {
            try {
                // Attempt to parse the input as an article ID
                long id = Long.parseLong(idStr.trim());
                Article article = articleManager.getArticle(id);
                if (article != null) {
                    lastSearchResults = Collections.singletonList(article);
                } else {
                    lastSearchResults = Collections.emptyList();
                }

                // Store the search ID in history
                searchHistory.add(idStr.trim());

                // Display search results
                displaySearchResults();
            } catch (NumberFormatException e) {
                Alert invalidIdAlert = new Alert(Alert.AlertType.ERROR, "Invalid Article ID. Please enter a valid number.");
                invalidIdAlert.showAndWait();
                searchArticles(); // Prompt for a new search if input is invalid
            }
        });
    }





    private void setContentLevel() {     // Create a choice dialog with predefined content level options

        ChoiceDialog<String> levelDialog = new ChoiceDialog<>("all", "beginner", "intermediate", "advanced", "expert");
        levelDialog.setTitle("Set Content Level");
        levelDialog.setHeaderText("Select content level:");
        Optional<String> levelResult = levelDialog.showAndWait();

        levelResult.ifPresent(level -> {
            currentLevel = level.trim();
            Alert levelSetAlert = new Alert(Alert.AlertType.INFORMATION, "Content level set to: " + currentLevel);
            levelSetAlert.showAndWait();
        });
    }

    private void setArticleGroup() { // Sets the article group filter based on user input by prompting for a group name or selecting 'all'.

        TextInputDialog groupDialog = new TextInputDialog("all");
        groupDialog.setTitle("Set Article Group");
        groupDialog.setHeaderText("Enter group name or 'all' for all groups:");
        Optional<String> groupResult = groupDialog.showAndWait();

        groupResult.ifPresent(groupName -> {
            currentGroup = groupName.trim();
            Alert groupSetAlert = new Alert(Alert.AlertType.INFORMATION, "Article group set to: " + currentGroup);
            groupSetAlert.showAndWait();
        });
    }


    private void sendGenericMessage() { // Sends a generic message to the help system by prompting the user for input.

        TextInputDialog messageDialog = new TextInputDialog();
        messageDialog.setTitle("Send Generic Message");
        messageDialog.setHeaderText("Enter your message:");
        Optional<String> messageResult = messageDialog.showAndWait();

        messageResult.ifPresent(message -> {
            accountManager.saveGenericMessage(currentUser.getUsername(), message.trim());
            Alert messageSentAlert = new Alert(Alert.AlertType.INFORMATION, "Message sent to the help system.");
            messageSentAlert.showAndWait();
        });
    }

    private void sendSpecificMessage() {//Sends a specific message to the help system by prompting the user for a detailed description.

        TextInputDialog messageDialog = new TextInputDialog();
        messageDialog.setTitle("Send Specific Message");
        messageDialog.setHeaderText("Describe what you need and cannot find:");
        Optional<String> messageResult = messageDialog.showAndWait();

        messageResult.ifPresent(message -> {
            accountManager.saveSpecificMessage(currentUser.getUsername(), message.trim(), searchHistory);
            Alert messageSentAlert = new Alert(Alert.AlertType.INFORMATION, "Message sent to the help system.");
            messageSentAlert.showAndWait();
        });
    }

    private void viewArticlesByLevel() { // Views articles filtered by their difficulty level by prompting the user to select a level.

        ChoiceDialog<String> levelDialog = new ChoiceDialog<>("beginner", "intermediate", "advanced", "expert");
        levelDialog.setTitle("View Articles by Difficulty Level");
        levelDialog.setHeaderText("Select difficulty level:");
        Optional<String> levelResult = levelDialog.showAndWait();
        levelResult.ifPresent(level -> {
            List<Article> results = articleManager.getArticlesByLevel(level.trim());         // Retrieve the list of articles that match the selected difficulty level using the ArticleManager

            if (results.isEmpty()) {
                Alert noResultsAlert = new Alert(Alert.AlertType.INFORMATION, "No articles found at the '" + level + "' level.");
                noResultsAlert.showAndWait();
            } else {
                StringBuilder articleList = new StringBuilder("Articles at '" + level + "' level:\n");
                int index = 1;
                for (Article article : results) {
                    articleList.append(index++).append(". ID: ").append(article.getId()).append(", Title: ").append(article.getTitle()).append("\n");
                }
                Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
                resultsAlert.showAndWait();
            }
        });
    }

    private void backupArticles() { // Backs up articles based on the user's selection by prompting for a filename and backup type.

        TextInputDialog filenameDialog = new TextInputDialog("backup.sql");
        filenameDialog.setTitle("Backup Articles");
        filenameDialog.setHeaderText("Enter filename to backup articles to:");
        Optional<String> filenameResult = filenameDialog.showAndWait();
        filenameResult.ifPresent(filename -> {
            ChoiceDialog<String> backupTypeDialog = new ChoiceDialog<>("All Articles", "All Articles", "Specific Group", "Special Access Groups");
            backupTypeDialog.setTitle("Backup Type");
            backupTypeDialog.setHeaderText("Select backup type:");
            Optional<String> backupTypeResult = backupTypeDialog.showAndWait();
            backupTypeResult.ifPresent(backupType -> {
                if (backupType.equals("All Articles")) {
                    articleManager.backupArticles(0);
                    Alert backupAlert = new Alert(Alert.AlertType.INFORMATION, "Articles backed up successfully to " + filename); // Inform the user that the backup was successful

                    backupAlert.showAndWait();
                } else if (backupType.equals("Specific Group")) { // If backing up articles from a specific group
                    TextInputDialog groupDialog = new TextInputDialog();
                    groupDialog.setTitle("Group Name");
                    groupDialog.setHeaderText("Enter Group Name to backup:");
                    Optional<String> groupResult = groupDialog.showAndWait();
                    groupResult.ifPresent(groupName -> {
                        Set<String> groupsToBackup = new HashSet<>();
                        groupsToBackup.add(groupName.trim());
                        articleManager.backupArticles(0);
                        Alert backupAlert = new Alert(Alert.AlertType.INFORMATION, "Articles in group '" + groupName + "' backed up successfully to " + filename);
                        backupAlert.showAndWait();
                    });
                } else if (backupType.equals("Special Access Groups")) {
                    articleManager.backupSpecialAccessGroups(filename.trim());
                    Alert backupAlert = new Alert(Alert.AlertType.INFORMATION, "Special access groups backed up successfully to " + filename);
                    backupAlert.showAndWait();
                }
            });
        });
    }


    private void restoreArticles() { // Restores articles from a backup file by prompting for the filename and confirming whether to remove existing articles.

        TextInputDialog filenameDialog = new TextInputDialog("backup.sql");
        filenameDialog.setTitle("Restore Articles");
        filenameDialog.setHeaderText("Enter filename to restore articles from:");
        Optional<String> filenameResult = filenameDialog.showAndWait();
        filenameResult.ifPresent(filename -> {
            articleManager.restoreArticles(0);        // Perform the restoration of articles using the ArticleManager

            Alert restoreAlert = new Alert(Alert.AlertType.INFORMATION, "Articles restored successfully from " + filename);
            restoreAlert.showAndWait();
        });
    }

    private void displaySearchResults() { // Displays the results of the latest article search, including a summary and a list of matching articles.

        StringBuilder searchSummary = new StringBuilder();
        searchSummary.append("Current Group: ").append(currentGroup).append("\n");

        // Count articles by level (will be 1 or 0 for a single search result)
        Map<String, Long> levelCounts = lastSearchResults.stream()
                .collect(Collectors.groupingBy(Article::getLevel, Collectors.counting()));

        // Display the number of articles that match each level
        for (String level : Arrays.asList("beginner", "intermediate", "advanced", "expert")) {
            searchSummary.append("Number of ").append(level).append(" articles: ").append(levelCounts.getOrDefault(level, 0L)).append("\n");
        }

        // Display the matching articles in a short form
        searchSummary.append("\nMatching Articles:\n");
        for (int i = 0; i < lastSearchResults.size(); i++) {
            Article article = lastSearchResults.get(i);
            searchSummary.append(i + 1).append(". Title: ").append(article.getTitle())
                    .append(", Author: ").append(article.getAuthor())
                    .append(", Abstract: ").append(article.getShortDescription()).append("\n");
        }

        // Show the search summary to the user
        Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, searchSummary.toString());
        resultsAlert.getDialogPane().setPrefWidth(600);
        resultsAlert.showAndWait();

        // Prompt the user to choose a post-search action
        if (!lastSearchResults.isEmpty()) {
            promptPostSearchAction();
        }
    }

    private void promptPostSearchAction() { // Prompts the user for an action after displaying search results.

        TextInputDialog actionDialog = new TextInputDialog();
        actionDialog.setTitle("Post Search Action");
        actionDialog.setHeaderText("Enter sequence number to view an article, type 'search' for a new search, or type 'exit' to return to the menu:");
        Optional<String> actionResult = actionDialog.showAndWait();

        actionResult.ifPresent(input -> {
            if (input.equalsIgnoreCase("search")) {
                searchArticles();
            } else if (input.equalsIgnoreCase("exit")) {
                studentPanel(); // or showMainMenu() based on your application flow
            } else {
                try {
                    int sequenceNumber = Integer.parseInt(input.trim());
                    if (sequenceNumber >= 1 && sequenceNumber <= lastSearchResults.size()) {
                        viewArticle(lastSearchResults.get(sequenceNumber - 1));
                        promptPostArticleAction(); // After viewing the article, prompt for the next action
                    } else {
                        Alert invalidNumberAlert = new Alert(Alert.AlertType.ERROR, "Invalid sequence number.");
                        invalidNumberAlert.showAndWait();
                        promptPostSearchAction();
                    }
                } catch (NumberFormatException e) {
                    Alert invalidInputAlert = new Alert(Alert.AlertType.ERROR, "Invalid input.");
                    invalidInputAlert.showAndWait();
                    promptPostSearchAction();
                }
            }
        });
    }

    private void promptPostArticleAction() {//Prompts the user for an action after viewing a specific article.

        TextInputDialog actionDialog = new TextInputDialog();
        actionDialog.setTitle("Post Article View Action");
        actionDialog.setHeaderText("Enter 'search' for a new search, enter sequence number to view a different article, or type 'exit' to return to the menu:");
        Optional<String> actionResult = actionDialog.showAndWait();

        actionResult.ifPresent(input -> {
            if (input.equalsIgnoreCase("search")) {
                searchArticles();
            } else if (input.equalsIgnoreCase("exit")) {
                studentPanel(); // or showMainMenu() based on your application flow
            } else {
                try {
                    int sequenceNumber = Integer.parseInt(input.trim());
                    if (sequenceNumber >= 1 && sequenceNumber <= lastSearchResults.size()) {
                        viewArticle(lastSearchResults.get(sequenceNumber - 1));
                        promptPostArticleAction(); // Keep prompting after viewing an article
                    } else {
                        Alert invalidNumberAlert = new Alert(Alert.AlertType.ERROR, "Invalid sequence number.");
                        invalidNumberAlert.showAndWait();
                        promptPostArticleAction();
                    }
                } catch (NumberFormatException e) {
                    Alert invalidInputAlert = new Alert(Alert.AlertType.ERROR, "Invalid input.");
                    invalidInputAlert.showAndWait();
                    promptPostArticleAction();
                }
            }
        });
    }


    private void promptArticleView() {  // Create a dialog to input the desired action after search results are displayed

        TextInputDialog viewDialog = new TextInputDialog();
        viewDialog.setTitle("View Article");
        viewDialog.setHeaderText("Enter sequence number to view an article in detail, or type 'search' for a new search:");
        Optional<String> viewResult = viewDialog.showAndWait();

        viewResult.ifPresent(input -> {
            if (input.equalsIgnoreCase("search")) {
                searchArticles();
            } else {
                try {
                    int sequenceNumber = Integer.parseInt(input.trim());
                    if (sequenceNumber >= 1 && sequenceNumber <= lastSearchResults.size()) {
                        viewArticle(null);
                    } else {
                        Alert invalidNumberAlert = new Alert(Alert.AlertType.ERROR, "Invalid sequence number.");
                        invalidNumberAlert.showAndWait();
                        promptArticleView();
                    }
                } catch (NumberFormatException e) {
                    Alert invalidInputAlert = new Alert(Alert.AlertType.ERROR, "Invalid input.");
                    invalidInputAlert.showAndWait();
                    promptArticleView();
                }
            }
        });
    }
    private void manageGeneralGroups() {  // Prompts the user for an action after viewing a specific article.

        VBox groupPanel = new VBox(10);
        groupPanel.setPadding(new Insets(20));
        Label groupLabel = new Label("General Group Management");
        Button createGroupButton = new Button("Create Group");
        Button viewGroupButton = new Button("View Group");
        Button editGroupButton = new Button("Edit Group");
        Button deleteGroupButton = new Button("Delete Group");
        Button returnButton = new Button("Return to Instructor Menu");

        createGroupButton.setOnAction(e -> createGroup());
        viewGroupButton.setOnAction(e -> viewGroup());
        editGroupButton.setOnAction(e -> editGroup());
        deleteGroupButton.setOnAction(e -> deleteGroup());
        returnButton.setOnAction(e -> instructorPanel());

        groupPanel.getChildren().addAll(groupLabel, createGroupButton, viewGroupButton, editGroupButton, deleteGroupButton, returnButton);
        Scene groupScene = new Scene(groupPanel, 400, 400);
        primaryStage.setScene(groupScene);
    }

    private void manageSpecialAccessGroups() { // Prompts the user to view an article by entering a sequence number or perform a new search.

        VBox specialGroupPanel = new VBox(10);
        specialGroupPanel.setPadding(new Insets(20));
        Label specialGroupLabel = new Label("Special Access Group Management");
        Button createSpecialGroupButton = new Button("Create Special Group");
        Button viewSpecialGroupButton = new Button("View Special Group");
        Button editSpecialGroupButton = new Button("Edit Special Group");
        Button deleteSpecialGroupButton = new Button("Delete Special Group");
        Button returnButton = new Button("Return to Instructor Menu");

        createSpecialGroupButton.setOnAction(e -> createSpecialAccessGroup());
        viewSpecialGroupButton.setOnAction(e -> viewSpecialGroup());
        editSpecialGroupButton.setOnAction(e -> editSpecialGroup());
        deleteSpecialGroupButton.setOnAction(e -> deleteSpecialGroup());
        returnButton.setOnAction(e -> instructorPanel());

        specialGroupPanel.getChildren().addAll(specialGroupLabel, createSpecialGroupButton, viewSpecialGroupButton, editSpecialGroupButton, deleteSpecialGroupButton, returnButton);
        Scene specialGroupScene = new Scene(specialGroupPanel, 400, 400);
        primaryStage.setScene(specialGroupScene);
    }

 private void deleteSpecialGroup() { // Initializes and displays the General Group Management panel.

     TextInputDialog groupDialog = new TextInputDialog();
     groupDialog.setTitle("Delete Special Group");
     groupDialog.setHeaderText("Enter Special Group Name to Delete:");
     Optional<String> groupResult = groupDialog.showAndWait();

     groupResult.ifPresent(groupName -> {
         if (!groupName.trim().isEmpty()) {
             boolean success = accountManager.deleteSpecialAccessGroup(groupName.trim());
             if (success) {
                 Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Special Group deleted successfully.");
                 successAlert.showAndWait();
             } else {
                 Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to delete special group. Please ensure the group exists.");
                 failureAlert.showAndWait();
             }
         } else {
             Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
             emptyAlert.showAndWait();
         }
     });
 }

//Edits the name of an existing special access group by prompting the user for the current and new group names.

 private void editSpecialGroup() {
     TextInputDialog oldGroupNameDialog = new TextInputDialog();
     oldGroupNameDialog.setTitle("Edit Special Group");
     oldGroupNameDialog.setHeaderText("Enter Existing Group Name:");
     Optional<String> oldGroupResult = oldGroupNameDialog.showAndWait();

     oldGroupResult.ifPresent(oldGroupName -> { // If an existing group name is entered
         if (!oldGroupName.trim().isEmpty()) {
             TextInputDialog newGroupNameDialog = new TextInputDialog();
             newGroupNameDialog.setTitle("Edit Special Group");
             newGroupNameDialog.setHeaderText("Enter New Group Name:");
             Optional<String> newGroupResult = newGroupNameDialog.showAndWait();

             newGroupResult.ifPresent(newGroupName -> { // If a new group name is entered
                 if (!newGroupName.trim().isEmpty()) {
                     boolean success = accountManager.renameSpecialAccessGroup(oldGroupName.trim(), newGroupName.trim());
                     if (success) {
                         Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Special group name updated successfully.");
                         successAlert.showAndWait();
                     } else {
                         Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to update group name. Please try again.");
                         failureAlert.showAndWait();
                     }
                 } else {
                     Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "New group name cannot be empty.");
                     emptyAlert.showAndWait();
                 }
             });
         } else {
             Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Existing group name cannot be empty.");
             emptyAlert.showAndWait();
         }
     });
 }

 // Adds a student to a specified group by prompting for the group name and student username.

 private void addStudentToGroup() {
	    TextInputDialog groupDialog = new TextInputDialog();
	    groupDialog.setTitle("Add Student to Group");
	    groupDialog.setHeaderText("Enter Group Name:");
	    Optional<String> groupResult = groupDialog.showAndWait();

	    groupResult.ifPresent(groupName -> {
	        if (!groupName.trim().isEmpty()) {
	            if (accountManager.userHasGroupRights(currentUser.getUsername(), groupName.trim())) {
	                TextInputDialog studentDialog = new TextInputDialog();
	                studentDialog.setTitle("Add Student to Group");
	                studentDialog.setHeaderText("Enter Student Username:");
	                Optional<String> studentResult = studentDialog.showAndWait();

	                studentResult.ifPresent(username -> {
	                    if (!username.trim().isEmpty()) {
	                        boolean success = accountManager.addStudentToGroup(groupName.trim(), username.trim());
	                        if (success) {
	                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Student added to group successfully.");
	                            successAlert.showAndWait();
	                        } else {
	                            Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to add student to group. Please ensure the group and user exist.");
	                            failureAlert.showAndWait();
	                        }
	                    } else {
	                        Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Username cannot be empty.");
	                        emptyAlert.showAndWait();
	                    }
	                });
	            } else {
	                Alert noRightsAlert = new Alert(Alert.AlertType.ERROR, "You do not have rights to manage this group.");
	                noRightsAlert.showAndWait();
	            }
	        } else {
	            Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
	            emptyAlert.showAndWait();
	        }
	    });
	}


 // Views the list of students in a specified group by prompting for the group name.

 private void viewStudentsInGroup() {
	    TextInputDialog groupDialog = new TextInputDialog();
	    groupDialog.setTitle("View Students in Group");
	    groupDialog.setHeaderText("Enter Group Name:");
	    Optional<String> groupResult = groupDialog.showAndWait();

	    groupResult.ifPresent(groupName -> {
	        if (!groupName.trim().isEmpty()) {
	            if (accountManager.userHasGroupRights(currentUser.getUsername(), groupName.trim())) {
	                Set<String> students = accountManager.getStudentsInGroup(groupName.trim());
	                if (students.isEmpty()) {
	                    Alert noStudentsAlert = new Alert(Alert.AlertType.INFORMATION, "No students found in this group.");
	                    noStudentsAlert.showAndWait();
	                } else {
	                    StringBuilder studentList = new StringBuilder("Students in Group '" + groupName + "':\n");
	                    for (String student : students) {
	                        studentList.append("- ").append(student).append("\n");
	                    }
	                    Alert studentsAlert = new Alert(Alert.AlertType.INFORMATION, studentList.toString());
	                    studentsAlert.showAndWait();
	                }
	            } else {
	                Alert noRightsAlert = new Alert(Alert.AlertType.ERROR, "You do not have rights to view this group.");
	                noRightsAlert.showAndWait();
	            }
	        } else {
	            Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
	            emptyAlert.showAndWait();
	        }
	    });
	}

 // Removes a student from a specified group by prompting for the group name and student username.
 private void removeStudentFromGroup() {
	    TextInputDialog groupDialog = new TextInputDialog();
	    groupDialog.setTitle("Remove Student from Group");
	    groupDialog.setHeaderText("Enter Group Name:");
	    Optional<String> groupResult = groupDialog.showAndWait();

	    groupResult.ifPresent(groupName -> {
	        if (!groupName.trim().isEmpty()) {
	            if (accountManager.userHasGroupRights(currentUser.getUsername(), groupName.trim())) {
	                TextInputDialog studentDialog = new TextInputDialog();
	                studentDialog.setTitle("Remove Student from Group");
	                studentDialog.setHeaderText("Enter Student Username:");
	                Optional<String> studentResult = studentDialog.showAndWait();

	                studentResult.ifPresent(username -> {
	                    if (!username.trim().isEmpty()) {
	                        boolean success = accountManager.removeStudentFromGroup(groupName.trim(), username.trim());
	                        if (success) {
	                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Student removed from group successfully.");
	                            successAlert.showAndWait();
	                        } else {
	                            Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to remove student from group. Please ensure the group and user exist.");
	                            failureAlert.showAndWait();
	                        }
	                    } else {
	                        Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Username cannot be empty.");
	                        emptyAlert.showAndWait();
	                    }
	                });
	            } else {
	                Alert noRightsAlert = new Alert(Alert.AlertType.ERROR, "You do not have rights to manage this group.");
	                noRightsAlert.showAndWait();
	            }
	        } else {
	            Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
	            emptyAlert.showAndWait();
	        }
	    });
	}
    private void manageStudents() { // Initializes and displays the Student Management panel.

        VBox studentPanel = new VBox(10);
        studentPanel.setPadding(new Insets(20));
        Label studentLabel = new Label("Student Management");
        Button addStudentButton = new Button("Add Student to Group");
        Button viewStudentButton = new Button("View Students in Group");
        Button deleteStudentButton = new Button("Remove Student from Group");
        Button returnButton = new Button("Return to Instructor Menu");

        addStudentButton.setOnAction(e -> addStudentToGroup());
        viewStudentButton.setOnAction(e -> viewStudentsInGroup());
        deleteStudentButton.setOnAction(e -> removeStudentFromGroup());
        returnButton.setOnAction(e -> instructorPanel());

        studentPanel.getChildren().addAll(studentLabel, addStudentButton, viewStudentButton, deleteStudentButton, returnButton);
        Scene studentScene = new Scene(studentPanel, 400, 400);
        primaryStage.setScene(studentScene);
    }
    private void backupRestoreArticles() { // Initializes and displays the Backup & Restore Articles panel.

        VBox backupRestorePanel = new VBox(10);
        backupRestorePanel.setPadding(new Insets(20));
        Label backupRestoreLabel = new Label("Backup & Restore Articles");
        Button backupButton = new Button("Backup Articles");
        Button restoreButton = new Button("Restore Articles");
        Button returnButton = new Button("Return to Instructor Menu");

        backupButton.setOnAction(e -> backupArticles());
        restoreButton.setOnAction(e -> restoreArticles());
        returnButton.setOnAction(e -> instructorPanel());

        backupRestorePanel.getChildren().addAll(backupRestoreLabel, backupButton, restoreButton, returnButton);
        Scene backupRestoreScene = new Scene(backupRestorePanel, 400, 300);
        primaryStage.setScene(backupRestoreScene);
    }

    private void createGroup() { // Creates a new general group by prompting the user for the group name.

        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("Create Group");
        groupDialog.setHeaderText("Enter Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();

        groupResult.ifPresent(groupName -> {
            if (!groupName.trim().isEmpty()) {
                boolean success = accountManager.createGeneralGroup(groupName.trim(), currentUser.getUsername());
                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Group created successfully.");
                    successAlert.showAndWait();
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to create group. It may already exist.");
                    failureAlert.showAndWait();
                }
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
                emptyAlert.showAndWait();
            }
        });
    }


    private void viewGroup() { // Views the details of a specified group by prompting for the group name.

        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("View Group");
        groupDialog.setHeaderText("Enter Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();

        groupResult.ifPresent(groupName -> {
            if (!groupName.trim().isEmpty()) {
                String details = accountManager.getGroupDetails(groupName.trim());
                if (details != null) {
                    Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION, details);
                    detailsAlert.showAndWait();
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Group not found.");
                    failureAlert.showAndWait();
                }
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
                emptyAlert.showAndWait();
            }
        });
    }

    private void editGroup() { // Edits the name of an existing general group by prompting the user for the current and new group names.

        TextInputDialog oldGroupNameDialog = new TextInputDialog();
        oldGroupNameDialog.setTitle("Edit Group");
        oldGroupNameDialog.setHeaderText("Enter Existing Group Name:");
        Optional<String> oldGroupResult = oldGroupNameDialog.showAndWait();

        oldGroupResult.ifPresent(oldGroupName -> {
            if (!oldGroupName.trim().isEmpty()) {
                TextInputDialog newGroupNameDialog = new TextInputDialog();
                newGroupNameDialog.setTitle("Edit Group");
                newGroupNameDialog.setHeaderText("Enter New Group Name:");
                Optional<String> newGroupResult = newGroupNameDialog.showAndWait();

                newGroupResult.ifPresent(newGroupName -> {
                    if (!newGroupName.trim().isEmpty()) {
                        boolean success = accountManager.renameSpecialAccessGroup(oldGroupName.trim(), newGroupName.trim());
                        if (success) {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Group name updated successfully.");
                            successAlert.showAndWait();
                        } else {
                            Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to update group name. Please try again.");
                            failureAlert.showAndWait();
                        }
                    } else {
                        Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "New group name cannot be empty.");
                        emptyAlert.showAndWait();
                    }
                });
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Existing group name cannot be empty.");
                emptyAlert.showAndWait();
            }
        });
    }

    private void deleteGroup() { // Deletes an existing general group by prompting the user for the group name.

        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("Delete Group");
        groupDialog.setHeaderText("Enter Group Name to Delete:");
        Optional<String> groupResult = groupDialog.showAndWait();

        groupResult.ifPresent(groupName -> {
            if (!groupName.trim().isEmpty()) {
                boolean success = accountManager.deleteSpecialAccessGroup(groupName.trim());
                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Group deleted successfully.");
                    successAlert.showAndWait();
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to delete group. Please ensure the group exists.");
                    failureAlert.showAndWait();
                }
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
                emptyAlert.showAndWait();
            }
        });
    }
    private void viewSpecialGroup() { // Views the details of a specified special access group by prompting for the group name.

        TextInputDialog groupDialog = new TextInputDialog();
        groupDialog.setTitle("View Special Access Group");
        groupDialog.setHeaderText("Enter Special Group Name:");
        Optional<String> groupResult = groupDialog.showAndWait();

        groupResult.ifPresent(groupName -> {
            if (!groupName.trim().isEmpty()) {
                String details = accountManager.getGroupDetails(groupName.trim());
                if (details != null) {
                    Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION, details);
                    detailsAlert.getDialogPane().setPrefWidth(500); 
                    detailsAlert.showAndWait();
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Special access group not found.");
                    failureAlert.showAndWait();
                }
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Group name cannot be empty.");
                emptyAlert.showAndWait();
            }
        });
    }

    private void logout() { // Logs out the current user by interacting with the SessionManager.

        if (currentUser != null) {
            sessionManager.logoutUser(currentUser.getUsername());
            currentUser = null;
            Alert logoutAlert = new Alert(Alert.AlertType.INFORMATION, "Logged out successfully.");
            logoutAlert.showAndWait();
            showMainMenu();
        }
    }
}
