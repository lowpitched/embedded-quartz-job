--liquibase formatted sql

--changeset system:1-distributed-task-tables
--comment: Create Distributed Task Management Tables

CREATE TABLE job (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    clazz VARCHAR(500) NOT NULL,
    cron VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'NORMAL',
    description TEXT,
    current_instance VARCHAR(255) default "0",
    total_shards INTEGER DEFAULT 1,
    params JSONB,
    created_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_job_group_name ON job(group_name);
CREATE INDEX idx_job_status ON job(status);
CREATE INDEX idx_job_current_instance ON job(current_instance);

--changeset system:create-job_shard
CREATE TABLE job_shard (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    instance_id VARCHAR(255) NOT NULL,
    shard_index INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'WAITING',
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    created_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT job_shard_unique_index UNIQUE (job_id, instance_id, shard_index)
)
