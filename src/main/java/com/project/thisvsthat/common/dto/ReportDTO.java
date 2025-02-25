package com.project.thisvsthat.common.dto;

import com.project.thisvsthat.common.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long reportId;
    private Long postId;
    private Long reporterId;
    private LocalDateTime createdAt;

    public static ReportDTO fromEntity(Report report) {
        return new ReportDTO(
                report.getReportId(),
                report.getPost().getPostId(),
                report.getUser().getUserId(),
                report.getCreatedAt()
        );
    }
}