package phase2;

import java.sql.*;
import java.util.*;

public class ArticleManager {

    private DatabaseHelper dbHelper = DatabaseHelper.getInstance();
    private long nextId = 1;

    public synchronized long createArticle(Article article) {
        try {
            Connection conn = dbHelper.getConnection();
            String insertArticle = "INSERT INTO articles (id, level, title, shortDescription, body, sensitive) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertArticle);
            long id = getNextId();
            pstmt.setLong(1, id);
            pstmt.setString(2, article.getLevel());
            pstmt.setString(3, article.getTitle());
            pstmt.setString(4, article.getShortDescription());
            pstmt.setString(5, article.getBody());
            pstmt.setBoolean(6, article.isSensitive());
            pstmt.executeUpdate();
            pstmt.close();

            insertKeywords(id, article.getKeywords());
            insertLinks(id, article.getLinks());
            insertGroups(id, article.getGroups());

            return id;
        } catch (SQLException e) {
            System.out.println("Error creating article: " + e.getMessage());
            return -1;
        }
    }

    private void insertKeywords(long articleId, List<String> keywords) throws SQLException {
        if (keywords == null) return;
        Connection conn = dbHelper.getConnection();
        String insertKeyword = "INSERT INTO article_keywords (article_id, keyword) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertKeyword);
        for (String keyword : keywords) {
            pstmt.setLong(1, articleId);
            pstmt.setString(2, keyword.trim());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
    }

    private void insertLinks(long articleId, List<String> links) throws SQLException {
        if (links == null) return;
        Connection conn = dbHelper.getConnection();
        String insertLink = "INSERT INTO article_links (article_id, link) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertLink);
        for (String link : links) {
            pstmt.setLong(1, articleId);
            pstmt.setString(2, link.trim());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
    }

    private void insertGroups(long articleId, Set<String> groups) throws SQLException {
        if (groups == null) return;
        Connection conn = dbHelper.getConnection();
        String insertGroup = "INSERT INTO article_groups (article_id, groupName) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertGroup);
        for (String group : groups) {
            pstmt.setLong(1, articleId);
            pstmt.setString(2, group.trim());
            pstmt.addBatch();
        }
        pstmt.executeBatch();
        pstmt.close();
    }

    private long getNextId() {
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT MAX(id) FROM articles";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                long maxId = rs.getLong(1);
                nextId = maxId + 1;
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextId++;
    }

    public void updateArticle(long id, Article updatedArticle) {
        try {
            Connection conn = dbHelper.getConnection();
            String updateArticle = "UPDATE articles SET level = ?, title = ?, shortDescription = ?, body = ?, sensitive = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateArticle);
            pstmt.setString(1, updatedArticle.getLevel());
            pstmt.setString(2, updatedArticle.getTitle());
            pstmt.setString(3, updatedArticle.getShortDescription());
            pstmt.setString(4, updatedArticle.getBody());
            pstmt.setBoolean(5, updatedArticle.isSensitive());
            pstmt.setLong(6, id);
            pstmt.executeUpdate();
            pstmt.close();

            deleteKeywords(id);
            insertKeywords(id, updatedArticle.getKeywords());

            deleteLinks(id);
            insertLinks(id, updatedArticle.getLinks());

            deleteArticleGroups(id);
            insertGroups(id, updatedArticle.getGroups());
        } catch (SQLException e) {
            System.out.println("Error updating article: " + e.getMessage());
        }
    }

    private void deleteKeywords(long articleId) throws SQLException {
        Connection conn = dbHelper.getConnection();
        String deleteKeywords = "DELETE FROM article_keywords WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(deleteKeywords);
        pstmt.setLong(1, articleId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void deleteLinks(long articleId) throws SQLException {
        Connection conn = dbHelper.getConnection();
        String deleteLinks = "DELETE FROM article_links WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(deleteLinks);
        pstmt.setLong(1, articleId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    private void deleteArticleGroups(long articleId) throws SQLException {
        Connection conn = dbHelper.getConnection();
        String deleteGroups = "DELETE FROM article_groups WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(deleteGroups);
        pstmt.setLong(1, articleId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public Article getArticle(long id) {
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT * FROM articles WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Article article = new Article(
                        id,
                        rs.getString("level"),
                        rs.getString("title"),
                        rs.getString("shortDescription"),
                        getKeywords(id),
                        rs.getString("body"),
                        getLinks(id),
                        rs.getBoolean("sensitive")
                );
                article.setGroups(getGroups(id));
                rs.close();
                pstmt.close();
                return article;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving article: " + e.getMessage());
        }
        return null;
    }

    private List<String> getKeywords(long articleId) throws SQLException {
        List<String> keywords = new ArrayList<>();
        Connection conn = dbHelper.getConnection();
        String query = "SELECT keyword FROM article_keywords WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setLong(1, articleId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            keywords.add(rs.getString("keyword"));
        }
        rs.close();
        pstmt.close();
        return keywords;
    }

    private List<String> getLinks(long articleId) throws SQLException {
        List<String> links = new ArrayList<>();
        Connection conn = dbHelper.getConnection();
        String query = "SELECT link FROM article_links WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setLong(1, articleId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            links.add(rs.getString("link"));
        }
        rs.close();
        pstmt.close();
        return links;
    }

    private Set<String> getGroups(long articleId) throws SQLException {
        Set<String> groups = new HashSet<>();
        Connection conn = dbHelper.getConnection();
        String query = "SELECT groupName FROM article_groups WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setLong(1, articleId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            groups.add(rs.getString("groupName"));
        }
        rs.close();
        pstmt.close();
        return groups;
    }

    public void deleteArticle(long id) {
        try {
            Connection conn = dbHelper.getConnection();
            String deleteArticle = "DELETE FROM articles WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteArticle);
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            pstmt.close();

            deleteKeywords(id);
            deleteLinks(id);
            deleteArticleGroups(id);

        } catch (SQLException e) {
            System.out.println("Error deleting article: " + e.getMessage());
        }
    }

    public void addArticleToGroup(long id, String groupName) {
        try {
            Connection conn = dbHelper.getConnection();
            String insertGroup = "INSERT INTO article_groups (article_id, groupName) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertGroup);
            pstmt.setLong(1, id);
            pstmt.setString(2, groupName.trim());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error adding article to group: " + e.getMessage());
        }
    }

    public Set<Article> getArticlesByGroup(String groupName) {
        Set<Article> articles = new HashSet<>();
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT article_id FROM article_groups WHERE groupName = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long articleId = rs.getLong("article_id");
                Article article = getArticle(articleId);
                if (article != null) {
                    articles.add(article);
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving articles by group: " + e.getMessage());
        }
        return articles;
    }

    public List<Article> searchArticles(String keyword) {
        List<Article> results = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT DISTINCT a.id FROM articles a " +
                           "JOIN article_keywords ak ON a.id = ak.article_id " +
                           "WHERE a.title LIKE ? OR ak.keyword LIKE ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            String keywordPattern = "%" + keyword.trim() + "%";
            pstmt.setString(1, keywordPattern);
            pstmt.setString(2, keywordPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                long articleId = rs.getLong("id");
                Article article = getArticle(articleId);
                if (article != null) {
                    results.add(article);
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error searching articles: " + e.getMessage());
        }
        return results;
    }

    public List<Article> getArticlesByLevel(String level) {
        List<Article> results = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT id FROM articles WHERE level = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, level.trim());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long articleId = rs.getLong("id");
                Article article = getArticle(articleId);
                if (article != null) {
                    results.add(article);
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving articles by level: " + e.getMessage());
        }
        return results;
    }

    public void backupArticles(String filename, boolean allArticles, Set<String> groupsToBackup) {
        try {
            Connection conn = dbHelper.getConnection();
            Statement stmt = conn.createStatement();
            String backupQuery = "SCRIPT TO '" + filename + "'";
            stmt.execute(backupQuery);
            stmt.close();
            System.out.println("Articles backed up successfully to " + filename);
        } catch (SQLException e) {
            System.out.println("Error backing up articles: " + e.getMessage());
        }
    }

    public void restoreArticles(String filename, boolean removeExisting, boolean merge) {
        try {
            Connection conn = dbHelper.getConnection();
            Statement stmt = conn.createStatement();

            if (removeExisting) {
                String deleteArticles = "DELETE FROM articles";
                stmt.execute(deleteArticles);
                stmt.execute("DELETE FROM article_keywords");
                stmt.execute("DELETE FROM article_links");
                stmt.execute("DELETE FROM article_groups");
            }

            String restoreQuery = "RUNSCRIPT FROM '" + filename + "'";
            stmt.execute(restoreQuery);
            stmt.close();
            System.out.println("Articles restored successfully from " + filename);
        } catch (SQLException e) {
            System.out.println("Error restoring articles: " + e.getMessage());
        }
    }
    
    public List<Article> listAllArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String query = "SELECT * FROM articles";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong("id");
                String level = rs.getString("level");
                String title = rs.getString("title");
                String shortDescription = rs.getString("shortDescription");
                String body = rs.getString("body");
                boolean sensitive = rs.getBoolean("sensitive");

                Article article = new Article(
                    id, level, title, shortDescription,
                    getKeywords(id), body, getLinks(id), sensitive
                );
                article.setGroups(getGroups(id));
                articles.add(article);
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error listing all articles: " + e.getMessage());
        }
        return articles;
    }

}
