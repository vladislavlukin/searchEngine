package searchengine.service.search;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Page;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.SiteRepository;
import searchengine.service.task.indexing.LemmaFinder;
import searchengine.service.task.search.RelevanceCalculator;
import searchengine.service.task.search.SnippetGenerator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IdentifierRepository identifierRepository;
    @Override
    public SearchResponse getResponse(SearchFormat searchFormat) throws IOException {
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstanceRu().getLemmas(searchFormat.getQuery()));
        List<SearchData> listData = new ArrayList<>();
        RelevanceCalculator relevanceCalculator = new RelevanceCalculator();
        SearchResponse response = new SearchResponse();
        if (searchFormat.getQuery().isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }
        relevanceCalculator.addSites(siteRepository,searchFormat);
        relevanceCalculator.searchRelevance(lemmaSet, lemmaRepository, identifierRepository);
        Map<Page, Integer> relevanceMap = new HashMap<>(relevanceCalculator.getRelevanceMap());
        int max = relevanceCalculator.getMaxRank();
        if(max == 0){
            return response;
        }
        float relevance;
        Page page;
        int stopIndex = 10;
        int i = 0;
        for (Map.Entry<Page, Integer> entry : sorted(relevanceMap).entrySet()) {
            if(i++ == stopIndex){
                break;
            }
            relevance = (float) entry.getValue() / max;
            page = entry.getKey();
            listData.add(getData(page, relevance, lemmaSet, searchFormat));
        }
        response.setResult(true);
        response.setCount(listData.size());
        response.setData(listData);
        return response;

    }
    private String getTitle(String url) throws Exception{
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("title");
        return elements.text();
    }
    private Map<Page, Integer> sorted(Map<Page, Integer> map) {
        return map.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> -e.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
    }
    private SearchData getData(Page page, float relevance, Set<String> lemmaSet, SearchFormat searchFormat){
        SnippetGenerator snippetGenerator = new SnippetGenerator(lemmaSet, searchFormat.getQuery());
        SearchData data = new SearchData();
        String path = page.getPath();
        String siteName = page.getSite().getName();
        String copySite = page.getSite().getUrl();
        String site = copySite.substring(0, copySite.length() - 1);
        String snippet = snippetGenerator.getSnippet(page.getContent());
        String title = "";
        try {
            title = getTitle(site + path);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        data.setSite(site);
        data.setRelevance(relevance);
        data.setSiteName(siteName);
        data.setUri(path);
        data.setSnippet(snippet);
        data.setTitle(title);
        return data;
    }
}
