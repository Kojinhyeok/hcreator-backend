package com.hcreator.creator.domain.inquiry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    @Column(length = 30)
    private String inquiryService;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String attachmentPath;

    @Column(length = 20)
    @Builder.Default
    private String status = "대기"; // 대기 / 연락 완료

    @JsonIgnore
    private String password; // 해시 저장, 목록 응답엔 노출 안 함

    private LocalDateTime createdAt;

    /** 기존 데이터(status 비어 있음)도 "대기"로 보이게 한다 */
    public String getStatus() {
        return status == null || status.isBlank() ? "대기" : status;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}