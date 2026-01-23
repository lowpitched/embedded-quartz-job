--liquibase formatted sql

--changeset system:create-job
CREATE TABLE ${tablePrefix}job (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    group_name VARCHAR(255) NOT NULL,
    clazz VARCHAR(500) NOT NULL,
    cron VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'NORMAL',
    description TEXT,
    current_instance VARCHAR(255) default '0',
    fire_time TIMESTAMP WITH TIME ZONE,
    expire_seconds INTEGER DEFAULT 86400,
    total_shards INTEGER DEFAULT 1,
    params JSONB,
    created_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ${tablePrefix}job_unique_index UNIQUE (name, group_name)
);

--changeset system:create-job_instance_shard
CREATE TABLE ${tablePrefix}job_instance_shard (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    instance_id VARCHAR(255) NOT NULL,
    shard_index INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'WAITING',
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    error_message varchar(1000),
    retry_times INTEGER DEFAULT 0,
    created_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ${tablePrefix}job_instance_shard_unique_index UNIQUE (job_id, instance_id, shard_index)
)

--changeset system:create-job_instance
CREATE TABLE ${tablePrefix}job_instance (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    instance_id VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'WAITING',
    error_message varchar(1000),
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    created_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ${tablePrefix}job_instance_unique_index UNIQUE (job_id, instance_id)
)

