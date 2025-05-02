package com.koboolean.metagen.board.domain.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardViewDto {
    private Long id;
    private Long projectId;
    private String createdBy;
    private LocalDateTime created;
    private String title;
    private Integer hitCount;
}
