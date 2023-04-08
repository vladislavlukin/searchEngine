package searchengine.service.indexing;

import searchengine.model.lemma.Identifier;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.Lemma;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.Page;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.service.task.indexing.LemmaFinder;

import java.util.*;

public class LemmaService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Map<String, Map<Page, Integer>> lemmaOfPage = new HashMap<>();
    private final Map<String, Integer> copyIndex = new HashMap<>();

    public LemmaService(PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }
    private Lemma lemma (Site site, String text, int i){
        Lemma lemma = new Lemma();
        lemma.setSite(site);
        lemma.setLemma(text);
        lemma.setFrequency(i);

        return lemma;
    }
    private Identifier index (Page page, Lemma lemma, int i){
        Identifier index = new Identifier();
        index.setPage(page);
        index.setLemma(lemma);
        index.setNumber(i);

        return index;
    }
    private Set<String> lemmaSet (Page page) throws Exception{
        copyIndex.putAll(LemmaFinder.getInstance().collectLemmas(page.getContent()));
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstance().getLemmaSet(page.getContent()));
        return lemmaSet;
    }

    public void lemmaIndexing(Site site) {
        pageRepository.findAll().forEach(page -> {
            try {
                if (page.getSite().getId() == site.getId() && page.getCode() == 200) {
                    for (String lemma : lemmaSet(page)) {
                        int i;
                        if(copyIndex.get(lemma) != null){
                            i = copyIndex.get(lemma);
                        }else {
                            i = 0;
                        }
                        if(lemmaOfPage.containsKey(lemma)){
                            Map<Page, Integer> copyCountLemmaOfPage = new HashMap<>(lemmaOfPage.get(lemma));
                            copyCountLemmaOfPage.put(page, i);
                            lemmaOfPage.put(lemma, copyCountLemmaOfPage);
                        }else {
                            Map<Page, Integer> countLemmaOfPage = new HashMap<>();
                            countLemmaOfPage.put(page, i);
                            lemmaOfPage.put(lemma, countLemmaOfPage);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        for(Map.Entry<String, Map<Page, Integer>> result : lemmaOfPage.entrySet()){
            lemmaRepository.save(lemma(site, result.getKey(), lemmaOfPage.get(result.getKey()).size()));
        }
        lemmaRepository.findAll().forEach(lemma -> {
            if(lemma.getSite().getId() == site.getId()){
                Map<Page, Integer> countLemmaOfPage = new HashMap<>(lemmaOfPage.get(lemma.getLemma()));
                for (Map.Entry<Page, Integer> result : countLemmaOfPage.entrySet()){
                    indexRepository.save(index(result.getKey(), lemma, result.getValue()));
                }
            }
        });
    }

}
