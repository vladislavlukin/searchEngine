package searchengine.services.indexing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.site.Site;
import searchengine.model.site.SiteRepository;
import searchengine.model.site.Status;

import java.net.UnknownHostException;

public class AddSite {
    private SiteRepository siteRepository;
    private String url;
    public AddSite(SiteRepository siteRepository, String url) {
        this.siteRepository = siteRepository;
        this.url = url;
    }

    public Site site () {
        Site indexingSite = new Site();
        indexingSite.setUrl(url);
        indexingSite.setStatus(Status.INDEXING);
        indexingSite.setError("");
        try {
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.select("title");
            indexingSite.setName(elements.text());
        } catch (Exception ex) {
            if(ex.getClass().getSimpleName().equals("UnknownHostException")){
                indexingSite.setStatus(Status.FAILED);
                indexingSite.setError("Сайт не существует");
                indexingSite.setName("No name");
            }else {
                indexingSite.setStatus(Status.FAILED);
                indexingSite.setError("Ошибка индексации: главная страница сайта недоступна");
                indexingSite.setName("No name");
            }
            return indexingSite;
        }
        return indexingSite;
    }
    public void add(){
        siteRepository.save(site());
    }
}
