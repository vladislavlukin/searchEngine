package searchengine.services.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.model.site.SiteRepository;
import searchengine.model.site.Status;

import java.util.List;
import java.util.Set;

public class AddSite {
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private List<Thread> thread;
    private String url;
    private Set<String> sites;
    public AddSite(SiteRepository siteRepository,PageRepository pageRepository,
                   LemmaRepository lemmaRepository, IndexRepository indexRepository,
                   String url, Set<String> sites, List<Thread> thread) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.url = url;
        this.sites = sites;
        this.thread = thread;
    }
    private boolean threadIsNotLive(){
        if (!url.isEmpty()) {
            if (thread == null) {
                return true;
            } else {
                int i = 0;
                for (Thread newThread : thread) {
                    if (newThread.isAlive()) {
                        i++;
                    }
                }
                if (i == 0){
                    return true;
                }
            }
        }
        return false;
    }
    public void addUpdate(){
        siteRepository.findAll().forEach(site -> {
            sites.add(site.getUrl());
        });
        String regex = "http://";
        String regex1 = "https://";

        if (sites.contains(url) && threadIsNotLive()) {
            delete();
            sites.remove(url);
        }
        if (!sites.contains(url) && threadIsNotLive() && (url.startsWith(regex) || url.startsWith(regex1))) {
            add();
            sites.add(url);
        }
    }
    public Set<String> returnSites(){
        return sites;
    }

    private Site site () {
        Site indexingSite = new Site();
        indexingSite.setUrl(url);
        indexingSite.setStatus(Status.INDEXING);
        indexingSite.setError("");
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("title");
            indexingSite.setName(elements.text());
        } catch (Exception ex) {
            if(ex.getClass().getSimpleName().equals("UnknownHostException")){
                indexingSite.setStatus(Status.FAILED);
                indexingSite.setError("Сайт не существует");
                indexingSite.setName("No name");
            }else {
                indexingSite.setStatus(Status.FAILED);
                indexingSite.setError("Ошибка индексации: главная страница сайта недоступна");
                indexingSite.setName("No name");
            }
            return indexingSite;
        }
        return indexingSite;
    }
    private void add(){
        siteRepository.save(site());
    }
    private void delete(){
        siteRepository.findAll().forEach(site -> {
            if (site.getUrl().equals(url)) {
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
