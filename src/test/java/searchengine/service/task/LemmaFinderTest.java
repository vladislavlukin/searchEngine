package searchengine.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.service.task.indexing.LemmaFinder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LemmaFinderTest {

    private LemmaFinder lemmaFinder;

    @BeforeEach
    public void setUp() throws IOException {
        lemmaFinder = LemmaFinder.getInstance();
    }

    @Test
    public void testGetLemmasRu() throws IOException {
        String html = "<html><body>Это текст на русском языке для тестирования</body></html>";
        List<String> lemmas = lemmaFinder.getLemmas(html);

        assertTrue(lemmas.contains("этот"));
        assertTrue(lemmas.contains("текст"));
        assertTrue(lemmas.contains("русский"));
        assertTrue(lemmas.contains("язык"));
        assertTrue(lemmas.contains("тестирование"));

        assertFalse(lemmas.contains("на"));
        assertFalse(lemmas.contains("для"));
    }

    @Test
    public void testGetLemmasEn() throws IOException {
        String html = "<html><body>This is some English text for testing</body></html>";
        List<String> lemmas = lemmaFinder.getLemmas(html);

        assertTrue(lemmas.contains("this"));
        assertTrue(lemmas.contains("some"));
        assertTrue(lemmas.contains("english"));
        assertTrue(lemmas.contains("text"));
        assertTrue(lemmas.contains("for"));
        assertTrue(lemmas.contains("testing"));

        assertFalse(lemmas.contains("is"));
    }

}

