package searchengine.service.task.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.model.Page;

import java.io.IOException;
import java.util.*;
@Component
@RequiredArgsConstructor
public class LemmaIndexer {
    public Map<String, Map<Page, Integer>> indexedLemmasOnPages(List<Page> pages) {
        Map<String, Map<Page, Integer>> lemmaOfPagesMap = new HashMap<>();
        pages = pages.stream().filter(page -> page.getCode() == 200).toList();
        pages.forEach(page -> {
            try {
                getLemmas(page).forEach(l -> {
                    lemmaOfPagesMap.computeIfAbsent(l, k -> new HashMap<>())
                            .merge(page, 1, Integer::sum);
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return lemmaOfPagesMap;
    }

    private List<String> getLemmas(Page page) throws IOException {
        String content = page.getContent();
        return LemmaFinder.getInstance().getLemmas(content);
    }
}
