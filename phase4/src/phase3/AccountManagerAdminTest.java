package phase3;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountManagerAdminTest {

    private AccountManager accountManager;
    private DatabaseHelper dbHelper;

    @BeforeEach
    public void setUp() {
        dbHelper = DatabaseHelper.getInstance();
        dbHelper.resetDatabase();
        accountManager = new AccountManager();
    }

    @Test
    public void testDeleteSpecialGroup() {
        String testAdminUsername = "adminUser" + System.currentTimeMillis();
        accountManager.createUser(testAdminUsername, "password123");
        accountManager.addRoleToUser(testAdminUsername, Role.ADMIN);

        String groupName = "SpecialGroupToDelete" + System.currentTimeMillis();
        boolean created = accountManager.createSpecialAccessGroup(groupName, testAdminUsername);
        assertTrue(created);

        String groupDetails = accountManager.getGroupDetails(groupName);
        assertNotNull(groupDetails);

        boolean deleted = accountManager.deleteSpecialAccessGroup(groupName);
        assertTrue(deleted);

        groupDetails = accountManager.getGroupDetails(groupName);
        assertEquals("Group '" + groupName + "' does not exist.", groupDetails);
    }


    @Test
    public void testEditSpecialGroup() {
        String testAdminUsername = "adminUser" + System.currentTimeMillis();
        accountManager.createUser(testAdminUsername, "password123");
        accountManager.addRoleToUser(testAdminUsername, Role.ADMIN);

        String oldGroupName = "OldSpecialGroupName" + System.currentTimeMillis();
        String newGroupName = "NewSpecialGroupName" + System.currentTimeMillis();
        boolean created = accountManager.createSpecialAccessGroup(oldGroupName, testAdminUsername);
        assertTrue(created);

        boolean renamed = accountManager.renameSpecialAccessGroup(oldGroupName, newGroupName);
        assertTrue(renamed);

        String oldGroupDetails = accountManager.getGroupDetails(oldGroupName);
        assertEquals("Group '" + oldGroupName + "' does not exist.", oldGroupDetails);

        String newGroupDetails = accountManager.getGroupDetails(newGroupName);
        assertNotNull(newGroupDetails);
        assertTrue(newGroupDetails.contains(testAdminUsername));
    }

}