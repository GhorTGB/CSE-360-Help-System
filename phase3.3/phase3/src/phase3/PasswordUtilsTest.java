package phase3;

public class PasswordUtilsTest {
    static int numPassed = 0;
    static int numFailed = 0;

    public static void main(String[] args) {
        System.out.println("____________________________________________________________________________");
        System.out.println("\nAutomated Testing for PasswordUtils");
        performTestCase(1, true);
        performTestCase(2, true);
        performTestCase(3, true);
        System.out.println("____________________________________________________________________________");
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void performTestCase(int testCase, boolean expectedPass) {
        System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
        switch (testCase) {
            case 1:
                testHashAndVerifyPassword(expectedPass);
                break;
            case 2:
                testHashUniqueness(expectedPass);
                break;
            case 3:
                testIncorrectPasswordVerification(expectedPass);
                break;
            default:
                System.out.println("Invalid test case number.");
                numFailed++;
        }
    }

    private static void testHashAndVerifyPassword(boolean expectedPass) {
        System.out.println("Test: Hash and Verify Password");
        String password = "StrongPassword123!";
        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            boolean isValid = PasswordUtils.verifyPassword(password, hashedPassword);
            if (isValid) {
                System.out.println("***Success*** Password hashed and verified successfully.");
                numPassed++;
            } else {
                System.out.println("***Failure*** Password verification failed.");
                numFailed++;
            }
        } catch (Exception e) {
            System.out.println("***Failure*** Exception occurred: " + e.getMessage());
            numFailed++;
        }
    }

    private static void testHashUniqueness(boolean expectedPass) {
        System.out.println("Test: Hash Uniqueness");
        String password = "UniquePassword123!";
        try {
            String hash1 = PasswordUtils.hashPassword(password);
            String hash2 = PasswordUtils.hashPassword(password);
            if (!hash1.equals(hash2)) {
                System.out.println("***Success*** Different hashes generated for the same password.");
                numPassed++;
            } else {
                System.out.println("***Failure*** Hashes are identical, salting might not be implemented correctly.");
                numFailed++;
            }
        } catch (Exception e) {
            System.out.println("***Failure*** Exception occurred: " + e.getMessage());
            numFailed++;
        }
    }

    private static void testIncorrectPasswordVerification(boolean expectedPass) {
        System.out.println("Test: Incorrect Password Verification");
        String password = "CorrectPassword123!";
        String wrongPassword = "WrongPassword123!";
        try {
            String hashedPassword = PasswordUtils.hashPassword(password);
            boolean isValid = PasswordUtils.verifyPassword(wrongPassword, hashedPassword);
            if (!isValid) {
                System.out.println("***Success*** Incorrect password verification failed as expected.");
                numPassed++;
            } else {
                System.out.println("***Failure*** Incorrect password was verified successfully, which is incorrect.");
                numFailed++;
            }
        } catch (Exception e) {
            System.out.println("***Failure*** Exception occurred: " + e.getMessage());
            numFailed++;
        }
    }
}
