package searchengine.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.service.task.indexing.LemmaFinder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LemmaFinderTest {

    private LemmaFinder lemmaFinderRu;
    private LemmaFinder lemmaFinderEn;

    @BeforeEach
    public void setUp() throws IOException {
        lemmaFinderRu = LemmaFinder.getInstanceRu();
        lemmaFinderEn = LemmaFinder.getInstanceEn();
    }

    @Test
    public void testGetLemmas() {
        String html = "<html><body>Это текст на русском языке для тестирования</body></html>";
        List<String> lemmaSet = lemmaFinderRu.getLemmas(html);

        assertTrue(lemmaSet.contains("этот"));
        assertTrue(lemmaSet.contains("текст"));
        assertTrue(lemmaSet.contains("русский"));
        assertTrue(lemmaSet.contains("язык"));
        assertTrue(lemmaSet.contains("тестирование"));

        assertFalse(lemmaSet.contains("на"));
        assertFalse(lemmaSet.contains("для"));
    }

    @Test
    public void testGetLemmasEn() {
        String html = "<html><body>This is some English text for testing</body></html>";
        List<String> lemmaSet = lemmaFinderEn.getLemmasEn(html);

        assertTrue(lemmaSet.contains("this"));
        assertTrue(lemmaSet.contains("some"));
        assertTrue(lemmaSet.contains("english"));
        assertTrue(lemmaSet.contains("text"));
        assertTrue(lemmaSet.contains("for"));
        assertTrue(lemmaSet.contains("testing"));

        assertFalse(lemmaSet.contains("is"));
    }

}

