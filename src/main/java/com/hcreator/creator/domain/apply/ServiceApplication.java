package com.hcreator.creator.domain.apply;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String serviceType; // detail-page / blog / video

    private String companyName;
    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private String productName;
    private String brandName;
    private String launchDate;
    private String needsShooting;
    private String hasPlan;

    private String attachmentPath;

    @Column(columnDefinition = "TEXT")
    private String promoContent;

    private String videoFormat;
    private String videoContent;

    @Column(columnDefinition = "TEXT")
    private String etcNote;

    @Column(columnDefinition = "TEXT")
    private String requestNote;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}