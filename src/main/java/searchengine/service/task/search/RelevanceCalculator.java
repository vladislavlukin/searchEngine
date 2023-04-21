package searchengine.service.task.search;

import lombok.Getter;
import searchengine.dto.search.SearchFormat;
import searchengine.model.lemma.Identifier;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.Lemma;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.Page;
import searchengine.model.site.Site;
import searchengine.model.site.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class RelevanceCalculator {
    private final Map<Page, Integer> relevanceMap = new HashMap<>();
    private final List<Site> siteList = new ArrayList<>();
    private int maxRank;

    private void calculateMaxRank(int rank) {
        if (rank > maxRank) {
            maxRank = rank;
        }
    }

    public Map<Lemma, Integer> sorted(Map<Lemma, Integer> map) {
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

}


