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

    @JsonIgnore
    private String password; // 해시 저장, 목록 응답엔 노출 안 함

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}