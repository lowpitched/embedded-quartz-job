package com.xenia.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobInstanceShardEntity extends BaseEntity {

    private Long jobId;

    private Integer shardIndex;

    private String status;

    private String instanceId;

    private Integer retryTimes;

    private String errorMessage;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public static enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

}
