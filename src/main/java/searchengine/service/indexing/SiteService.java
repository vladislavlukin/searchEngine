package searchengine.service.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.model.site.SiteRepository;
import searchengine.model.site.Status;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SiteService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private Site site;
    public SiteService(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }
    private Site site (String url) {
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
    public void addSite(String url, List<Thread> thread){
        Set<String> sites = new HashSet<>();
        siteRepository.findAll().forEach(site -> {
            sites.add(site.getUrl());
        });
        if (sites.contains(url) && threadIsNotLive(url, thread)) {
            deleteSite(url);
            sites.remove(url);
        }
        if (!sites.contains(url) && threadIsNotLive(url, thread) ) {
            siteRepository.save(site(url));
            sites.add(url);
        }
    }
    private void deleteSite(String url){
        siteRepository.findAll().forEach(s -> {
            if(s.getUrl().equals(url)) {
                site = s;
            }
        });
        pageRepository.findAll().forEach(p -> {
            if(p.getSite().equals(site)){
                pageRepository.deleteIndexByPage(p);
            }
        });
        siteRepository.deleteLemmaBySite(site);
        siteRepository.deletePageBySite(site);
        siteRepository.delete(site);
    }
    private boolean threadIsNotLive(String url, List<Thread> thread){
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
}
