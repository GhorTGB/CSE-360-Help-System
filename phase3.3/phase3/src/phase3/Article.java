package phase3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String level;
    private String title;
    private String shortDescription;
    private List<String> keywords;
    private String body;
    private List<String> links;
    private Set<String> groups;
    private boolean sensitive;
    private String author;

    public Article(long id, String level, String title, String shortDescription, List<String> keywords, String body, List<String> links, boolean sensitive) {
        this.id = id;
        this.level = level;
        this.title = title;
        this.shortDescription = shortDescription;
        // Initialize keywords to an empty list if null is passed
        this.keywords = (keywords != null) ? keywords : new ArrayList<>();
        this.body = body;
        // Initialize links to an empty list if null is passed
        this.links = (links != null) ? links : new ArrayList<>();
        this.groups = new HashSet<>();
        this.sensitive = sensitive;
        this.author = "Unknown";
    }

    // Getters and setters
    public long getId() { return id; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) {
        this.keywords = (keywords != null) ? keywords : new ArrayList<>();
    }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public List<String> getLinks() { return links; }
    public void setLinks(List<String> links) {
        this.links = (links != null) ? links : new ArrayList<>();
    }
    public Set<String> getGroups() { return groups; }
    public void setGroups(Set<String> groups) { this.groups = groups; }
    public void addGroup(String group) { groups.add(group); }
    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}