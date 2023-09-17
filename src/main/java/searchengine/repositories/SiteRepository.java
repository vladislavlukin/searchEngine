package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;

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
    @Query("select case when exists (select 1 from Site s where s.status = 'INDEXING') then true else false end")
    boolean isIndexingStatus();

}
