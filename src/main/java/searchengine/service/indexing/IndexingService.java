package searchengine.service.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.Status;
import searchengine.model.Identifier;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.service.task.indexing.LemmaIndexer;
import searchengine.service.task.indexing.SiteScanner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IndexingService {
    private final SiteRepository siteRepository;
    private final LemmaIndexer lemmaIndexer;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IdentifierRepository identifierRepository;
    private final ThreadManager threadManager;

    public void startIndexing() {
        validateStartIndexing();

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                Thread thread = new Thread(() -> {
                    indexingSite(site);
                });
                threadManager.addThread(thread);
            }
        });
        threadManager.startAllThreads();
    }

    public void stopIndexing() {
        validateStopIndexing();

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                site.setStatus(Status.INDEXED);
                site.setError("Индексация остановлена пользователем");
                site.setCreationTime(LocalDateTime.now());
                siteRepository.save(site);
            }
        });
        threadManager.stopAllThreads();
    }
    private void indexingSite(Site site) {
        List<Page> pages = new ForkJoinPool().invoke(new SiteScanner(site.getUrl(), site));
        pageRepository.saveAll(pages);

        Map<String, Map<Page, Integer>> indexedLemmasOnPages = lemmaIndexer.indexedLemmasOnPages(pages);
        List<Lemma> lemmas = getLemmasForSite(site, indexedLemmasOnPages);
        List<Identifier> identifiers = getIndexesByLemmas(indexedLemmasOnPages, lemmas);

        lemmaRepository.saveAll(lemmas);
        identifierRepository.saveAll(identifiers);

        site.setStatus(Status.INDEXED);
        site.setCreationTime(LocalDateTime.now());

        siteRepository.save(site);
    }
    private List<Lemma> getLemmasForSite (Site site, Map<String, Map<Page, Integer>> lemmaOfPagesMap){
        return lemmaOfPagesMap.entrySet()
                .stream()
                .map(entry -> {
                    String lemmaString = entry.getKey();
                    int countLemma = entry.getValue().size();
                    return Lemma.builder()
                            .lemma(lemmaString)
                            .site(site)
                            .frequency(countLemma)
                            .build();
                })
                .collect(Collectors.toList());

    }
    private List<Identifier> getIndexesByLemmas(Map<String, Map<Page, Integer>> lemmaOfPagesMap, List<Lemma> lemmaList){
        return lemmaList.stream()
                .flatMap(lemma -> {
                    Map<Page, Integer> countLemmaOfPage = new HashMap<>(lemmaOfPagesMap.get(lemma.getLemma()));
                    return countLemmaOfPage.entrySet().stream()
                            .map(entry -> {
                                Page page = entry.getKey();
                                int countLemma = entry.getValue();
                                return Identifier.builder()
                                        .lemma(lemma)
                                        .page(page)
                                        .number(countLemma)
                                        .build();
                            });
                })
                .collect(Collectors.toList());
    }

    private void validateStartIndexing() {
        if (!siteRepository.isIndexingStatus()) {
            throw new IllegalArgumentException("Добавьте не менее одного сайта или обновите текущий");
        }
        if (threadManager.areThreadsAlive()) {
            throw new IllegalArgumentException("Индексация уже запущена");
        }
    }

    private void validateStopIndexing() {
        if (threadManager.areThreadsNotAlive()) {
            throw new IllegalArgumentException("Индексация не запущена");
        }
    }

}
