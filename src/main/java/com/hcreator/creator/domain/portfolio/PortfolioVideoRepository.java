package com.hcreator.creator.domain.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioVideoRepository extends JpaRepository<PortfolioVideo, Long> {
    List<PortfolioVideo> findAllByOrderBySortOrderAsc();
}