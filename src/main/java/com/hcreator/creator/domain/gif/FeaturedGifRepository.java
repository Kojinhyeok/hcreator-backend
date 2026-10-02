package com.hcreator.creator.domain.gif;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeaturedGifRepository extends JpaRepository<FeaturedGif, Long> {
    List<FeaturedGif> findAllByOrderBySortOrderAsc();
}