package searchengine.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(indexes = {@Index(columnList = "page_id"), @Index(columnList = "lemma_id")})
public class Identifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne()
    private Page page;
    @ManyToOne()
    private Lemma lemma;
    private int number;
}

