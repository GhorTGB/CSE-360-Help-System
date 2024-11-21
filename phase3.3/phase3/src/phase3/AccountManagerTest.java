package phase3;

public class AccountManagerTest {
    static int numPassed = 0;
    static int numFailed = 0;
    static DatabaseHelper dbHelper = DatabaseHelper.getInstance();

    public static void main(String[] args) {
        System.out.println("\nAutomated Testing for AccountManager");
        dbHelper.resetDatabase();
        AccountManager accountManager = new AccountManager();
        performTestCase(1, accountManager);
        performTestCase(2, accountManager);
        performTestCase(3, accountManager);
        performTestCase(4, accountManager);
        performTestCase(5, accountManager);
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void performTestCase(int testCase, AccountManager accountManager) {
        System.out.println("\nTest case: " + testCase);
        switch (testCase) {
            case 1:
                testCreateUser(accountManager);
                break;
            case 2:
                testAuthenticateUser(accountManager);
                break;
            case 3:
                testAddRoleToUser(accountManager);
                break;
            case 4:
                testCreateSpecialAccessGroup(accountManager);
                break;
            case 5:
                testAddUserToGroup(accountManager);
                break;
            default:
                System.out.println("Invalid test case number.");
                numFailed++;
        }
    }

    private static void testCreateUser(AccountManager accountManager) {
        System.out.println("Test: Create User");
        String username = "user" + System.currentTimeMillis();
        User user = accountManager.createUser(username, "password");
        if (user != null && user.getUsername().equals(username)) {
            System.out.println("Success** User created successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to create user.");
            numFailed++;
        }
    }

    private static void testAuthenticateUser(AccountManager accountManager) {
        System.out.println("Test: Authenticate User");
        String username = "authuser" + System.currentTimeMillis();
        accountManager.createUser(username, "password");
        User user = accountManager.authenticateUser(username, "password");
        if (user != null && user.getUsername().equals(username)) {
            System.out.println("Success** User authenticated successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to authenticate user.");
            numFailed++;
        }
    }

    private static void testAddRoleToUser(AccountManager accountManager) {
        System.out.println("Test: Add Role to User");
        String username = "roleuser" + System.currentTimeMillis();
        accountManager.createUser(username, "password");
        accountManager.addRoleToUser(username, Role.INSTRUCTOR);
        User user = accountManager.getUser(username);
        if (user.getRoles().contains(Role.INSTRUCTOR)) {
            System.out.println("Success** Role added to user successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to add role to user.");
            numFailed++;
        }
    }

    private static void testCreateSpecialAccessGroup(AccountManager accountManager) {
        System.out.println("Test: Create Special Access Group");
        String groupName = "Group" + System.currentTimeMillis();
        String adminUsername = "admin" + System.currentTimeMillis();
        accountManager.createUser(adminUsername, "password");
        boolean success = accountManager.createSpecialAccessGroup(groupName, adminUsername);
        if (success) {
            System.out.println("Success** Special Access Group created successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to create Special Access Group.");
            numFailed++;
        }
    }

    private static void testAddUserToGroup(AccountManager accountManager) {
        System.out.println("Test: Add User to Group");
        String groupName = "Group" + System.currentTimeMillis();
        String adminUsername = "admin" + System.currentTimeMillis();
        String userUsername = "user" + System.currentTimeMillis();
        accountManager.createUser(adminUsername, "password");
        accountManager.createUser(userUsername, "password");
        accountManager.createSpecialAccessGroup(groupName, adminUsername);
        boolean success = accountManager.addUserToGroup(groupName, userUsername, "View Access");
        if (success) {
            System.out.println("Success** User added to group successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to add user to group.");
            numFailed++;
        }
    }
}