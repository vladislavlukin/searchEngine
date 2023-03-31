package searchengine.model.site;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {
    @Transactional
    @Modifying
    @Query("delete from Page p where p.site=:site")
    void deletePageBySite(@Param("site") Site site);
    @Transactional
    @Modifying
    @Query("delete from Lemma l where l.site=:site")
    void deleteLemmaBySite(@Param("site") Site site);
}
