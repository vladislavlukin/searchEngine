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
import searchengine.service.task.indexing.LemmaFinder;
import searchengine.service.task.search.RelevanceCalculator;
import searchengine.service.task.search.SnippetGenerator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final RelevanceCalculator relevanceCalculator;
    @Override
    public SearchResponse getResponse(SearchFormat searchFormat) throws IOException {
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstance().getLemmas(searchFormat.getQuery()));
        SearchResponse response = new SearchResponse();
        if (searchFormat.getQuery().isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }

        List<SearchData> listData = relevanceCalculator.searchRelevance(lemmaSet, searchFormat.getSite())
                .entrySet()
                .stream()
                .map(entry -> getData(entry.getKey(), entry.getValue(), lemmaSet, searchFormat))
                .collect(Collectors.toList());

        response.setResult(true);
        response.setCount(listData.size());
        response.setLimit(searchFormat.getLimit());
        response.setOffset(searchFormat.getOffset());
        response.setData(listData);
        return response;

    }
    private String getTitle(String url) throws Exception{
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select("title");
        return elements.text();
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
