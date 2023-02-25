package searchengine.services.indexing;

import searchengine.model.lemma.Identifier;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.Lemma;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.Page;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.services.LemmaFinder;

import java.util.*;

public class LemmaIndexing {
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    public LemmaIndexing(PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }
    private Map<String, Map<Page, Integer>> base = new HashMap<>();
    private Map<String, Integer> copyIndex = new HashMap<>();
    public Set<String> lemmaSet (Page page) throws Exception{
        copyIndex.putAll(LemmaFinder.getInstance().collectLemmas(page.getContent()));
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstance().getLemmaSet(page.getContent()));
        return lemmaSet;
    }
    public Lemma lemma (Site site, String text, int i){
        Lemma lemma = new Lemma();
        lemma.setSite(site);
        lemma.setLemma(text);
        lemma.setFrequency(i);

        return lemma;
    }
    public Identifier index (Page page, Lemma lemma, int i){
        Identifier index = new Identifier();
        index.setPage(page);
        index.setLemma(lemma);
        index.setNumber(i);

        return index;
    }

    public void startIndexingLemma(Site site) {
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
                        if(base.containsKey(lemma)){
                            Map<Page, Integer> copy = new HashMap<>(base.get(lemma));
                            copy.put(page, i);
                            base.put(lemma, copy);
                        }else {
                            Map<Page, Integer> pageRank = new HashMap<>();
                            pageRank.put(page, i);
                            base.put(lemma, pageRank);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        for(Map.Entry<String, Map<Page, Integer>> result : base.entrySet()){
            lemmaRepository.save(lemma(site, result.getKey(), base.get(result.getKey()).size()));
        }
        lemmaRepository.findAll().forEach(lemma -> {
            if(lemma.getSite().getId() == site.getId()){
                Map<Page, Integer> pageRank = new HashMap<>(base.get(lemma.getLemma()));
                for (Map.Entry<Page, Integer> result : pageRank.entrySet()){
                    indexRepository.save(index(result.getKey(), lemma, result.getValue()));
                }
            }
        });
    }

}
