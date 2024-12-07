package phase3;

import java.util.ArrayList;

public class ArticleManagerTest {
    static int numPassed = 0;
    static int numFailed = 0;
    static DatabaseHelper dbHelper = DatabaseHelper.getInstance();

    public static void main(String[] args) {
        System.out.println("\nAutomated Testing for ArticleManager");
        dbHelper.resetDatabase();
        ArticleManager articleManager = new ArticleManager();
        performTestCase(1, articleManager);
        performTestCase(2, articleManager);
        performTestCase(3, articleManager);
        performTestCase(4, articleManager);
        performTestCase(5, articleManager);
        System.out.println();
        System.out.println("Number of tests passed: " + numPassed);
        System.out.println("Number of tests failed: " + numFailed);
    }

    private static void performTestCase(int testCase, ArticleManager articleManager) {
        System.out.println("\nTest case: " + testCase);
        switch (testCase) {
            case 1:
                testCreateAndRetrieveArticle(articleManager);
                break;
            case 2:
                testUpdateArticle(articleManager);
                break;
            case 3:
                testDeleteArticle(articleManager);
                break;
            case 4:
                testSensitiveArticleEncryption(articleManager);
                break;
            case 5:
                testArticleAccessByGroup(articleManager);
                break;
            default:
                System.out.println("Invalid test case number.");
                numFailed++;
        }
    }

    private static void testCreateAndRetrieveArticle(ArticleManager articleManager) {
        System.out.println("Test: Create and Retrieve Article");
        Article article = new Article(
                0,
                "beginner",
                "Test Article",
                "This is a test article.",
                new ArrayList<>(),
                "Test body content",
                new ArrayList<>(),
                false
        );
        long id = articleManager.createArticle(article);
        Article retrievedArticle = articleManager.getArticle(id);
        if (retrievedArticle != null && retrievedArticle.getTitle().equals("Test Article")) {
            System.out.println("Success** Article created and retrieved successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to create or retrieve the article.");
            numFailed++;
        }
    }

    private static void testUpdateArticle(ArticleManager articleManager) {
        System.out.println("Test: Update Article");
        Article article = new Article(
                0,
                "beginner",
                "Original Title",
                "Original description.",
                new ArrayList<>(),
                "Original body",
                new ArrayList<>(),
                false
        );
        long id = articleManager.createArticle(article);
        Article updatedArticle = new Article(
                id,
                "advanced",
                "Updated Title",
                "Updated description.",
                new ArrayList<>(),
                "Updated body",
                new ArrayList<>(),
                false
        );
        articleManager.updateArticle(id, updatedArticle);
        Article retrievedArticle = articleManager.getArticle(id);
        if (retrievedArticle != null && retrievedArticle.getTitle().equals("Updated Title")) {
            System.out.println("Success** Article updated successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Failed to update the article.");
            numFailed++;
        }
    }

    private static void testDeleteArticle(ArticleManager articleManager) {
        System.out.println("Test: Delete Article");
        Article article = new Article(
                0,
                "intermediate",
                "Delete Me",
                "To be deleted.",
                new ArrayList<>(),
                "Delete this article",
                new ArrayList<>(),
                false
        );
        long id = articleManager.createArticle(article);
        articleManager.deleteArticle(id);
        Article retrievedArticle = articleManager.getArticle(id);
        if (retrievedArticle == null) {
            System.out.println("Success** Article deleted successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Article was not deleted.");
            numFailed++;
        }
    }

    private static void testSensitiveArticleEncryption(ArticleManager articleManager) {
        System.out.println("Test: Sensitive Article Encryption");
        Article article = new Article(
                0,
                "advanced",
                "Sensitive Article",
                "Contains sensitive information.",
                new ArrayList<>(),
                "Secret content",
                new ArrayList<>(),
                true
        );
        long id = articleManager.createArticle(article);
        Article retrievedArticle = articleManager.getArticle(id);
        if (retrievedArticle != null && retrievedArticle.getBody().equals("Secret content")) {
            System.out.println("Success** Sensitive article encrypted and decrypted successfully.");
            numPassed++;
        } else {
            System.out.println("Failure** Encryption or decryption of sensitive article failed.");
            numFailed++;
        }
    }

    private static void testArticleAccessByGroup(ArticleManager articleManager) {
        System.out.println("Test: Article Access by Group");
        AccountManager accountManager = new AccountManager();
        String uniqueUser = "testuser" + System.currentTimeMillis();
        User testUser = accountManager.createUser(uniqueUser, "password");
        accountManager.addRoleToUser(uniqueUser, Role.STUDENT);
        String uniqueGroup = "TestGroup" + System.currentTimeMillis();
        accountManager.createSpecialAccessGroup(uniqueGroup, uniqueUser);
        accountManager.addUserToGroup(uniqueGroup, uniqueUser, "View Access");
        Article article = new Article(
                0,
                "expert",
                "Group Article",
                "Article in a group.",
                new ArrayList<>(),
                "Group restricted content",
                new ArrayList<>(),
                true
        );
        article.addGroup(uniqueGroup);
        long id = articleManager.createArticle(article);
        boolean hasAccess = articleManager.userHasAccessToArticle(uniqueUser, id);
        if (hasAccess) {
            System.out.println("Success** User has access to the article in the group.");
            numPassed++;
        } else {
            System.out.println("Failure** User does not have access to the article in the group.");
            numFailed++;
        }
    }
}