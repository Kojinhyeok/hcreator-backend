package com.hcreator.creator.domain.section;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceDetailSectionRepository extends JpaRepository<ServiceDetailSection, Long> {
    List<ServiceDetailSection> findByPageTypeOrderBySortOrderAsc(String pageType);
}