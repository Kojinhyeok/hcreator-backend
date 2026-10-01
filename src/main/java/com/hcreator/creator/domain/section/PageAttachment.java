package com.hcreator.creator.domain.section;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String pageType; // "detail-page" 등, 페이지당 1개만 존재

    private String label;    // 버튼에 표시할 텍스트 (예: "기획안 다운로드")

    private String filePath; // S3 URL

    private String fileName; // 원본 파일명 (참고용)
}