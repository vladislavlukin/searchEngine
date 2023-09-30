package searchengine.service.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.service.task.search.RelevanceCalculator;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Sql(scripts = {"/sql_script/indexedSite.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RelevanceCalculatorTest {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private IdentifierRepository identifierRepository;

    private RelevanceCalculator relevanceCalculator;

    private Site site;
    private Page page;
    private Set<String> lemmaSet;


    @BeforeEach
    void setUp() {
        relevanceCalculator = new RelevanceCalculator(siteRepository, lemmaRepository, identifierRepository);

        site = siteRepository.findAll().iterator().next();
        page = pageRepository.findAll().iterator().next();

        lemmaSet = new HashSet<>();
        lemmaSet.add("house");
        lemmaSet.add("home");
        lemmaSet.add("window");
    }


    @Test
    public void testSearchRelevanceIfNoneSite() {
        Map<Page, Float> result = relevanceCalculator.searchRelevance(lemmaSet, null);

        assertEquals(1.0f, result.get(page));

    }

    @Test
    public void testSearchRelevanceWithSite() {
        Map<Page, Float> result = relevanceCalculator.searchRelevance(lemmaSet, site.getUrl());

        assertEquals(1.0f, result.get(page));

    }
}

