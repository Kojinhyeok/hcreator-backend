package com.hcreator.creator.domain.gif;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GifApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private String pointColor;
    private String bgColor;
    private String bgEffect;
    private String quantity;
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String etcNote;

    @Column(length = 20)
    @Builder.Default
    private String status = "대기중"; // 대기중 / 작업중 / 전달완료

    @Column(columnDefinition = "TEXT")
    private String memo; // 내부 직원용 코멘트

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GifApplicationRow> rows = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}