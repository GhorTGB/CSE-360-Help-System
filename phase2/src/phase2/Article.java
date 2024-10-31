package phase2;

import java.io.Serializable;
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

    public Article(long id, String level, String title, String shortDescription, List<String> keywords, String body, List<String> links, boolean sensitive) {
        this.id = id;
        this.level = level;
        this.title = title;
        this.shortDescription = shortDescription;
        this.keywords = keywords;
        this.body = body;
        this.links = links;
        this.groups = new HashSet<>();
        this.sensitive = sensitive;
    }

  
    public long getId() { return id; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public List<String> getLinks() { return links; }
    public void setLinks(List<String> links) { this.links = links; }

    public Set<String> getGroups() { return groups; }
    public void setGroups(Set<String> groups) { this.groups = groups; }
    public void addGroup(String group) { groups.add(group); }

    public boolean isSensitive() { return sensitive; }
    public void setSensitive(boolean sensitive) { this.sensitive = sensitive; }
}
