package searchengine.service.indexingService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.site.*;
import searchengine.service.task.indexing.SiteMap;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class IndexingService {
    private final SiteRepository siteRepository;
    private LemmaService lemmaService;
    private PageRepository pageRepository;
    private String url;
    private Status status;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository, LemmaService lemmaService) {
        this.lemmaService = lemmaService;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }
    public IndexingService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    public synchronized void startIndexing(String nameURL) {
        url = nameURL;
        siteRepository.findAll().forEach(site -> {
            try {
                if (site.getUrl().equals(url) && statusIsIndexing(url)) {
                    addPage(site);
                    lemmaService.lemmaIndexing(site);
                    site.setStatus(Status.INDEXED);
                    site.setCreationTime(null);
                    siteRepository.save(site);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    private Set<String> siteMap() {
        String name = new ForkJoinPool().invoke(new SiteMap(url));
        Set<String> map = new TreeSet<>();
        String[] token = name.split("\n");
        for (String s : token) {
            map.add(s.trim());
        }
        return map;
    }

    private Page page(String url, Site site) {
        Page page = new Page();
        int ok = 200;
        int error = 404;
        String uri = url.substring(url.length() - 1);
        page.setCode(ok);
        page.setPath(uri);
        page.setSite(site);
        try {
            Document doc = Jsoup.connect(url).get();
            page.setContent(doc.html());
        } catch (Exception ex) {
            page.setCode(error);
        }
        return page;
    }

    private void addPage(Site site) {
        try {
            for (String url : siteMap()) {
                pageRepository.save(page(url, site));
                sleep(150);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Thread> startThread(Set<String> sites) throws InterruptedException {
        List <Thread> thread = new ArrayList<>();
        for (String nameSite : sites) {
            thread.add(new Thread(() -> {
                startIndexing(nameSite);
            }));
        }
        sleep(1000);
        thread.forEach(Thread::start);
        return thread;
    }
    private Integer stopIndexing(Set<String> sites) {
        AtomicInteger i = new AtomicInteger();
        siteRepository.findAll().forEach(site -> {
            sites.forEach(s -> {
                if (site.getUrl().equals(s) && site.getStatus().equals(Status.INDEXING)) {
                    site.setStatus(Status.INDEXED);
                    site.setError("Индексация остановлена пользователем");
                    site.setCreationTime(null);
                    siteRepository.save(site);
                    i.getAndIncrement();
                }
            });
        });
        return i.get();
    }

    public boolean isIndexing(List<Thread> threads, Set<String> sites) {
        if (stopIndexing(sites) > 0) {
            threads.forEach(Thread::stop);
            return true;
        }
        return false;
    }
    public boolean statusIsIndexing(String nameURL){
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
}
