package searchengine.services.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveTask;

public class Indexing extends RecursiveTask<String> {
    private String url;
    private static CopyOnWriteArrayList<String> listCopy = new CopyOnWriteArrayList<>();

    public Indexing(String url) {
        this.url = url.trim();
    }
    @Override
    protected String compute() {
        StringBuffer stringBuffer = new StringBuffer(String.format(url + "\n"));
        List<Indexing> list = new CopyOnWriteArrayList<>();
        try {
            Thread.sleep(150);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get();
            Elements elements = doc.select("a[href]");
            elements.forEach(k -> {
                String attributeUrl = k.absUrl("href");
                if (!attributeUrl.contains("#")
                        && !listCopy.contains(attributeUrl)
                        && !attributeUrl.isEmpty()
                        && attributeUrl.startsWith(url))
                {
                    Indexing work = new Indexing(attributeUrl);
                    work.fork();
                    list.add(work);
                    listCopy.add(attributeUrl);
                }
            });

        }catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        }
        list.sort(Comparator.comparing((Indexing o) -> o.url));
        for (Indexing work : list){
            stringBuffer.append(work.join());
        }

        return stringBuffer.toString();
    }
}

