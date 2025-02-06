package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.SpamFilter;
import com.project.thisvsthat.common.enums.FilterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpamFilterDTO {
    private Long filterId;
    private FilterType filterType;
    private String filterValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SpamFilterDTO fromEntity(SpamFilter spamFilter) {
        return new SpamFilterDTO(
                spamFilter.getFilterId(),
                spamFilter.getFilterType(),
                spamFilter.getFilterValue(),
                spamFilter.getCreatedAt(),
                spamFilter.getUpdatedAt()
        );
    }
}