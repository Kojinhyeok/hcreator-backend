package com.hcreator.creator.repository.gif;

import com.hcreator.creator.domain.gif.GifTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GifTemplateRepository extends JpaRepository<GifTemplate, Long> {
    List<GifTemplate> findAllByOrderBySortOrderAsc();
    List<GifTemplate> findByFeaturedTrueOrderBySortOrderAsc();
    List<GifTemplate> findByFeaturedFalseOrderBySortOrderAsc();
}