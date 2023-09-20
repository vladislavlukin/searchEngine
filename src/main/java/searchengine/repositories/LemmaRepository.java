package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
    @Query("select l from Lemma l where l.lemma=:lemma and l.site=:site")
    List<Lemma> getLemmas (@Param("lemma") String lemma, @Param("site") Site site);

    @Transactional
    @Modifying
    @Query("delete from Lemma l where l.site=:site")
    void deleteLemmaBySite(@Param("site") Site site);
}
