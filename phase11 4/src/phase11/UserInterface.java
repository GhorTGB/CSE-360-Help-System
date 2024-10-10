package phase11;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class UserInterface extends Application {

    private UserManager userManager = new UserManager();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Help System phase 1");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        GridPane.setConstraints(usernameLabel, 0, 0);
        TextField usernameInput = new TextField();
        GridPane.setConstraints(usernameInput, 1, 0);

        Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel, 0, 1);
        PasswordField passwordInput = new PasswordField();
        GridPane.setConstraints(passwordInput, 1, 1);

        Label otpLabel = new Label("OTP (optional):");
        GridPane.setConstraints(otpLabel, 0, 2);
        TextField otpInput = new TextField();
        GridPane.setConstraints(otpInput, 1, 2);

        Button loginButton = new Button("Login with Password");
        GridPane.setConstraints(loginButton, 1, 3);
        loginButton.setOnAction(e -> {
            String username = usernameInput.getText();
            String password = passwordInput.getText();
            handleLogin(username, password, primaryStage);
        });

        Button otpLoginButton = new Button("Login with OTP");
        GridPane.setConstraints(otpLoginButton, 1, 4);
        otpLoginButton.setOnAction(e -> {
            String username = usernameInput.getText();
            String enteredOtp = otpInput.getText();
            handleOtpLogin(username, enteredOtp, primaryStage);
        });

        Button requestOtpButton = new Button("Request OTP");
        GridPane.setConstraints(requestOtpButton, 1, 5);
        requestOtpButton.setOnAction(e -> {
            String username = usernameInput.getText();
            requestOtpForUser(username);
        });

        Button registerButton = new Button("Register New User");
        GridPane.setConstraints(registerButton, 1, 6);
        registerButton.setOnAction(e -> {
            showRegistrationPage(primaryStage);
        });

        grid.getChildren().addAll(usernameLabel, usernameInput, passwordLabel, passwordInput, otpLabel, otpInput, loginButton, otpLoginButton, requestOtpButton, registerButton);
        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(String username, String password, Stage primaryStage) {
        boolean loginSuccess = userManager.login(username, password);
        if (loginSuccess) {
            if (userManager.getLoggedInUser().getRoles().contains("Admin")) {
                showAdminScreen(primaryStage);
            } else {
                showNextScreen(primaryStage, userManager.getLoggedInUser().getRoles().get(0), username);
            }
        } else {
            showAlert("Login Failed", "Invalid username or password.");
        }
    }

    private void handleOtpLogin(String username, String enteredOtp, Stage primaryStage) {
        boolean loginSuccess = userManager.loginWithOtp(username, enteredOtp);
        if (loginSuccess) {
            if (userManager.getLoggedInUser().getRoles().contains("Admin")) {
                showAdminScreen(primaryStage);
            } else {
                showNextScreen(primaryStage, userManager.getLoggedInUser().getRoles().get(0), username);
            }
        } else {
            showAlert("Login Failed", "Invalid OTP.");
        }
    }

    private void requestOtpForUser(String username) {
        userManager.generateOtpForUser(username);
        showAlert("OTP Sent", "An OTP has been generated for " + username + ". Check the console.");
    }

    private void showRegistrationPage(Stage primaryStage) {
        Stage registerStage = new Stage();
        registerStage.setTitle("Register New User");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        GridPane.setConstraints(usernameLabel, 0, 0);
        TextField usernameInput = new TextField();
        GridPane.setConstraints(usernameInput, 1, 0);

        Label passwordLabel = new Label("Password:");
        GridPane.setConstraints(passwordLabel, 0, 1);
        PasswordField passwordInput = new PasswordField();
        GridPane.setConstraints(passwordInput, 1, 1);

        Label emailLabel = new Label("Email:");
        GridPane.setConstraints(emailLabel, 0, 2);
        TextField emailInput = new TextField();
        GridPane.setConstraints(emailInput, 1, 2);

        Label roleLabel = new Label("Roles (comma-separated):");
        GridPane.setConstraints(roleLabel, 0, 3);
        TextField roleInput = new TextField("Student,Instructor");
        GridPane.setConstraints(roleInput, 1, 3);

        Button registerButton = new Button("Register");
        GridPane.setConstraints(registerButton, 1, 4);
        registerButton.setOnAction(e -> {
            String username = usernameInput.getText();
            String password = passwordInput.getText();
            String email = emailInput.getText();
            String roleString = roleInput.getText();
            List<String> roles = Arrays.asList(roleString.split(","));
            handleRegistration(username, password, email, roles, registerStage);
        });

        grid.getChildren().addAll(usernameLabel, usernameInput, passwordLabel, passwordInput, emailLabel, emailInput, roleLabel, roleInput, registerButton);
        Scene scene = new Scene(grid, 400, 300);
        registerStage.setScene(scene);
        registerStage.show();
    }

    private void handleRegistration(String username, String password, String email, List<String> roles, Stage registerStage) {
        boolean success = userManager.createUser(username, password, email, roles);
        if (success) {
            showAlert("Registration Successful", "New user registered successfully.");
            registerStage.close();
        } else {
            showAlert("Registration Failed", "Username already taken.");
        }
    }

    private void showAdminScreen(Stage primaryStage) {
        Stage adminStage = new Stage();
        adminStage.setTitle("Admin Dashboard");

        Label welcomeLabel = new Label("Welcome Admin!");

        Button listUsersButton = new Button("List All Users");
        listUsersButton.setOnAction(e -> {
            userManager.listUsers();
        });

        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(e -> {
            String username = showInputDialog("Delete User", "Enter username to delete:");
            if (username != null && !username.isEmpty()) {
                userManager.deleteUser(username);
            }
        });

        Button resetPasswordButton = new Button("Reset User Password");
        resetPasswordButton.setOnAction(e -> {
            String username = showInputDialog("Reset Password", "Enter username to reset password:");
            if (username != null && !username.isEmpty()) {
                String newPassword = showInputDialog("New Password", "Enter new password:");
                if (newPassword != null && !newPassword.isEmpty()) {
                    userManager.resetUserAccount(username);
                }
            }
        });

        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(e -> {
            userManager.logout();
            adminStage.close();
            primaryStage.show();
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);
        GridPane.setConstraints(welcomeLabel, 0, 0);
        GridPane.setConstraints(listUsersButton, 0, 1);
        GridPane.setConstraints(deleteUserButton, 0, 2);
        GridPane.setConstraints(resetPasswordButton, 0, 3);
        GridPane.setConstraints(logoutButton, 0, 4);

        grid.getChildren().addAll(welcomeLabel, listUsersButton, deleteUserButton, resetPasswordButton, logoutButton);
        Scene adminScene = new Scene(grid, 400, 300);
        adminStage.setScene(adminScene);
        adminStage.show();
    }

    private void showNextScreen(Stage primaryStage, String role, String username) {
        Stage homeStage = new Stage();
        homeStage.setTitle(role + " Home Page");

        Label welcomeLabel = new Label("Welcome " + username + " (" + role + ")");
        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(e -> {
            userManager.logout();
            homeStage.close();
            primaryStage.show();
        });

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setVgap(8);
        layout.setHgap(10);
        GridPane.setConstraints(welcomeLabel, 0, 0);
        GridPane.setConstraints(logoutButton, 0, 1);

        layout.getChildren().addAll(welcomeLabel, logoutButton);
        Scene homeScene = new Scene(layout, 300, 200);
        homeStage.setScene(homeScene);
        homeStage.show();
    }

    private String showInputDialog(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait().orElse(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
