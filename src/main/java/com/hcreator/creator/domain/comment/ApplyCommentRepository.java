package com.hcreator.creator.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplyCommentRepository extends JpaRepository<ApplyComment, Long> {

    List<ApplyComment> findByTargetTypeAndTargetIdOrderByCreatedAtAscIdAsc(String targetType, Long targetId);

    List<ApplyComment> findByTargetTypeOrderByCreatedAtAscIdAsc(String targetType);

    void deleteByTargetTypeAndTargetId(String targetType, Long targetId);
}