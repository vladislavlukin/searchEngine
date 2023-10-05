package searchengine.service.task.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

@RequiredArgsConstructor
public class SiteScanner extends RecursiveTask<CopyOnWriteArrayList<Page>> {
    private static final CopyOnWriteArrayList<String> listCopy = new CopyOnWriteArrayList<>();

    private final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    private final String REFERRER = "http://www.google.com";
    private final String url;
    private final Site site;

    @Override
    public CopyOnWriteArrayList<Page> compute() {
        CopyOnWriteArrayList<Page> pages = new CopyOnWriteArrayList<>();
        List<SiteScanner> list = new CopyOnWriteArrayList<>();

        try {
            Thread.sleep(250);
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .get();

            Elements elements = doc.select("a[href]");

            if (isSinglePageWithURL(elements)){
                Page mainPage = fillingPage(url, doc.title());
                pages.add(mainPage);
                return pages;
            }

            elements.forEach(k -> {
                String attributeUrl = k.absUrl("href");
                if (isValidAttributeUrl(attributeUrl)) {
                    SiteScanner work = new SiteScanner(attributeUrl, site);
                    work.fork();
                    list.add(work);
                    listCopy.add(attributeUrl);
                    Page page = fillingPage(attributeUrl, doc.title());
                    if (!pages.contains(page) && page != null) {
                        pages.add(page);
                    }
                }
            });

        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
        for (SiteScanner work : list) {
            pages.addAll(work.join());
        }

        return pages;
    }

    private Page fillingPage(String urlSite, String title) {
        String content = "no content";
        int statusCode = 200;
        String uri = (urlSite).contains(site.getUrl()) ? urlSite.substring(site.getUrl().length()) : urlSite;
        try {
            Thread.sleep(250);
            Connection.Response response = Jsoup.connect(urlSite)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .execute();
            statusCode = response.statusCode();
            content = response.parse().html();

        } catch (HttpStatusException e) {
            statusCode = e.getStatusCode();
        } catch (UnsupportedMimeTypeException e) {
            return null;
        } catch (Exception ignored) {
        }
        return Page.builder()
                .content(content)
                .title(title)
                .path(uri)
                .site(site)
                .code(statusCode)
                .build();
    }
    private boolean isSinglePageWithURL(Elements elements){
        return elements
                .stream()
                .noneMatch(element -> element.absUrl("href")
                        .startsWith(url)
                );
    }
    private boolean isValidAttributeUrl(String attributeUrl) {
        return !attributeUrl.contains("#")
                && !listCopy.contains(attributeUrl)
                && !attributeUrl.isEmpty()
                && isUrlContains(attributeUrl);
    }

    private boolean isUrlContains(String attributeUrl) {
        String cleanedAttributeUrl = getUrl(attributeUrl);
        String cleanedMainUrl = getUrl(url);

        return cleanedMainUrl.contains(cleanedAttributeUrl);
    }

    private String getUrl(String url) {
        String[] token = url.replaceAll("www.", "").split("\\.");
        return token[0].replaceAll("[^a-zA-Z0-9:/._-]", "").toLowerCase();
    }
}