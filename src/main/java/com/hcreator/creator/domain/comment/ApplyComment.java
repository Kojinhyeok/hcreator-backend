package com.hcreator.creator.domain.comment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** GIF 신청 / 서비스 문의에 직원이 남기는 댓글형 메모 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(indexes = @Index(name = "idx_apply_comment_target", columnList = "targetType, targetId"))
public class ApplyComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String targetType; // gif / inquiry

    @Column(nullable = false)
    private Long targetId;

    private Long authorId;

    @Column(length = 50)
    private String authorName; // 작성 당시 이름

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}