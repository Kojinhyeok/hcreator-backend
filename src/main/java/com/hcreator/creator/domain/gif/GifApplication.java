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

    private String clinicalAmount; // 임상시험 금액 (자유 입력)

    @Column(length = 1000)
    private String nasPath; // NAS 경로 (링크 텍스트)

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
    private String memo; // (구) 내부 메모 - 이제는 댓글(ApplyComment)을 사용, 기존 데이터 보존용

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<GifApplicationRow> rows = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}