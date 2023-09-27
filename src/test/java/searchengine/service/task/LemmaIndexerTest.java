package searchengine.service.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.model.Page;
import searchengine.service.task.indexing.LemmaIndexer;

import java.util.*;

public class LemmaIndexerTest {

    private LemmaIndexer lemmaIndexer;

    @BeforeEach
    public void setUp() {
        lemmaIndexer = new LemmaIndexer();
    }

    @Test
    public void testStartLemmaIndexing() {
        List<Page> pagesRu = new ArrayList<>();
        pagesRu.add(Page.builder().code(200).content("тестовое тестовый").build());
        pagesRu.add(Page.builder().code(200).content("значение").build());
        pagesRu.add(Page.builder().code(404).content("значение").build());

        List<Page> pagesEn = new ArrayList<>();
        pagesEn.add(Page.builder().code(200).content("Hello").build());

        Map<String, Map<Page, Integer>> resultRu = lemmaIndexer.indexedLemmasOnPages(pagesRu);

        Map<String, Map<Page, Integer>> resultEn = lemmaIndexer.indexedLemmasOnPages(pagesEn);

        Map<String, Map<Page, Integer>> expectedRu = new HashMap<>();
        expectedRu.put("тестовый", new HashMap<>(Map.of(pagesRu.get(0), 2)));
        expectedRu.put("значение", new HashMap<>(Map.of(pagesRu.get(1), 1)));

        Map<String, Map<Page, Integer>> expectedEn = new HashMap<>();
        expectedEn.put("hello", new HashMap<>(Map.of(pagesEn.get(0), 1)));

        assertEquals(expectedRu, resultRu);
        assertEquals(expectedEn, resultEn);
    }
}


