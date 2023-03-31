package searchengine.model.lemma;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.site.Site;


@Setter
@Getter
@Entity
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    private Site site;
    private String lemma;
    private int frequency;
}
