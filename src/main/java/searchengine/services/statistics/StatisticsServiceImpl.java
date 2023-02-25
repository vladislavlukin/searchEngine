package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    @Override
    public StatisticsResponse getStatistics(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository) {

        TotalStatistics total = new TotalStatistics();
        total.setSites(siteRepository.count());
        total.setLemmas(lemmaRepository.count());
        total.setPages(pageRepository.count());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        siteRepository.findAll().forEach(site -> {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setError(site.getError());
            item.setUrl(site.getUrl());
            item.setStatus(site.getStatus().toString());
            item.setStatusTime(site.getCreationTime());
            AtomicInteger p = new AtomicInteger();
            pageRepository.findAll().forEach(page -> {
                if (page.getSite().equals(site)) {
                    p.getAndIncrement();
                }
            });
            item.setPages(p.get());
            AtomicInteger l = new AtomicInteger();
            lemmaRepository.findAll().forEach(lemma -> {
                if (lemma.getSite().equals(site)) {
                    l.getAndIncrement();
                }
            });
            item.setLemmas(l.get());
            detailed.add(item);

        });

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
