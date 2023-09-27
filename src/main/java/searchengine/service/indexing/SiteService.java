package searchengine.service.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.HttpStatusException;
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

import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IdentifierRepository identifierRepository;
    private final ThreadManager threadManager;

    public void addSite(String inputUrl) {
        String url = normalizeUrl(inputUrl);

        if (siteRepository.existsByURL(url) && threadManager.areThreadsNotAlive()) {
            deleteSite(url);
        }else {
            siteRepository.save(fillingSite(url));
        }
    }
    private Site fillingSite(String url) {
        Status status = Status.INDEXING;
        String error = "";
        String name = "";
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("title");
            name = elements.text();
        } catch (HttpStatusException e) {
            error = "An error occurred while fetching the site.";
            status = Status.FAILED;
        } catch (UnknownHostException e) {
            error = "Host not found error";
            status = Status.FAILED;
        } catch (ConnectException e) {
            error = "Connection refused error";
            status = Status.FAILED;
        } catch (SocketTimeoutException e) {
            error = "Socket timeout error";
            status = Status.FAILED;
        } catch (Exception e) {
            error = "Other error: " + e.getMessage();
            status = Status.FAILED;
        }
        return Site.builder()
                .creationTime(LocalDateTime.now())
                .name(name)
                .error(error)
                .status(status)
                .url(url)
                .build();
    }

    private String normalizeUrl(String inputUrl) {
        if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
            try {
                URL httpUrl = new URL("http://" + inputUrl);
                HttpURLConnection httpConnection = (HttpURLConnection) httpUrl.openConnection();
                httpConnection.setRequestMethod("HEAD");
                int httpResponseCode = httpConnection.getResponseCode();
                if (httpResponseCode == HttpURLConnection.HTTP_OK) {
                    return httpUrl.toString();
                }
            } catch (IOException ignored) {
            }

            try {
                URL httpsUrl = new URL("https://" + inputUrl);
                HttpURLConnection httpsConnection = (HttpURLConnection) httpsUrl.openConnection();
                httpsConnection.setRequestMethod("HEAD");
                int httpsResponseCode = httpsConnection.getResponseCode();
                if (httpsResponseCode == HttpURLConnection.HTTP_OK) {
                    return httpsUrl.toString();
                }
            } catch (IOException ignored) {
            }

            return inputUrl;
        }
        return inputUrl.endsWith("/") ? inputUrl.substring(0, inputUrl.length() - 1) : inputUrl;
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
