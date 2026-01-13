package com.xenia.core.po;

import liquibase.change.Change;
import lombok.*;
import org.quartz.Job;

import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
@ToString
public class JobEntity {

    private String name;

    private String group;
    @Setter
    private Class clazz;
    @Setter
    private String cron;
    @Setter
    private Status status;
    @Setter
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
