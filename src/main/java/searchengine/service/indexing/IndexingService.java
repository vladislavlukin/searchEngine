package searchengine.service.indexing;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.site.*;
import searchengine.service.task.indexing.SiteMapTask;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static java.lang.Thread.sleep;
@Getter
@Setter
public class IndexingService {
    private final SiteRepository siteRepository;
    private LemmaService lemmaService;
    private PageRepository pageRepository;
    private Integer countStatusIndexing;
    private List <Thread> threads;
    private Set<String> setUrlInSite;
    private Page page;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository, LemmaService lemmaService) {
        this.lemmaService = lemmaService;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }
    public IndexingService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }
    public void startIndexing() throws InterruptedException {
        threads = new ArrayList<>();
        siteRepository.findAll().forEach(site -> {
            if(site.getStatus().equals(Status.INDEXING)){
                threads.add(new Thread(() -> {
                    indexingSite(site);
                }));
            }
        });
        threads.forEach(Thread::start);
    }
    public boolean isIndexing() {
        searchCountStatusIndexing();
        return getCountStatusIndexing() > 0;
    }

    public void stopIndexing(List<Thread> threads) {
        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                site.setStatus(Status.INDEXED);
                site.setError("Индексация остановлена пользователем");
                site.setCreationTime(null);
                siteRepository.save(site);
            }

        });
        threads.forEach(Thread::stop);
    }
    private synchronized void indexingSite(Site site) {
        addPages(site);
        lemmaService.startLemmaIndexing(site);
        site.setStatus(Status.INDEXED);
        site.setCreationTime(null);
        siteRepository.save(site);

    }
    private void addPages(Site site) {
        searchUrlsInSite(site.getUrl());
        try {
            for (String url : getSetUrlInSite()) {
                fillingPage(url, site);
                pageRepository.save(getPage());
                sleep(150);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private void searchUrlsInSite(String url) {
        String name = new ForkJoinPool().invoke(new SiteMapTask(url));
        setUrlInSite = new TreeSet<>();
        String[] token = name.split("\n");
        for (String s : token) {
            setUrlInSite.add(s.trim());
        }
    }

    private void fillingPage(String urlSite, Site site) {
        page = new Page();
        int ok = 200;
        int error = 404;
        String uri = urlSite.substring(site.getUrl().length() - 1);
        page.setCode(ok);
        page.setPath(uri);
        page.setSite(site);
        try {
            Document doc = Jsoup.connect(urlSite).get();
            page.setContent(doc.html());
        } catch (Exception ex) {
            page.setCode(error);
        }
    }

    private void searchCountStatusIndexing() {
        countStatusIndexing = 0;
        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                countStatusIndexing++;
            }
        });
    }
}
