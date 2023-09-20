package searchengine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Identifier;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/site.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class IdentifierRepositoryTests {
    private Site site;
    private Lemma lemma;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private IdentifierRepository identifierRepository;

    @Autowired
    private TestEntityManager testEntityManager;


    @BeforeEach
    void setUp() {
        site = siteRepository.findAll().iterator().next();
        lemma = Lemma.builder().site(site).build();
        Page page = Page.builder().site(site).build();
        this.testEntityManager.persistAndFlush(lemma);
        this.testEntityManager.persistAndFlush(page);
        this.testEntityManager.persistAndFlush(Identifier.builder().lemma(lemma).page(page).build());
    }

    @Test
    public void testGetIndexes() {
        List<Identifier> identifiers = identifierRepository.getIndexes(lemma);
        assertThat(identifiers.stream().anyMatch(i -> i.getLemma().equals(lemma))).isTrue();
    };

    @Test
    public void testDeleteIndexBySite() {
        identifierRepository.deleteIndexBySite(site);
        assertThat(identifierRepository.count()).isEqualTo(0);
    };
}
