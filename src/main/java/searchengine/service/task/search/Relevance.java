package searchengine.service.task.search;

import lombok.Data;
import searchengine.dto.search.SearchFormat;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.Page;
import searchengine.model.site.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;
@Data
public class Relevance {
    private List<Thread> threads = new ArrayList<>();
    private Map<Page, Integer> relevanceMap = new HashMap<>();
    private List<String> urlList = new ArrayList<>();
    private int max;

    private Map<String, Integer> sorted(Map<String, Integer> map) {
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

    private void calculateRank(int rank) {
        if (rank > max) {
            max = rank;
        }
    }
    public void addUrl(SiteRepository siteRepository, SearchFormat searchFormat){
        if (searchFormat.getSite() == null) {
            siteRepository.findAll().forEach(s -> {
                urlList.add(s.getUrl());
            });
        } else {
            urlList.add(searchFormat.getSite());
        }
    }

    public void mapFinalPage(Map<String, Integer> lemmaMap, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        for (Map.Entry<String, Integer> entry : sorted(lemmaMap).entrySet()) {
            lemmaRepository.findAll().forEach(lemma -> {
                if (lemma.getLemma().equals(entry.getKey()) && urlList.contains(lemma.getSite().getUrl())) {
                    indexRepository.findAll().forEach(identifier -> {
                        if (identifier.getLemma().equals(lemma)) {
                            int rank;
                            if (relevanceMap.containsKey(identifier.getPage())) {
                                rank = relevanceMap.get(identifier.getPage()) + identifier.getNumber();
                            } else {
                                rank = identifier.getNumber();
                            }
                            calculateRank(rank);
                            relevanceMap.put(identifier.getPage(), rank);
                        }
                    });
                }
            });
        }
    }
}
