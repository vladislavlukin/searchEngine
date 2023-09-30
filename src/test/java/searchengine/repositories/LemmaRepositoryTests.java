package searchengine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Lemma;
import searchengine.model.Site;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/sql_script/lemmaTest.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LemmaRepositoryTests {
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    private Site site;
    private Set<String> lemmasName;


    @BeforeEach
    void setUp() {
        site = siteRepository.findAll().iterator().next();

        lemmasName = new HashSet<>();
        lemmasName.add("home");
        lemmasName.add("house");
        lemmasName.add("cat");
    }

    @Test
    public void testFindLemmasByLemmaNames() {
        List<Site> sites = siteRepository.getAllSites();

        List<Lemma> lemmas = lemmaRepository.findLemmasByLemmaNames(lemmasName, sites);

        assertThat(lemmas.size()).isEqualTo(4);
        assertThat(lemmas.stream().anyMatch(lemma -> lemma.getLemma().equals("home"))).isTrue();
        assertThat(lemmas.stream().anyMatch(lemma -> lemma.getLemma().equals("house"))).isTrue();
    };

    @Test
    public void testFindLemmasByLemmaNamesAndSite() {
        List<Site> sites = new ArrayList<>();
        sites.add(site);

        List<Lemma> lemmas = lemmaRepository.findLemmasByLemmaNames(lemmasName, sites);

        assertThat(lemmas.size()).isEqualTo(2);
        assertThat(lemmas.stream().anyMatch(lemma -> lemma.getLemma().equals("home"))).isTrue();
        assertThat(lemmas.stream().anyMatch(lemma -> lemma.getLemma().equals("house"))).isTrue();
        assertThat(lemmas.stream().anyMatch(lemma -> lemma.getSite().equals(site))).isTrue();
    };

    @Test
    public void testDeleteLemmaBySite(){
        lemmaRepository.deleteLemmaBySite(site);

        assertThat(lemmaRepository.count()).isEqualTo(3);

        Site site2 = lemmaRepository.findAll().iterator().next().getSite();

        assertThat(site2).isNotEqualTo(site);

        lemmaRepository.deleteLemmaBySite(site2);

        assertThat(lemmaRepository.count()).isEqualTo(0);
    };
}
