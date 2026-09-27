package com.hcreator.creator.domain.apply;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceApplicationRepository extends JpaRepository<ServiceApplication, Long> {
    List<ServiceApplication> findAllByOrderByCreatedAtDesc();
}