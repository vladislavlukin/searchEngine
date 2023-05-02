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
    public SiteService(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
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
            Site site = fillingSite(url);
            siteRepository.save(site);
            sites.add(url);
        }
    }
    private Site fillingSite(String url) {
        Site site = new Site();
        site.setUrl(url);
        site.setStatus(Status.INDEXING);
        site.setError("");
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("title");
            site.setName(elements.text());
        } catch (Exception ex) {
            if(ex.getClass().getSimpleName().equals("UnknownHostException")){
                site.setStatus(Status.FAILED);
                site.setError("Сайт не существует");
                site.setName("No name");
            }else {
                site.setStatus(Status.FAILED);
                site.setError("Ошибка индексации: главная страница сайта недоступна");
                site.setName("No name");
            }
        }
        return site;
    }
    private void deleteSite(String url){
        siteRepository.findAll().forEach(s -> {
            if(s.getUrl().equals(url)) {
                pageRepository.findAll().forEach(p -> {
                    if(p.getSite().equals(s)){
                        pageRepository.deleteIndexByPage(p);
                    }
                });
                siteRepository.deleteLemmaBySite(s);
                siteRepository.deletePageBySite(s);
                siteRepository.delete(s);
            }
        });
    }
    private boolean threadIsNotLive(String url, List<Thread> thread){
        if (!url.isEmpty()) {
            if (thread == null) {
                return true;
            } else {
                int countThreadLive = 0;
                for (Thread newThread : thread) {
                    if (newThread.isAlive()) {
                        countThreadLive++;
                    }
                }
                return countThreadLive == 0;
            }
        }
        return false;
    }
}
