package phase3;

import java.io.*;
import java.sql.*;
import java.util.*;

public class ArticleManager {
    private DatabaseHelper dbHelper;

    public ArticleManager() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public long createArticle(Article article) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO articles (id, level, title, shortDescription, body, sensitive, author) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            long id = System.currentTimeMillis();
            pstmt.setLong(1, id);
            pstmt.setString(2, article.getLevel());
            pstmt.setString(3, article.getTitle());
            pstmt.setString(4, article.getShortDescription());
            if (article.isSensitive()) {
                String encryptedBody = EncryptionUtils.encrypt(article.getBody());
                pstmt.setString(5, encryptedBody);
            } else {
                pstmt.setString(5, article.getBody());
            }
            pstmt.setBoolean(6, article.isSensitive());
            pstmt.setString(7, article.getAuthor());
            pstmt.executeUpdate();
            pstmt.close();
            saveKeywords(id, article.getKeywords());
            saveLinks(id, article.getLinks());
            saveGroups(id, article.getGroups());
            return id;
        } catch (SQLException e) {
            System.out.println("Error creating article: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.out.println("Encryption error: " + e.getMessage());
            return -1;
        }
    }

    private void saveKeywords(long articleId, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO article_keywords (article_id, keyword) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (String keyword : keywords) {
                pstmt.setLong(1, articleId);
                pstmt.setString(2, keyword.trim());
                pstmt.executeUpdate();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error saving keywords: " + e.getMessage());
        }
    }

    private void saveLinks(long articleId, List<String> links) {
        if (links == null || links.isEmpty()) {
            return;
        }
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO article_links (article_id, link) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (String link : links) {
                pstmt.setLong(1, articleId);
                pstmt.setString(2, link.trim());
                pstmt.executeUpdate();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error saving links: " + e.getMessage());
        }
    }

    private void saveGroups(long articleId, Set<String> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO article_groups (article_id, groupName) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            for (String group : groups) {
                pstmt.setLong(1, articleId);
                pstmt.setString(2, group.trim());
                pstmt.executeUpdate();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error saving groups: " + e.getMessage());
        }
    }

    public Article getArticle(long id) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT * FROM articles WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String level = rs.getString("level");
                String title = rs.getString("title");
                String shortDescription = rs.getString("shortDescription");
                String body = rs.getString("body");
                boolean sensitive = rs.getBoolean("sensitive");
                String author = rs.getString("author");
                if (sensitive) {
                    body = EncryptionUtils.decrypt(body);
                }
                List<String> keywords = getKeywords(id);
                List<String> links = getLinks(id);
                Set<String> groups = getGroups(id);
                Article article = new Article(id, level, title, shortDescription, keywords, body, links, sensitive);
                article.setGroups(groups);
                article.setAuthor(author);
                rs.close();
                pstmt.close();
                return article;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving article: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Decryption error: " + e.getMessage());
        }
        return null;
    }

    private List<String> getKeywords(long articleId) {
        List<String> keywords = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT keyword FROM article_keywords WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                keywords.add(rs.getString("keyword"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving keywords: " + e.getMessage());
        }
        return keywords;
    }

    private List<String> getLinks(long articleId) {
        List<String> links = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT link FROM article_links WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                links.add(rs.getString("link"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving links: " + e.getMessage());
        }
        return links;
    }

    private Set<String> getGroups(long articleId) {
        Set<String> groups = new HashSet<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT groupName FROM article_groups WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                groups.add(rs.getString("groupName"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving groups: " + e.getMessage());
        }
        return groups;
    }

    public long updateArticle(long id, Article updatedArticle) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "UPDATE articles SET level = ?, title = ?, shortDescription = ?, body = ?, sensitive = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, updatedArticle.getLevel());
            pstmt.setString(2, updatedArticle.getTitle());
            pstmt.setString(3, updatedArticle.getShortDescription());
            if (updatedArticle.isSensitive()) {
                String encryptedBody = EncryptionUtils.encrypt(updatedArticle.getBody());
                pstmt.setString(4, encryptedBody);
            } else {
                pstmt.setString(4, updatedArticle.getBody());
            }
            pstmt.setBoolean(5, updatedArticle.isSensitive());
            pstmt.setLong(6, id);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            if (affectedRows > 0) {
                deleteKeywords(id);
                saveKeywords(id, updatedArticle.getKeywords());
                deleteLinks(id);
                saveLinks(id, updatedArticle.getLinks());
                deleteGroups(id);
                saveGroups(id, updatedArticle.getGroups());
                return id;
            } else {
                System.out.println("No article found with ID: " + id);
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error updating article: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.out.println("Encryption error: " + e.getMessage());
            return -1;
        }
    }

    private void deleteKeywords(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM article_keywords WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error deleting keywords: " + e.getMessage());
        }
    }

    private void deleteLinks(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM article_links WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error deleting links: " + e.getMessage());
        }
    }

    private void deleteGroups(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "DELETE FROM article_groups WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error deleting groups: " + e.getMessage());
        }
    }

    public boolean deleteArticle(long articleId) {
        Connection conn = null;  // Ensure this is declared and used properly
        try {
            conn = dbHelper.getConnection();

            // Delete related data (keywords, links, groups)
            deleteRelatedData(conn, articleId);

            // Delete the article itself
            String sql = "DELETE FROM articles WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            int affectedRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (affectedRows > 0) {
                System.out.println("Article deleted successfully.");
                return true;
            } else {
                System.out.println("Article not found with ID: " + articleId);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error deleting article: " + e.getMessage());
            return false;
        } finally {
            // Ensure that the connection is closed after use
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }


    private void deleteRelatedData(Connection conn, long articleId) {
        try {
            // Delete keywords, links, and groups related to the article
            deleteData(conn, "article_keywords", articleId);
            deleteData(conn, "article_links", articleId);
            deleteData(conn, "article_groups", articleId);
        } catch (SQLException e) {
            System.out.println("Error deleting related data: " + e.getMessage());
        }
    }
    private void deleteData(Connection conn, String tableName, long articleId) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE article_id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, articleId);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public List<Article> listAllArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT id FROM articles";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                long id = rs.getLong("id");
                Article article = getArticle(id);
                if (article != null) {
                    articles.add(article);
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error listing articles: " + e.getMessage());
        }
        return articles;
    }

    public List<Article> searchArticles(String keyword, String level, String groupName) {
        List<Article> articles = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT a.id FROM articles a WHERE a.title LIKE ?"
            );
            if (!"all".equals(level)) {
                sql.append(" AND a.level = ?");
            }
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            pstmt.setString(1, "%" + keyword + "%");
            if (!"all".equals(level)) {
                pstmt.setString(2, level);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                articles.add(getArticle(rs.getLong("id")));
            }
        } catch (SQLException e) {
            System.out.println("Error searching articles: " + e.getMessage());
        }
        return articles;
    }

    public List<Article> getArticlesByLevel(String level) {
        List<Article> articles = new ArrayList<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT id FROM articles WHERE level = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, level);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                Article article = getArticle(id);
                if (article != null) {
                    articles.add(article);
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error retrieving articles by level: " + e.getMessage());
        }
        return articles;
    }

    public void addArticleToGroup(long articleId, String groupName) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO article_groups (article_id, groupName) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.setString(2, groupName);
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
            String sql = "SELECT article_id FROM article_groups WHERE groupName = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("article_id");
                Article article = getArticle(id);
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

    public boolean userHasAccessToArticle(String username, long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT groupName FROM article_groups WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            ResultSet rs = pstmt.executeQuery();
            Set<String> articleGroups = new HashSet<>();
            while (rs.next()) {
                articleGroups.add(rs.getString("groupName"));
            }
            rs.close();
            pstmt.close();
            if (articleGroups.isEmpty()) {
                return true;
            }
            sql = "SELECT groupName FROM group_users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            Set<String> userGroups = new HashSet<>();
            while (rs.next()) {
                userGroups.add(rs.getString("groupName"));
            }
            rs.close();
            pstmt.close();
            for (String group : articleGroups) {
                if (userGroups.contains(group)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking user access to article: " + e.getMessage());
        }
        return false;
    }

    public boolean backupArticles(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            
            // Backup the article
            String sql = "INSERT INTO backup_articles (id, level, title, shortDescription, body, sensitive, author) " +
                         "SELECT id, level, title, shortDescription, body, sensitive, author FROM articles WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                // Backup keywords
                backupArticleKeywords(articleId);

                // Backup links
                backupArticleLinks(articleId);

                // Backup groups
                backupArticleGroups(articleId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("Error backing up article: " + e.getMessage());
            return false;
        }
    }

    private void backupArticleKeywords(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO backup_article_keywords (article_id, keyword) " +
                         "SELECT article_id, keyword FROM article_keywords WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error backing up article keywords: " + e.getMessage());
        }
    }

    private void backupArticleLinks(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO backup_article_links (article_id, link) " +
                         "SELECT article_id, link FROM article_links WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error backing up article links: " + e.getMessage());
        }
    }

    private void backupArticleGroups(long articleId) {
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "INSERT INTO backup_article_groups (article_id, groupName) " +
                         "SELECT article_id, groupName FROM article_groups WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error backing up article groups: " + e.getMessage());
        }
    }


    public boolean restoreArticles(long articleId) {
        Connection conn = null;
        try {
            conn = dbHelper.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM articles WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setLong(1, articleId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Article with ID " + articleId + " already exists.");
                return false;
            }

            String restoreArticleSql = "INSERT INTO articles (id, level, title, shortDescription, body, sensitive, author) " +
                                       "SELECT id, level, title, shortDescription, body, sensitive, author FROM backup_articles WHERE id = ?";
            PreparedStatement restoreArticleStmt = conn.prepareStatement(restoreArticleSql);
            restoreArticleStmt.setLong(1, articleId);
            int rows = restoreArticleStmt.executeUpdate();
            
            if (rows > 0) {
                restoreArticleKeywords(conn, articleId);
                restoreArticleLinks(conn, articleId);
                restoreArticleGroups(conn, articleId);

                conn.commit();
                System.out.println("Article restored successfully.");
                return true;
            }

            conn.rollback();
            System.out.println("Error: Article not found in backup.");
            return false;
        } catch (SQLException e) {
            System.out.println("Error restoring article: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Error rolling back: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }


    private void restoreArticleKeywords(Connection conn, long articleId) {
        try {
            String restoreSql = "INSERT INTO article_keywords (article_id, keyword) " +
                                "SELECT article_id, keyword FROM backup_article_keywords WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(restoreSql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error restoring article keywords: " + e.getMessage());
        }
    }


    private void restoreArticleLinks(Connection conn, long articleId) {
        try {
            String restoreSql = "INSERT INTO article_links (article_id, link) " +
                                "SELECT article_id, link FROM backup_article_links WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(restoreSql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error restoring article links: " + e.getMessage());
        }
    }


    private void restoreArticleGroups(Connection conn, long articleId) {
        try {
            String restoreSql = "INSERT INTO article_groups (article_id, groupName) " +
                                "SELECT article_id, groupName FROM backup_article_groups WHERE article_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(restoreSql);
            pstmt.setLong(1, articleId);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error restoring article groups: " + e.getMessage());
        }
    }


    private Set<Article> getSpecialAccessGroupArticles() {
        Set<Article> articles = new HashSet<>();
        try {
            Connection conn = dbHelper.getConnection();
            String sql = "SELECT DISTINCT article_id FROM article_groups WHERE groupName LIKE 'Special%'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
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
            System.out.println("Error retrieving articles from special access groups: " + e.getMessage());
        }
        return articles;
    }

    public void backupSpecialAccessGroups(String filename) {
        Set<Article> articles = getSpecialAccessGroupArticles();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(new ArrayList<>(articles));
            System.out.println("Backup of special access groups successful: " + articles.size() + " articles backed up.");
        } catch (IOException e) {
            System.out.println("Error during backup of special access groups: " + e.getMessage());
        }
    }
}