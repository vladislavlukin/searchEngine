package searchengine.model.site;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    @Transactional
    @Modifying
    @Query("delete from Identifier i where i.page=:page")
    void deleteIndexByPage(@Param("page") Page page);
}
