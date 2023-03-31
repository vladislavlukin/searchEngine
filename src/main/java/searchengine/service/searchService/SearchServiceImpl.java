package searchengine.service.searchService;

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
import searchengine.service.task.indexing.LemmaFinder;
import searchengine.service.task.search.Relevance;
import searchengine.service.task.search.Snippet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private Map<String, Integer> lemmaMap;
    private Map<Page, Integer> relevanceMap;
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
        Relevance searchRelevance = new Relevance();
        SearchResponse response = new SearchResponse();
        SearchData data = new SearchData();
        lemmaMap = new HashMap<>();
        if (searchFormat.getQuery().isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstance().getLemmaSet(searchFormat.getQuery()));
        Snippet searchSnippet = new Snippet(lemmaSet);
        for (String lemma : lemmaSet) {
            lemmaRepository.findAll().forEach(l -> {
                if (lemma.equals(l.getLemma())) {
                    lemmaMap.put(lemma, l.getFrequency());
                }
            });
        }
        searchRelevance.addUrl(siteRepository,searchFormat);
        searchRelevance.mapFinalPage(lemmaMap, lemmaRepository, indexRepository);
        relevanceMap = new HashMap<>(searchRelevance.getRelevanceMap());
        int max = searchRelevance.getMax();
        if(max == 0){
            return response;
        }
        float relevance = 0;
        Page page = new Page();
        for (Map.Entry<Page, Integer> entry : sorted(relevanceMap).entrySet()) {
            relevance = (float) entry.getValue() / max;
            page = entry.getKey();
        }
        String path = page.getPath();
        String siteName = page.getSite().getName();
        String copySite = page.getSite().getUrl();
        String site = copySite.substring(0, copySite.length() - 1);
        searchSnippet.search(path, copySite, pageRepository);
        String snippet = searchSnippet.getStringSnippet().toString();
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
        response.setCount(relevanceMap.size());
        response.setData(data);
        return response;

    }
}
