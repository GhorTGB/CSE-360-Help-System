package phase3;

import java.util.HashSet;
import java.util.Set;

public class Group {
    private String groupName;
    private String createdBy;
    private Set<String> members;

    public Group(String groupName, String createdBy) {
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.members = new HashSet<>();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Set<String> getMembers() {
        return new HashSet<>(members);
    }

    public boolean addMember(String username) {
        return members.add(username);
    }

    public boolean removeMember(String username) {
        return members.remove(username);
    }

    public boolean isMember(String username) {
        return members.contains(username);
    }
}