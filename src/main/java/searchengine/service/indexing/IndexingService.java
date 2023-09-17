package searchengine.service.indexing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.Status;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.service.task.indexing.indexing.SiteMapTask;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static java.lang.Thread.sleep;
@Getter
@Setter
@RequiredArgsConstructor
@Service
public class IndexingService {
    private final SiteRepository siteRepository;
    private final LemmaService lemmaService;
    private final PageRepository pageRepository;
    private final ThreadManager threadManager;
    private Integer countStatusIndexing;
    private Set<String> setUrlInSite;

    public void startIndexing() {
        if(!siteRepository.isIndexingStatus()) {
            throw new IllegalArgumentException("Добавтье не менее одного сайта или обновите текущий");
        }
        if(threadManager.getThreads() != null && threadManager.getThreads().stream().anyMatch(Thread::isAlive)){
            throw new IllegalArgumentException("Индексация уже запущена");
        }
        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                Thread thread = new Thread(() -> {
                    indexingSite(site);
                });
                threadManager.addThread(thread);
            }
        });
        threadManager.startAllThreads();
    }

    public void stopIndexing() {
        if(siteRepository.isIndexingStatus()){
            throw new IllegalArgumentException("Индексация не запущена");
        }
        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                site.setStatus(Status.INDEXED);
                site.setError("Индексация остановлена пользователем");
                site.setCreationTime(LocalDateTime.now());
                siteRepository.save(site);
            }

        });
        threadManager.stopAllThreads();
    }
    private synchronized void indexingSite(Site site) {
        addPages(site);
        lemmaService.startLemmaIndexing(site);
        site.setStatus(Status.INDEXED);
        site.setCreationTime(LocalDateTime.now());
        siteRepository.save(site);

    }
    private void addPages(Site site) {
        searchUrlsInSite(site.getUrl());
        try {
            for (String url : getSetUrlInSite()) {
                Page page = fillingPage(url, site);
                pageRepository.save(page);
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

    private Page fillingPage(String urlSite, Site site) {
        Page page = new Page();
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
        return page;
    }
}
