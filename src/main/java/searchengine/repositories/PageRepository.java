package searchengine.repositories;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.Site;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
   @Transactional
   @Modifying
   @Query("delete from Page p where p.site=:site")
   void deletePageBySite(@Param("site") Site site);
}
