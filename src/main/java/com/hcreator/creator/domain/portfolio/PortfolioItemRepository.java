package com.hcreator.creator.domain.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    List<PortfolioItem> findAllByOrderBySortOrderAsc();
}