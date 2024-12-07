package phase3;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

public class ArticleTest {

    @Test
    public void testCreateArticle() {
        ArticleManager articleManager = new ArticleManager();
        Article article = new Article(
            0,
            "beginner",
            "Test Article",
            "This is a test article.",
            Arrays.asList("test", "article"),
            "Test body content",
            Arrays.asList("http://example.com"),
            false
        );
        article.setAuthor("authorUser");
        long id = articleManager.createArticle(article);
        assertTrue(id > 0);
        Article retrievedArticle = articleManager.getArticle(id);
        assertNotNull(retrievedArticle);
        assertEquals(article.getTitle(), retrievedArticle.getTitle());
        assertEquals(article.getShortDescription(), retrievedArticle.getShortDescription());
        assertEquals(article.getBody(), retrievedArticle.getBody());
        assertEquals(article.getKeywords(), retrievedArticle.getKeywords());
        assertEquals(article.getLinks(), retrievedArticle.getLinks());
        assertEquals(article.getAuthor(), retrievedArticle.getAuthor());
        assertEquals(article.isSensitive(), retrievedArticle.isSensitive());
    }
}