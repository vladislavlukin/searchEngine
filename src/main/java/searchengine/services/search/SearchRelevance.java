package searchengine.services.search;

import searchengine.dto.search.SearchFormat;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.Page;
import searchengine.model.site.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

public class SearchRelevance {
    public SearchRelevance(SiteRepository siteRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, SearchFormat searchFormat) {
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.searchFormat = searchFormat;
    }

    private SiteRepository siteRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private SearchFormat searchFormat;
    private List<Thread> threads = new ArrayList<>();
    private Map<Page, Integer> result = new HashMap<>();
    private List<String> urlList = new ArrayList<>();
    private int max;

    public Map<String, Integer> sorted(Map<String, Integer> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }

    public void calculateRank(int rank) {
        if (rank > max) {
            max = rank;
        }
    }

    public Integer maxRank() {
        return max;
    }

    public Map<Page, Integer> mapFinalPage(Map<String, Integer> lemmaMap) {
        if (searchFormat.getSite() == null) {
            siteRepository.findAll().forEach(s -> {
                urlList.add(s.getUrl());
            });
        } else {
            urlList.add(searchFormat.getSite());
        }

        for (Map.Entry<String, Integer> entry : sorted(lemmaMap).entrySet()) {
            lemmaRepository.findAll().forEach(lemma -> {
                if (lemma.getLemma().equals(entry.getKey()) && urlList.contains(lemma.getSite().getUrl())) {
                    indexRepository.findAll().forEach(identifier -> {
                        if (identifier.getLemma().equals(lemma)) {
                            int rank;
                            if (result.containsKey(identifier.getPage())) {
                                rank = result.get(identifier.getPage()) + identifier.getNumber();
                            } else {
                                rank = identifier.getNumber();
                            }
                            calculateRank(rank);
                            result.put(identifier.getPage(), rank);
                        }
                    });
                }
            });
        }
        return result;
    }
}
