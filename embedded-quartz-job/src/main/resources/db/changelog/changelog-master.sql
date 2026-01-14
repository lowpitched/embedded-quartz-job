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
    current_instance VARCHAR(255),
    total_shards INTEGER DEFAULT 0,
    params JSONB,
    created_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_job_group_name ON job(group_name);
CREATE INDEX idx_job_status ON job(status);
CREATE INDEX idx_job_current_instance ON job(current_instance);

-- 添加注释
COMMENT ON TABLE job IS '定时任务实体表';
COMMENT ON COLUMN job.id IS '主键ID';
COMMENT ON COLUMN job.name IS '任务名称';
COMMENT ON COLUMN job.group_name IS '任务组名';
COMMENT ON COLUMN job.clazz IS '任务类全限定名';
COMMENT ON COLUMN job.cron IS 'Cron表达式';
COMMENT ON COLUMN job.status IS '任务状态(NORMAL,PAUSED,COMPLETE,ERROR,BLOCKED)';
COMMENT ON COLUMN job.description IS '任务描述';
COMMENT ON COLUMN job.current_instance IS '当前执行实例';
COMMENT ON COLUMN job.total_shards IS '总分片数';
COMMENT ON COLUMN job.params IS '任务参数(JSONB格式)';
COMMENT ON COLUMN job.created_time IS '创建时间';
COMMENT ON COLUMN job.updated_time IS '更新时间';
