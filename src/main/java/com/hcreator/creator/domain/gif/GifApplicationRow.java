package com.hcreator.creator.domain.gif;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GifApplicationRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    @JsonIgnore
    private GifApplication application;

    private String reportNo;
    private String testItem;
    private String templateNo;
    private String subjectNo;
}