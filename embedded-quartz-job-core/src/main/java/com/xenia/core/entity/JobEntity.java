package com.xenia.core.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@SuperBuilder
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@Setter
@NoArgsConstructor
public class JobEntity extends BaseEntity{

    @Column("name")
    private String name;

    @Column("group_name")
    private String groupName;

    @Column("clazz")
    private String clazz;

    @Column("cron")
    private String cron;

    @Enum
    @Column("status")
    private Status status;

    @Column("description")
    private String description;

    @Column("current_instance")
    private String currentInstance;

    private LocalDateTime fireTime;

    @Column("total_shards")
    private Integer totalShards;

    @Column("params")
    private Map<String, Object> params;

    private Long expireSeconds;

    public enum Status {
        NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED;

        public static Status fromString(String value) {
            for (Status status : Status.values()) {
                if (status.name().equalsIgnoreCase(value)) {
                    return status;
                }
            }
            return null;
        }
    }

}
