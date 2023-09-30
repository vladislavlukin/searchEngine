package searchengine.service.task.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.repositories.IdentifierRepository;
import searchengine.model.Lemma;
import searchengine.repositories.LemmaRepository;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RelevanceCalculator {
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IdentifierRepository identifierRepository;

    public Map<Page, Float> searchRelevance(Set<String> lemmaSet, String url) {
        List<Site> sites = getSites(url);
        List<Lemma> lemmaList = lemmaRepository.findLemmasByLemmaNames(lemmaSet, sites);

        Map<Page, Integer> relevanceMap = new HashMap<>();
        Map<Page, List<Lemma>> pageLemmaMap = pageLemmasMap(lemmaList);

        pageLemmaMap.forEach((page, pageLemmas) -> pageLemmas.forEach(lemma -> relevanceMap.merge(page,
                identifierRepository.countLemmaNameInPage(lemma, page),
                Integer::sum)));

        int maxRank = relevanceMap.values().stream().max(Integer::compareTo).orElse(0);

        return relevanceMap.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(e -> -e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (float) entry.getValue() / maxRank,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

    }

    private List<Site> getSites(String url) {
        if (url == null) {
            return new ArrayList<>(siteRepository.getAllSites());
        } else {
            return Collections.singletonList(siteRepository.findByUrl(url));
        }
    }

    private Map<Page, List<Lemma>> pageLemmasMap(List<Lemma> lemmas) {
        Map<Page, List<Lemma>> pageLemmaMap = new HashMap<>();

        lemmas.forEach(lemma -> identifierRepository.findPagesByLemma(lemma)
                .forEach(page -> pageLemmaMap.computeIfAbsent(page, k -> new ArrayList<>()).add(lemma)));

        int maxListSize = pageLemmaMap.values().stream().mapToInt(List::size).max().orElse(0);

        pageLemmaMap.entrySet().removeIf(entry -> entry.getValue().size() != maxListSize);

        return pageLemmaMap;
    }
}



