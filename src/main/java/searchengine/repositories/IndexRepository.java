package searchengine.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Identifier;
import searchengine.model.Lemma;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Identifier, Integer> {
    @Transactional(readOnly = true)
    @Modifying
    @Query("select i from Identifier i where i.lemma=:lemma")
    List<Identifier> getIndexes (@Param("lemma") Lemma lemma);
}


