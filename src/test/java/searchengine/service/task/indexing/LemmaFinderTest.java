package searchengine.service.task.indexing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.service.task.indexing.indexing.LemmaFinder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class LemmaFinderTest {

    private LemmaFinder lemmaFinder;

    @BeforeEach
    public void setUp() throws IOException {
        lemmaFinder = LemmaFinder.getInstance();
    }

    @Test
    public void testCollectLemmas() {
        String html = "<html><body>красивейший красивая краснее краски</body></html>";
        Map<String, Integer> lemmas = lemmaFinder.collectLemmas(html);

        assertEquals(lemmas.get("красивый"), 2);
        assertEquals(lemmas.get("красный"), 1);
        assertEquals(lemmas.get("краска"), 1);
    }

    @Test
    public void testGetLemmaSet() {
        String html = "<html><body>красивейший краснее краски</body></html>";
        Set<String> lemmaSet = lemmaFinder.getLemmaSet(html);

        assertTrue(lemmaSet.contains("красивый"));
        assertTrue(lemmaSet.contains("красный"));
        assertTrue(lemmaSet.contains("краска"));
    }
}

