package searchengine.service.statisticService;

import searchengine.dto.statistic.StatisticsResponse;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.SiteRepository;

public interface StatisticsService {
    StatisticsResponse getStatistics(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository);
}
