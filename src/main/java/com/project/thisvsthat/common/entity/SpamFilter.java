package com.project.thisvsthat.common.entity;

import com.project.thisvsthat.common.enums.FilterType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "spam_filters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpamFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long filterId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private FilterType filterType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String filterValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(insertable = false)
    private LocalDateTime updatedAt;
}
