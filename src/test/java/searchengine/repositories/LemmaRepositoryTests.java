package searchengine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Lemma;
import searchengine.model.Site;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/site.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LemmaRepositoryTests {
    private final String LEMMA = "Hours";
    private Site site;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private TestEntityManager testEntityManager;


    @BeforeEach
    void setUp() {
        site = siteRepository.findAll().iterator().next();
        this.testEntityManager.persistAndFlush(Lemma.builder().site(site).lemma(LEMMA).build());
    }


    @Test
    public void testGetLemmas() {
        List<Lemma> lemmas = lemmaRepository.getLemmas(LEMMA, site);
        assertThat(lemmas.stream().anyMatch(lemma -> lemma.getLemma().equals(LEMMA))).isTrue();
    };

    @Test
    public void testDeleteLemmaBySite(){
        lemmaRepository.deleteLemmaBySite(site);
        assertThat(lemmaRepository.count()).isEqualTo(0);
    };
}
