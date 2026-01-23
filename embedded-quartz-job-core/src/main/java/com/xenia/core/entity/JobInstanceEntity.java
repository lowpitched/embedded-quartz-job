package com.xenia.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobInstanceEntity extends BaseEntity {

    private Long jobId;
    private String instanceId;
    private String status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public static enum Status {
        RUNNING,
        COMPLETED,
        FAILED;
    }

}
