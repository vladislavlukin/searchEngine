package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.*;
import searchengine.services.LemmaFinder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Search implements SearchService {
    private Map<String, Integer> lemmaMap;
    private Map<Page, Integer> result;
    public String title (String url) throws Exception{
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("title");
        return elements.text();
    }
    public Map<Page, Integer> sorted(Map<Page, Integer> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }

    @Override
    public SearchResponse getSearch(SearchFormat searchFormat, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) throws IOException {
        SearchRelevance searchRelevance = new SearchRelevance(siteRepository, lemmaRepository, indexRepository, searchFormat);
        SearchResponse response = new SearchResponse();
        SearchData data = new SearchData();
        lemmaMap = new HashMap<>();
        if (searchFormat.getQuery().isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstance().getLemmaSet(searchFormat.getQuery()));
        SearchSnippet searchSnippet = new SearchSnippet(pageRepository, lemmaSet);
        for (String lemma : lemmaSet) {
            lemmaRepository.findAll().forEach(l -> {
                if (lemma.equals(l.getLemma())) {
                    lemmaMap.put(lemma, l.getFrequency());
                }
            });
        }
        result = new HashMap<>(searchRelevance.mapFinalPage(lemmaMap));
        int max = searchRelevance.maxRank();
        if(max == 0){
            return response;
        }
        float relevance = 0;
        Page page = new Page();
        for (Map.Entry<Page, Integer> entry : sorted(result).entrySet()) {
            relevance = (float) entry.getValue() / max;
            page = entry.getKey();
        }
        String path = page.getPath();
        String siteName = page.getSite().getName();
        String copySite = page.getSite().getUrl();
        String site = copySite.substring(0, copySite.length() - 1);
        String snippet = searchSnippet.search(path, copySite);
        String title = "";
        try {
            title = title(site + path);
        }catch (Exception ex){
            ex.printStackTrace();
        }


        data.setSite(site);
        data.setRelevance(relevance);
        data.setSiteName(siteName);
        data.setUri(path);
        data.setSnippet(snippet);
        data.setTitle(title);
        response.setResult(true);
        response.setCount(result.size());
        response.setData(data);
        return response;

    }
}
