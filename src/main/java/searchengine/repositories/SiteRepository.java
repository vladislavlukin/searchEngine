package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    @Query("select case when exists (select 1 from Site s where s.status = 'INDEXING') then true else false end")
    boolean isIndexingStatus();

    @Query("select case when exists (select 1 from Site s where s.url =:url) then true else false end")
    boolean existsByURL(@Param("url") String url);

    @Query("select s from Site s where s.url =:url")
    Site findByUrl(@Param("url") String url);

}
