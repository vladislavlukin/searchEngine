package searchengine.services.indexing;

import searchengine.model.site.SiteRepository;
import searchengine.model.site.Status;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StopIndexing {
    public static Integer stop(Set<String> sites, SiteRepository siteRepository){
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
}
