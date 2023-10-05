package searchengine.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = {@Index(columnList = "path"), @Index(columnList = "site_id")})
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne()
    private Site site;
    private String path;
    private String title;
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

}
