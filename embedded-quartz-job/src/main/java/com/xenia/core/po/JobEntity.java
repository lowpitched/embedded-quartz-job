package com.xenia.core.po;

import lombok.*;
import org.quartz.Job;

import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@Setter
public class JobEntity extends BaseEntity{

    private String name;

    private String group;

    private Class<? extends Job> clazz;

    private String cron;

    private Status status;

    private String description;

    private String currentInstance;

    private Integer totalShards;

    private Map<String, Object> params;

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
