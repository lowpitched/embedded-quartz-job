package com.xenia.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@SuperBuilder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class JobShardEntity extends BaseEntity {

    private Long jobId;

    private Integer shardIndex;

    private String status;

    private String instanceId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public static enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

}
