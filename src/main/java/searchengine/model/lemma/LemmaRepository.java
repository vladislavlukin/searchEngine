package searchengine.model.lemma;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.site.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    @Transactional(readOnly = true)
    @Modifying
    @Query("select l from Lemma l where l.lemma=:lemma and l.site=:site")
    List<Lemma> getLemmas (@Param("lemma") String lemma, @Param("site") Site site);
}
