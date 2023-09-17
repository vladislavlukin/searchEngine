package searchengine.service.statistic;

import searchengine.dto.statistic.StatisticsResponse;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

public interface StatisticsService {
    StatisticsResponse getStatistics();
}
