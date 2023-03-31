package searchengine.model.lemma;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.site.Page;

@Getter
@Setter
@Entity
public class Identifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    private Page page;
    @ManyToOne
    private Lemma lemma;
    private int number;
}

