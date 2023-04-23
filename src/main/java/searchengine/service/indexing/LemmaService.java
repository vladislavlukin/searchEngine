package searchengine.service.indexing;

import lombok.Getter;
import searchengine.model.lemma.Identifier;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.Lemma;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.Page;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.service.task.indexing.LemmaFinder;

import java.util.*;
@Getter
public class LemmaService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final Map<String, Map<Page, Integer>> lemmaOfPagesMap = new HashMap<>();
    private final Map<String, Integer> lemmaMap = new HashMap<>();
    private Lemma lemma;
    private Identifier index;

    public LemmaService(PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }
    public void startLemmaIndexing(Site site) {
        pageRepository.findAll().forEach(page -> {
            try {
                int codeStatus = 200;
                if (page.getSite().getId() == site.getId() && page.getCode() == codeStatus) {
                    for (String lemma : getLemmaSet(page)) {
                        int countLemma;
                        if(lemmaMap.get(lemma) != null){
                            countLemma = lemmaMap.get(lemma);
                        }else {
                            countLemma = 0;
                        }
                        Map<Page, Integer> pagesWithCountLemma;
                        if(lemmaOfPagesMap.containsKey(lemma)){
                            pagesWithCountLemma = new HashMap<>(lemmaOfPagesMap.get(lemma));
                        }else {
                            pagesWithCountLemma = new HashMap<>();
                        }
                        pagesWithCountLemma.put(page, countLemma);
                        lemmaOfPagesMap.put(lemma, pagesWithCountLemma);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        addLammas(site);
        addIndexes(site);
        lemmaMap.clear();
        lemmaOfPagesMap.clear();
    }
    private void fillingLemma(Site site, String text, int countLemma){
        lemma = new Lemma();
        lemma.setSite(site);
        lemma.setLemma(text);
        lemma.setFrequency(countLemma);
    }
    private void fillingIndex(Page page, Lemma lemma, int countLemma){
        index = new Identifier();
        index.setPage(page);
        index.setLemma(lemma);
        index.setNumber(countLemma);
    }
    private Set<String> getLemmaSet(Page page) throws Exception{
        lemmaMap.putAll(LemmaFinder.getInstance().collectLemmas(page.getContent()));
        return new HashSet<>(LemmaFinder.getInstance().getLemmaSet(page.getContent()));
    }
    private void addLammas (Site site){
        for(Map.Entry<String, Map<Page, Integer>> result : lemmaOfPagesMap.entrySet()){
            String lemma = result.getKey();
            int countLemma = lemmaOfPagesMap.get(lemma).size();
            fillingLemma(site, lemma, countLemma);
            lemmaRepository.save(getLemma());
        }
    }
    private void addIndexes(Site site){
        lemmaRepository.findAll().forEach(lemma -> {
            if(lemma.getSite().getId() == site.getId()){
                Map<Page, Integer> countLemmaOfPage = new HashMap<>(lemmaOfPagesMap.get(lemma.getLemma()));
                for (Map.Entry<Page, Integer> result : countLemmaOfPage.entrySet()){
                    Page page = result.getKey();
                    int countLemma = result.getValue();
                    fillingIndex(page, lemma, countLemma);
                    indexRepository.save(getIndex());
                }
            }
        });
    }
}
