package searchengine.services.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.site.*;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class StartIndexing {
    private String url;
    private Status status;
    public LemmaIndexing lemmaIndexing;
    public SiteRepository siteRepository;
    public PageRepository pageRepository;

    public StartIndexing(SiteRepository siteRepository, PageRepository pageRepository, LemmaIndexing lemmaIndexing) {
        this.lemmaIndexing = lemmaIndexing;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    public Page page(String path, Site site) {
        Page page = new Page();
        int ok = 200;
        int error = 404;
        String shortPath = path.substring(url.length() - 1);
        page.setCode(ok);
        page.setPath(shortPath);
        page.setSite(site);
        try {
            Document doc = Jsoup.connect(path).get();
            page.setContent(doc.html());
        } catch (Exception ex) {
            page.setCode(error);
        }
        return page;
    }

    public Set<String> listPath() {
        String name = new ForkJoinPool().invoke(new Indexing(url));
        Set<String> listPath = new TreeSet<>();
        String[] token = name.split("\n");
        for (String s : token) {
            listPath.add(s.trim());
        }
        return listPath;
    }
    public boolean statusIndexing(String nameURL){
        siteRepository.findAll().forEach(site -> {
            if(site.getUrl().equals(nameURL)){
                status = site.getStatus();
            }
        });
        if (status.equals(Status.INDEXING)){
            return true;
        }
        return false;
    }

    public void addPage(Site site) {
        try {
            for (String path : listPath()) {
                pageRepository.save(page(path, site));
                sleep(150);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized void startIndexing(String nameURL) {
        url = nameURL;
        siteRepository.findAll().forEach(site -> {
            if (site.getUrl().equals(url) && statusIndexing(url)){
                addPage(site);
                try {
                    lemmaIndexing.startIndexingLemma(site);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                site.setStatus(Status.INDEXED);
                site.setCreationTime(null);
                siteRepository.save(site);
            }
        });
    }
}
