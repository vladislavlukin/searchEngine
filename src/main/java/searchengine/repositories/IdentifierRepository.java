package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Identifier;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;
import java.util.Set;

@Repository
public interface IdentifierRepository extends CrudRepository<Identifier, Integer> {

    @Query("select i.page from Identifier i where i.lemma=:lemma")
    List<Page> findPagesByLemma(@Param("lemma") Lemma lemma);

    @Query("select i.number from Identifier i where i.lemma=:lemma and i.page=:page")
    int countLemmaNameInPage(@Param("lemma") Lemma lemma, @Param("page") Page page);

    @Transactional
    @Modifying
    @Query("delete from Identifier i where i.page in (select p from Page p where p.site = :site)")
    void deleteIndexBySite(@Param("site") Site site);
}


