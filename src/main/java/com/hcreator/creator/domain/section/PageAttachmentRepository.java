package com.hcreator.creator.domain.section;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PageAttachmentRepository extends JpaRepository<PageAttachment, Long> {
    Optional<PageAttachment> findByPageType(String pageType);
}