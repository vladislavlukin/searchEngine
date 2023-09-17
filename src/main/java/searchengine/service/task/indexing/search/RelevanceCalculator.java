package searchengine.service.task.indexing.search;

import lombok.Getter;
import searchengine.dto.search.SearchFormat;
import searchengine.model.Identifier;
import searchengine.repositories.IndexRepository;
import searchengine.model.Lemma;
import searchengine.repositories.LemmaRepository;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class RelevanceCalculator {
    private final Map<Page, Integer> relevanceMap = new HashMap<>();
    private final List<Site> siteList = new ArrayList<>();
    private int maxRank;
    public void addSites(SiteRepository siteRepository, SearchFormat searchFormat) {
        siteRepository.findAll().forEach(s -> {
            if (searchFormat.getSite() == null) {
                siteList.add(s);
            } else if (s.getUrl().equals(searchFormat.getSite())) {
                siteList.add(s);
            }
        });
    }

    public void searchRelevance(Set<String> lemmaSet, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        for (Map.Entry<Lemma, Integer> entry : sorted(getLemmaMap(lemmaRepository, lemmaSet)).entrySet()) {
            Lemma lemma = entry.getKey();
            List<Identifier> indexList = indexRepository.getIndexes(lemma);
            for (Identifier identifier : indexList) {
                int rank;
                if (relevanceMap.containsKey(identifier.getPage())) {
                    rank = relevanceMap.get(identifier.getPage()) + identifier.getNumber();
                } else {
                    rank = identifier.getNumber();
                }
                calculateMaxRank(rank);
                relevanceMap.put(identifier.getPage(), rank);
            }
        }
    }
    private Map<Lemma, Integer> getLemmaMap(LemmaRepository lemmaRepository, Set<String> lemmaSet) {
        Map<Lemma, Integer> lemmaMap = new HashMap<>();
        for (String lemma : lemmaSet) {
            for (Site site : siteList) {
                List<Lemma> lemmaList = lemmaRepository.getLemmas(lemma, site);
                for (Lemma l : lemmaList) {
                    lemmaMap.put(l, l.getFrequency());
                }
            }
        }
        siteList.clear();
        return sorted(lemmaMap);
    }
    private void calculateMaxRank(int rank) {
        if (rank > maxRank) {
            maxRank = rank;
        }
    }
    private Map<Lemma, Integer> sorted(Map<Lemma, Integer> map) {
        return map.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }
}


