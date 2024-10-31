package phase2;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.*;

public class UserInterface extends Application {
    private AccountManager accountManager;
    private SessionManager sessionManager;
    private ArticleManager articleManager;
    
    private Stage primaryStage;
    private User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    public UserInterface() {                                   // Initialize managers for user accounts, sessions, and articles
        this.accountManager = new AccountManager();
        this.sessionManager = new SessionManager();
        this.articleManager = new ArticleManager();
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("CSE360 Help System");

        if (accountManager.listUsers().isEmpty()) {        // If there are no users, show setup for the first admin account; otherwise, show main menu

            showAdminSetup();
        } else {
            showMainMenu();
        }
    }

    private void showAdminSetup() {        //  creating the first admin account

        GridPane setupGrid = new GridPane();
        setupGrid.setPadding(new Insets(20));
        setupGrid.setHgap(10);
        setupGrid.setVgap(10);

        Label setupLabel = new Label("Create the First Admin Account"); // UI components
        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button createAdminButton = new Button("Create Admin Account");

        setupGrid.add(setupLabel, 0, 0, 2, 1); // Add components
        setupGrid.add(userLabel, 0, 1);
        setupGrid.add(userField, 1, 1);
        setupGrid.add(passLabel, 0, 2);
        setupGrid.add(passField, 1, 2);
        setupGrid.add(createAdminButton, 1, 3);

        createAdminButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                User adminUser = accountManager.createUser(username, password);// Create an admin user and add admin role
                if (adminUser != null) {
                    accountManager.addRole(username, Role.ADMIN);
                    adminUser.addRole(Role.ADMIN);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Admin account is created.");
                    successAlert.showAndWait();
                    showMainMenu();
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Error in creating admin account. Please try again.");
                    failureAlert.showAndWait();
                }
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Username and password cannot be empty.");
                emptyAlert.showAndWait();
            }
        });

        Scene setupScene = new Scene(setupGrid, 500, 300);
        primaryStage.setScene(setupScene);
        primaryStage.show();
    }

    private void showMainMenu() {   // Main menu
        VBox mainMenu = new VBox(10);
        mainMenu.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome to the CSE360 Help System");
        Button loginButton = new Button("Login");
        Button loginWithInvitationButton = new Button("Login as Student or Instructor");
        Button exitButton = new Button("Exit");

        loginButton.setOnAction(e -> showLoginScreen());
        loginWithInvitationButton.setOnAction(e -> InvitationScreen());
        exitButton.setOnAction(e -> {
            DatabaseHelper.getInstance().closeConnection();
            primaryStage.close();
        });

        mainMenu.getChildren().addAll(welcomeLabel, loginButton, loginWithInvitationButton, exitButton);

        Scene scene = new Scene(mainMenu, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showLoginScreen() { //loginscreen section
        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(20));
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");
        Button backButton = new Button("Back");

        loginGrid.add(userLabel, 0, 0);
        loginGrid.add(userField, 1, 0);
        loginGrid.add(passLabel, 0, 1);
        loginGrid.add(passField, 1, 1);
        loginGrid.add(loginButton, 1, 2);
        loginGrid.add(backButton, 0, 2);

        loginButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            User user = accountManager.getUser(username);
            if (user != null && user.getPassword().equals(password)) {
                sessionManager.loginUser(username, user);
                currentUser = user;
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Login successful.");
                successAlert.showAndWait();
                UserBasedOnRole(user);
            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Invalid credentials or user does not exist.");
                failureAlert.showAndWait();
            }
        });

        backButton.setOnAction(e -> showMainMenu());

        Scene loginScene = new Scene(loginGrid, 400, 200);
        primaryStage.setScene(loginScene);
    }

    private void InvitationScreen() { //invitation screen for admin to invite new users
        GridPane invitationGrid = new GridPane();
        invitationGrid.setPadding(new Insets(20));
        invitationGrid.setHgap(10);
        invitationGrid.setVgap(10);

        Label invitationLabel = new Label("Enter Invitation Code:");
        TextField invitationField = new TextField();
        Button proceedButton = new Button("Proceed");
        Button backButton = new Button("Back");

        invitationGrid.add(invitationLabel, 0, 0);
        invitationGrid.add(invitationField, 1, 0);
        invitationGrid.add(proceedButton, 1, 1);
        invitationGrid.add(backButton, 0, 1);

        proceedButton.setOnAction(e -> {
            String invitationCode = invitationField.getText().trim();
            Role role = accountManager.RoleFromInvitationCode(invitationCode);
            if (role == null) {
                Alert invalidCodeAlert = new Alert(Alert.AlertType.ERROR, "Invalid invitation code.");
                invalidCodeAlert.showAndWait();
                return;
            }

            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("New User", "Existing User");
            choiceDialog.setTitle("User Type");
            choiceDialog.setHeaderText("Are you a new user or an existing user?");
            Optional<String> result = choiceDialog.showAndWait();

            result.ifPresent(userType -> {
                if (userType.equals("New User")) {
                    createNewUser(role, invitationCode);
                } else if (userType.equals("Existing User")) {
                    existingUserLogin(role, invitationCode);
                }
            });
        });

        backButton.setOnAction(e -> showMainMenu());

        Scene invitationScene = new Scene(invitationGrid, 400, 200);
        primaryStage.setScene(invitationScene);
    }

    private void createNewUser(Role role, String invitationCode) { //new user creation
        GridPane newUserGrid = new GridPane();
        newUserGrid.setPadding(new Insets(20));
        newUserGrid.setHgap(10);
        newUserGrid.setVgap(10);

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button createButton = new Button("Account");

        newUserGrid.add(userLabel, 0, 0);
        newUserGrid.add(userField, 1, 0);
        newUserGrid.add(passLabel, 0, 1);
        newUserGrid.add(passField, 1, 1);
        newUserGrid.add(createButton, 1, 2);

        createButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                User newUser = accountManager.createUser(username, password);
                if (newUser != null) {
                    accountManager.addRole(username, role);
                    newUser.addRole(role);
                    sessionManager.loginUser(username, newUser);
                    currentUser = newUser;
                    accountManager.deleteInvitationCode(invitationCode);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Account created and logged in successfully.");
                    successAlert.showAndWait();
                    UserBasedOnRole(newUser);
                } else {
                    Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Failed to create account.");
                    failureAlert.showAndWait();
                }
            } else {
                Alert emptyAlert = new Alert(Alert.AlertType.WARNING, "Username and password cannot be empty.");
                emptyAlert.showAndWait();
            }
        });

        Scene newUserScene = new Scene(newUserGrid, 400, 200);
        primaryStage.setScene(newUserScene);
    }

    private void existingUserLogin(Role role, String invitationCode) { //existing user login
        GridPane existingUserGrid = new GridPane();
        existingUserGrid.setPadding(new Insets(20));
        existingUserGrid.setHgap(10);
        existingUserGrid.setVgap(10);

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        Button loginButton = new Button("Login");

        existingUserGrid.add(userLabel, 0, 0);
        existingUserGrid.add(userField, 1, 0);
        existingUserGrid.add(passLabel, 0, 1);
        existingUserGrid.add(passField, 1, 1);
        existingUserGrid.add(loginButton, 1, 2);

        loginButton.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText().trim();

            User user = accountManager.getUser(username);
            if (user != null && user.getPassword().equals(password)) {
                accountManager.addRole(username, role);
                user.addRole(role);
                sessionManager.loginUser(username, user);
                currentUser = user;
                accountManager.deleteInvitationCode(invitationCode);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Role added and logged in successfully."); //roll adding
                successAlert.showAndWait();
                UserBasedOnRole(user);
            } else {
                Alert failureAlert = new Alert(Alert.AlertType.ERROR, "Invalid credentials or user does not exist.");
                failureAlert.showAndWait();
            }
        });

        Scene existingUserScene = new Scene(existingUserGrid, 400, 200);
        primaryStage.setScene(existingUserScene);
    }

    private void UserBasedOnRole(User user) {
        if (user.getRoles().contains(Role.ADMIN)) {
            AdminPanel();
        } else if (user.getRoles().contains(Role.INSTRUCTOR)) {
            InstructorPanel();
        } else if (user.getRoles().contains(Role.STUDENT)) {
            showStudentPanel();
        } else {
            Alert noRoleAlert = new Alert(Alert.AlertType.ERROR, "No specific roles assigned. Contact administrator.");
            noRoleAlert.showAndWait();
            logout();
        }
    }

    private void AdminPanel() { //admin panel to view all the users
        VBox adminPanel = new VBox(10);
        adminPanel.setPadding(new Insets(20));

        Label adminLabel = new Label("Admin Panel");
        Button listUsersButton = new Button("List Users");
        Button addRoleButton = new Button("Add Role to User");
        Button removeRoleButton = new Button("Remove Role from User");
        Button generateCodeButton = new Button("Generate Invitation Code");
        Button manageArticlesButton = new Button("Manage Articles");
        Button backupButton = new Button("Backup Articles");
        Button restoreButton = new Button("Restore Articles");
        Button logoutButton = new Button("Log Out");

        listUsersButton.setOnAction(e -> listUsers());
        addRoleButton.setOnAction(e -> addRoleToUser());
        removeRoleButton.setOnAction(e -> removeRoleFromUser());
        generateCodeButton.setOnAction(e -> InvitationCode());
        manageArticlesButton.setOnAction(e -> manageArticles());
        backupButton.setOnAction(e -> backupArticles());
        restoreButton.setOnAction(e -> restoreArticles());
        logoutButton.setOnAction(e -> logout());

        adminPanel.getChildren().addAll(adminLabel, listUsersButton, addRoleButton, removeRoleButton,
                generateCodeButton, manageArticlesButton, backupButton, restoreButton, logoutButton);

        Scene adminScene = new Scene(adminPanel, 400, 400);
        primaryStage.setScene(adminScene);
    }

    private void InstructorPanel() { //instructor panel for managing articles and backup and restore of articles
        VBox instructorPanel = new VBox(10);
        instructorPanel.setPadding(new Insets(20));

        Label instructorLabel = new Label("Instructor Panel");
        Button manageArticlesButton = new Button("Manage Articles");
        Button backupButton = new Button("Backup Articles");
        Button restoreButton = new Button("Restore Articles");
        Button logoutButton = new Button("Log Out");

        manageArticlesButton.setOnAction(e -> manageArticles());
        backupButton.setOnAction(e -> backupArticles());
        restoreButton.setOnAction(e -> restoreArticles());
        logoutButton.setOnAction(e -> logout());

        instructorPanel.getChildren().addAll(instructorLabel, manageArticlesButton, backupButton, restoreButton, logoutButton);

        Scene instructorScene = new Scene(instructorPanel, 400, 300);
        primaryStage.setScene(instructorScene);
    }

    private void showStudentPanel() { //student panel 
        VBox studentPanel = new VBox(10);
        studentPanel.setPadding(new Insets(20));

        Label studentLabel = new Label("Student Panel");
        Button searchArticlesButton = new Button("Search Articles"); //article search
        Button viewArticleButton = new Button("View Article"); //viewing articles
        Button viewByLevelButton = new Button("View Articles by Difficulty Level"); //difficulty level
        Button logoutButton = new Button("Log Out");

        searchArticlesButton.setOnAction(e -> searchArticles());
        viewArticleButton.setOnAction(e -> viewArticle());
        viewByLevelButton.setOnAction(e -> viewArticlesByLevel());
        logoutButton.setOnAction(e -> logout());

        studentPanel.getChildren().addAll(studentLabel, searchArticlesButton, viewArticleButton, viewByLevelButton, logoutButton);

        Scene studentScene = new Scene(studentPanel, 400, 300);
        primaryStage.setScene(studentScene);
    }

    private void listUsers() {
        Map<String, User> users = accountManager.listUsers();
        StringBuilder userList = new StringBuilder("Users:\n");
        for (Map.Entry<String, User> entry : users.entrySet()) {
            userList.append("Username: ").append(entry.getKey())
                    .append(", Roles: ").append(entry.getValue().getRoles())
                    .append("\n");
        }
        Alert userAlert = new Alert(Alert.AlertType.INFORMATION, userList.toString());
        userAlert.showAndWait();
    }

    private void addRoleToUser() { //add role function
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
                        accountManager.addRole(username, role);
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

    private void removeRoleFromUser() { //remove role function
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Remove Role from User");
        usernameDialog.setHeaderText("Enter username:");
        Optional<String> usernameResult = usernameDialog.showAndWait();

        usernameResult.ifPresent(username -> {
            ChoiceDialog<String> roleDialog = new ChoiceDialog<>("INSTRUCTOR", "STUDENT");
            roleDialog.setTitle("Select Role");
            roleDialog.setHeaderText("Select role to remove:");

            Optional<String> roleResult = roleDialog.showAndWait();
            roleResult.ifPresent(roleStr -> {
                try {
                    Role role = Role.valueOf(roleStr);
                    User user = accountManager.getUser(username);
                    if (user != null) {
                        accountManager.removeRole(username, role);
                        user.removeRole(role);
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Role removed successfully.");
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

    private void InvitationCode() { //invitation code
        ChoiceDialog<String> roleDialog = new ChoiceDialog<>("INSTRUCTOR", "STUDENT");
        roleDialog.setTitle("Generate Invitation Code");
        roleDialog.setHeaderText("Select role for the invitation code:");
        Optional<String> roleResult = roleDialog.showAndWait();

        roleResult.ifPresent(roleStr -> {
            try {
                Role role = Role.valueOf(roleStr);
                String code = "INV-" + System.currentTimeMillis();
                accountManager.InvitationCode(code, role);
                Alert codeAlert = new Alert(Alert.AlertType.INFORMATION, "Generated Invitation Code: " + code);
                codeAlert.showAndWait();
            } catch (IllegalArgumentException e) {
                Alert invalidRoleAlert = new Alert(Alert.AlertType.ERROR, "Invalid role.");
                invalidRoleAlert.showAndWait();
            }
        });
    }

    private void manageArticles() { //article section 
        String[] options = {
                "Create Article",
                "Update Article",
                "View Article",
                "Delete Article",
                "Add Article to Group",
                "View Articles by Group",
                "List All Articles",
                "Search Articles",
                "View Articles by Difficulty Level",
                "Backup Articles",
                "Restore Articles",
                "Return to Previous Menu"
        };

        VBox articlePanel = new VBox(10);
        articlePanel.setPadding(new Insets(20));

        for (String option : options) {
            Button button = new Button(option);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> handleArticleOption(option));
            articlePanel.getChildren().add(button);
        }

        Scene articleScene = new Scene(articlePanel, 400, 500);
        primaryStage.setScene(articleScene);
    }

    private void handleArticleOption(String option) {
        switch (option) {
            case "Create Article":
                createArticle();
                break;
            case "Update Article":
                updateArticle();
                break;
            case "View Article":
                viewArticle();
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
            case "List All Articles":
                listAllArticles();
                break;
            case "Search Articles":
                searchArticles();
                break;
            case "View Articles by Difficulty Level":
                viewArticlesByLevel();
                break;
            case "Backup Articles":
                backupArticles();
                break;
            case "Restore Articles":
                restoreArticles();
                break;
            case "Return to Previous Menu":
                if (currentUser.getRoles().contains(Role.ADMIN)) {
                    AdminPanel();
                } else if (currentUser.getRoles().contains(Role.INSTRUCTOR)) {
                    InstructorPanel();
                }
                break;
            default:
                break;
        }
    }

    private void createArticle() { //create article 
        GridPane articleGrid = new GridPane();
        articleGrid.setPadding(new Insets(20));
        articleGrid.setHgap(10);
        articleGrid.setVgap(10);

        Label levelLabel = new Label("Level:");
        TextField levelField = new TextField();
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
        Button createButton = new Button("Create Article");

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
        articleGrid.add(createButton, 1, 7);

        createButton.setOnAction(e -> {
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
            long articleId = articleManager.createArticle(article);
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Article created with ID: " + articleId);
            successAlert.showAndWait();
            manageArticles();
        });

        Scene createArticleScene = new Scene(articleGrid, 600, 500);
        primaryStage.setScene(createArticleScene);
    }

    private void updateArticle() { //update article
        GridPane updateGrid = new GridPane();
        updateGrid.setPadding(new Insets(20));
        updateGrid.setHgap(10);
        updateGrid.setVgap(10);
        Label idLabel = new Label("Article ID:");
        TextField idField = new TextField();
        Label titleLabel = new Label("New Title:");
        TextField titleField = new TextField();
        Label levelLabel = new Label("New Level (beginner, intermediate, advanced, expert):");
        TextField levelField = new TextField();
        Label descriptionLabel = new Label("New Description:");
        TextArea descriptionArea = new TextArea();
        Label keywordsLabel = new Label("New Keywords (comma-separated):");
        TextField keywordsField = new TextField();
        Button updateButton = new Button("Update Article");

        updateGrid.add(idLabel, 0, 0);
        updateGrid.add(idField, 1, 0);
        updateGrid.add(titleLabel, 0, 1);
        updateGrid.add(titleField, 1, 1);
        updateGrid.add(levelLabel, 0, 2);
        updateGrid.add(levelField, 1, 2);
        updateGrid.add(descriptionLabel, 0, 3);
        updateGrid.add(descriptionArea, 1, 3);
        updateGrid.add(keywordsLabel, 0, 4);
        updateGrid.add(keywordsField, 1, 4);
        updateGrid.add(updateButton, 1, 5);

        updateButton.setOnAction(e -> {
            try {
                long articleId = Long.parseLong(idField.getText().trim());
                String newTitle = titleField.getText();
                String newLevel = levelField.getText();
                String newDescription = descriptionArea.getText();
                List<String> newKeywords = List.of(keywordsField.getText().split(","));

              
                Article existingArticle = articleManager.getArticle(articleId);
                if (existingArticle != null) {
               
                    existingArticle.setTitle(newTitle);
                    existingArticle.setLevel(newLevel);
                    existingArticle.setShortDescription(newDescription);
                    existingArticle.setKeywords(newKeywords);

                 
                    articleManager.updateArticle(articleId, existingArticle);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Article updated successfully.");
                    successAlert.showAndWait();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Article not found.");
                    errorAlert.showAndWait();
                }
            } catch (NumberFormatException ex) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Invalid article ID.");
                errorAlert.showAndWait();
            } catch (Exception ex) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "An error occurred while updating the article.");
                errorAlert.showAndWait();
            }
        });

        Scene updateScene = new Scene(updateGrid, 500, 400);
        primaryStage.setScene(updateScene);
    }


    private void viewArticle() { //view article
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("View Article");
        idDialog.setHeaderText("Enter Article ID:");
        Optional<String> idResult = idDialog.showAndWait();

        idResult.ifPresent(idStr -> {
            try {
                long id = Long.parseLong(idStr.trim());
                Article article = articleManager.getArticle(id);
                if (article != null) {
                    StringBuilder articleDetails = new StringBuilder();
                    articleDetails.append("ID: ").append(article.getId()).append("\n");
                    articleDetails.append("Level: ").append(article.getLevel()).append("\n");
                    articleDetails.append("Title: ").append(article.getTitle()).append("\n");
                    articleDetails.append("Short Description: ").append(article.getShortDescription()).append("\n");
                    articleDetails.append("Keywords: ").append(article.getKeywords()).append("\n");
                    articleDetails.append("Body: ").append(article.getBody()).append("\n");
                    articleDetails.append("Links: ").append(article.getLinks()).append("\n");
                    articleDetails.append("Groups: ").append(article.getGroups()).append("\n");
                    articleDetails.append("Sensitive: ").append(article.isSensitive()).append("\n");

                    Alert articleAlert = new Alert(Alert.AlertType.INFORMATION, articleDetails.toString());
                    articleAlert.getDialogPane().setPrefWidth(500);
                    articleAlert.showAndWait();
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

    private void deleteArticle() { //delete article
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

    private void addArticleToGroup() { //adding article to groups
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

    private void viewArticlesByGroup() { //viewing articles
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
                for (Article article : articles) {
                    articleList.append("ID: ").append(article.getId()).append(", Title: ").append(article.getTitle()).append("\n");
                }
                Alert articlesAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
                articlesAlert.showAndWait();
            }
        });
    }

    private void listAllArticles() { //list of articles
        Collection<Article> articles = articleManager.listAllArticles();
        if (articles.isEmpty()) {
            Alert noArticlesAlert = new Alert(Alert.AlertType.INFORMATION, "No articles available.");
            noArticlesAlert.showAndWait();
        } else {
            StringBuilder articleList = new StringBuilder("All Articles:\n");
            for (Article article : articles) {
                articleList.append("ID: ").append(article.getId())
                        .append(", Title: ").append(article.getTitle())
                        .append(", Level: ").append(article.getLevel())
                        .append("\n");
            }
            Alert articlesAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
            articlesAlert.showAndWait();
        }
    }

    private void searchArticles() { //searching of articles
        TextInputDialog keywordDialog = new TextInputDialog();
        keywordDialog.setTitle("Search Articles");
        keywordDialog.setHeaderText("Enter keyword to search:"); //keyword shall be entered to search about the article
        Optional<String> keywordResult = keywordDialog.showAndWait();

        keywordResult.ifPresent(keyword -> {
            List<Article> results = articleManager.searchArticles(keyword.trim());
            if (results.isEmpty()) {
                Alert noResultsAlert = new Alert(Alert.AlertType.INFORMATION, "No articles found with keyword: " + keyword);
                noResultsAlert.showAndWait();
            } else {
                StringBuilder articleList = new StringBuilder("Articles found:\n");
                for (Article article : results) {
                    articleList.append("ID: ").append(article.getId()).append(", Title: ").append(article.getTitle()).append("\n");
                }
                Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
                resultsAlert.showAndWait();
            }
        });
    }

    private void viewArticlesByLevel() { //viewing articles by the level of difficulty
        ChoiceDialog<String> levelDialog = new ChoiceDialog<>("beginner", "intermediate", "advanced", "expert");
        levelDialog.setTitle("View Articles by Difficulty Level");
        levelDialog.setHeaderText("Select difficulty level:");
        Optional<String> levelResult = levelDialog.showAndWait();

        levelResult.ifPresent(level -> {
            List<Article> results = articleManager.getArticlesByLevel(level.trim());
            if (results.isEmpty()) {
                Alert noResultsAlert = new Alert(Alert.AlertType.INFORMATION, "No articles found at the '" + level + "' level.");
                noResultsAlert.showAndWait();
            } else {
                StringBuilder articleList = new StringBuilder("Articles at '" + level + "' level:\n");
                for (Article article : results) {
                    articleList.append("ID: ").append(article.getId()).append(", Title: ").append(article.getTitle()).append("\n");
                }
                Alert resultsAlert = new Alert(Alert.AlertType.INFORMATION, articleList.toString());
                resultsAlert.showAndWait();
            }
        });
    }

    private void backupArticles() { //back up articles
        TextInputDialog filenameDialog = new TextInputDialog("backup.sql");
        filenameDialog.setTitle("Backup Articles");
        filenameDialog.setHeaderText("Enter filename to backup articles to:");
        Optional<String> filenameResult = filenameDialog.showAndWait();

        filenameResult.ifPresent(filename -> {
            articleManager.backupArticles(filename.trim(), true, null);
            Alert backupAlert = new Alert(Alert.AlertType.INFORMATION, "Articles backed up successfully to " + filename);
            backupAlert.showAndWait();
        });
    }

    private void restoreArticles() { //restoring of articles
        TextInputDialog filenameDialog = new TextInputDialog("backup.sql");
        filenameDialog.setTitle("Restore Articles");
        filenameDialog.setHeaderText("Enter filename to restore articles from:");
        Optional<String> filenameResult = filenameDialog.showAndWait();

        filenameResult.ifPresent(filename -> {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Remove existing articles?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();

            boolean removeExisting = confirmationResult.isPresent() && confirmationResult.get() == ButtonType.YES;
            articleManager.restoreArticles(filename.trim(), removeExisting, false);
            Alert restoreAlert = new Alert(Alert.AlertType.INFORMATION, "Articles restored successfully from " + filename);
            restoreAlert.showAndWait();
        });
    }

    private void logout() { //logout button
        if (currentUser != null) {
            sessionManager.logoutUser(currentUser.getUsername());
            currentUser = null;
            Alert logoutAlert = new Alert(Alert.AlertType.INFORMATION, "Logged out successfully.");
            logoutAlert.showAndWait();
            showMainMenu();
        }
    }
}