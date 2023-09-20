package searchengine.service.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.dto.indexing.Status;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IdentifierRepository identifierRepository;
    private final ThreadManager threadManager;

    public void addSite(String url) {
        if (siteRepository.existsByURL(url) && threadManager.areThreadsNotAlive()) {
            deleteSite(url);
        }
        if (!siteRepository.existsByURL(url) && isValidURL(url)) {
            siteRepository.save(fillingSite(url));
        }
    }
    private Site fillingSite(String url) {
        Status status;
        String error;
        String name;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("title");
            error = "";
            status = Status.INDEXING;
            name = elements.text();
        } catch (Exception ex) {
            error = ex.getMessage();
            status = Status.FAILED;
            name = "Failed site";
        }
        return Site.builder()
                .creationTime(LocalDateTime.now())
                .name(name)
                .error(error)
                .status(status)
                .url(url)
                .build();
    }

    private boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Transactional
    private void deleteSite(String url) {
        Site site = siteRepository.findByUrl(url);
        identifierRepository.deleteIndexBySite(site);
        lemmaRepository.deleteLemmaBySite(site);
        pageRepository.deletePageBySite(site);
        siteRepository.delete(site);
    }
}
