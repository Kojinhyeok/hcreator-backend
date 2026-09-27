package com.hcreator.creator.domain.gif;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GifApplicationRepository extends JpaRepository<GifApplication, Long> {

    List<GifApplication> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT g FROM GifApplication g LEFT JOIN FETCH g.rows ORDER BY g.createdAt DESC")
    List<GifApplication> findAllWithRows();
}