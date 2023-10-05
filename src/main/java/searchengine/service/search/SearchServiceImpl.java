package searchengine.service.search;

import lombok.RequiredArgsConstructor;
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
    private final SnippetGenerator snippetGenerator;
    @Override
    public SearchResponse getResponse(SearchFormat searchFormat) throws IOException {
        Set<String> lemmaSet = new HashSet<>(LemmaFinder.getInstance().getLemmas(searchFormat.getQuery()));

        if (searchFormat.getQuery().isEmpty()) {
            return SearchResponse.builder()
                    .error("Empty search query provided")
                    .result(false)
                    .build();
        }

        List<SearchData> listData = relevanceCalculator.searchRelevance(lemmaSet, searchFormat.getSite())
                .entrySet()
                .stream()
                .map(entry -> createSearchDataFromPage(entry.getKey(), entry.getValue(), lemmaSet))
                .collect(Collectors.toList());

        int resultsFound = listData.size();

        if (listData.isEmpty()){
            return SearchResponse.builder()
                    .error("No results found")
                    .result(false).build();
        }

        int limit = searchFormat.getLimit();
        int offset = searchFormat.getOffset();

        listData = listData.stream().skip(offset).limit(limit).collect(Collectors.toList());

        return SearchResponse.builder()
                .result(true)
                .data(listData)
                .count(resultsFound)
                .build();

    }
    private SearchData createSearchDataFromPage(Page page, float relevance, Set<String> lemmaSet){
        String path = page.getPath();
        String siteName = page.getSite().getName();
        String site = page.getSite().getUrl();
        String snippet = snippetGenerator.generateSnippet(page.getContent(), lemmaSet);
        String title = page.getTitle();

        return SearchData.builder()
                .uri(path)
                .relevance(relevance)
                .site(site)
                .siteName(siteName)
                .snippet(snippet)
                .title(title)
                .build();
    }
}
