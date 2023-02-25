package searchengine.services.delete;

import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.SiteRepository;

public class DeleteSite {
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    public DeleteSite(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }
    public void delete(String nameURL){
        siteRepository.findAll().forEach(site -> {
            if (site.getUrl().equals(nameURL)) {
                lemmaRepository.findAll().forEach(lemma -> {
                    if(lemma.getSite().equals(site)){
                        indexRepository.findAll().forEach(identifier -> {
                            if(identifier.getLemma().equals(lemma)){
                                indexRepository.delete(identifier);
                            }
                        });
                        lemmaRepository.delete(lemma);
                    }
                });
                pageRepository.findAll().forEach(page -> {
                    if (page.getSite().equals(site)) {
                        pageRepository.delete(page);
                    }
                });
                siteRepository.delete(site);
            }
        });
    }
}
