package searchengine.services.statistics;

import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.SiteRepository;

public interface StatisticsService {
    StatisticsResponse getStatistics(SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository);
}
