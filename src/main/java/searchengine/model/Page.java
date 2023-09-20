package searchengine.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne()
    private Site site;
    private String path;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

}
