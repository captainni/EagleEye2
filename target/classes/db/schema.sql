-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS eagleeye DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE eagleeye;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(32) DEFAULT NULL COMMENT '手机号',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户偏好表
CREATE TABLE IF NOT EXISTS `user_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `policy_areas` json DEFAULT NULL COMMENT '关注的政策领域',
  `competitor_ids` json DEFAULT NULL COMMENT '关注的竞品机构',
  `push_config` json DEFAULT NULL COMMENT '推送配置',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户偏好表';

-- 政策信息表
CREATE TABLE IF NOT EXISTS `policy_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '政策标题',
  `source` varchar(128) NOT NULL COMMENT '政策来源',
  `source_url` varchar(512) DEFAULT NULL COMMENT '原文链接',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `content` text COMMENT '政策原文',
  `policy_type` varchar(64) DEFAULT NULL COMMENT '政策类型',
  `importance` varchar(16) DEFAULT NULL COMMENT '重要程度：高、中、低',
  `areas` json DEFAULT NULL COMMENT '相关领域',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政策信息表';

-- 政策分析表
CREATE TABLE IF NOT EXISTS `policy_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `policy_id` bigint NOT NULL COMMENT '政策ID',
  `summary` varchar(1000) DEFAULT NULL COMMENT '政策摘要',
  `key_points` json DEFAULT NULL COMMENT '关键条款',
  `impact_analysis` text COMMENT '影响分析',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_policy_id` (`policy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政策分析表';

-- 政策建议表
CREATE TABLE IF NOT EXISTS `policy_suggestion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `policy_id` bigint NOT NULL COMMENT '政策ID',
  `analysis_id` bigint NOT NULL COMMENT '分析ID',
  `suggestion` varchar(500) NOT NULL COMMENT '建议内容',
  `reason` varchar(1000) DEFAULT NULL COMMENT '建议理由',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_policy_id` (`policy_id`),
  KEY `idx_analysis_id` (`analysis_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='政策建议表';

-- 竞品信息表（更新为符合前端需求的结构）
DROP TABLE IF EXISTS `competitor_info`;
CREATE TABLE IF NOT EXISTS `competitor_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '竞品动态标题',
  `company` varchar(128) NOT NULL COMMENT '竞品公司/机构名称',
  `type` varchar(64) DEFAULT NULL COMMENT '动态类型：产品更新、营销活动、财报数据、APP更新、利率调整、合作动态等',
  `capture_time` datetime DEFAULT NULL COMMENT '抓取时间',
  `tags` json DEFAULT NULL COMMENT '相关标签(JSON格式存储)',
  `content` text COMMENT '详细内容',
  `sources` json DEFAULT NULL COMMENT '来源链接(JSON格式存储)',
  `summary` varchar(1000) DEFAULT NULL COMMENT '分析摘要',
  `related_info` text COMMENT '相关信息/详细说明',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company` (`company`),
  KEY `idx_capture_time` (`capture_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞品信息表';

-- 竞品分析建议表
DROP TABLE IF EXISTS `competitor_analysis`;
CREATE TABLE IF NOT EXISTS `competitor_analysis` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `competitor_id` bigint NOT NULL COMMENT '关联的竞品信息ID',
  `content` varchar(1000) NOT NULL COMMENT '分析和建议内容',
  `sort_order` int DEFAULT 0 COMMENT '排序序号',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_competitor_id` (`competitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞品分析建议表';

-- 竞品资源链接表
CREATE TABLE IF NOT EXISTS `competitor_source` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `competitor_id` bigint NOT NULL COMMENT '关联的竞品信息ID',
  `title` varchar(255) NOT NULL COMMENT '资源标题',
  `url` varchar(512) NOT NULL COMMENT '资源URL',
  `sort_order` int DEFAULT 0 COMMENT '排序序号',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_competitor_id` (`competitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞品资源链接表';

-- 竞品标签表
CREATE TABLE IF NOT EXISTS `competitor_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `competitor_id` bigint NOT NULL COMMENT '关联的竞品信息ID',
  `label` varchar(64) NOT NULL COMMENT '标签名称',
  `color` varchar(32) DEFAULT NULL COMMENT '标签颜色',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_competitor_id` (`competitor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞品标签表';

-- 需求表
CREATE TABLE IF NOT EXISTS `requirement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '需求标题',
  `description` text COMMENT '需求描述',
  `background` text COMMENT '需求背景',
  `priority` varchar(16) DEFAULT NULL COMMENT '优先级：高、中、低',
  `status` varchar(32) DEFAULT 'NEW' COMMENT '状态：NEW-新建，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已拒绝',
  `source_type` varchar(32) DEFAULT NULL COMMENT '来源类型：POLICY-政策建议，COMPETITOR-竞品分析，MANUAL-手动创建',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `user_id` bigint DEFAULT NULL COMMENT '创建用户ID',
  `plan_time` datetime DEFAULT NULL COMMENT '计划实施时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source` (`source_type`,`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求表';

-- 爬虫配置表
CREATE TABLE IF NOT EXISTS `crawler_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置主键ID',
  `target_name` varchar(255) NOT NULL COMMENT '目标名称 (e.g., \'中国人民银行政策发布\')',
  `target_type` varchar(50) NOT NULL COMMENT '目标类型 (\'policy\', \'competitor\')',
  `source_urls` text NOT NULL COMMENT '起始/入口 URL 列表 (建议JSON格式)',
  `crawl_depth` int DEFAULT 1 COMMENT '抓取深度 (可选, 默认1)',
  `trigger_schedule` varchar(100) DEFAULT NULL COMMENT '触发计划 (Cron 表达式)',
  `extraction_strategy_type` varchar(50) NOT NULL COMMENT '提取策略类型 (\'css\', \'llm\')',
  `extraction_schema` text NOT NULL COMMENT '提取Schema (JSON格式: CSS选择器或LLM的Pydantic Schema)',
  `llm_instruction` text COMMENT 'LLM提取指令 (当策略为LLM时)',
  `llm_provider_config` text COMMENT 'LLM提供商配置 (当策略为LLM时, JSON格式)',
  `crawl4ai_config_override` text COMMENT 'Crawl4AI配置覆盖 (JSON格式, 用于覆盖CrawlerRunConfig)',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用: 0-禁用, 1-启用',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爬虫任务配置表';

-- 用户信息源订阅表
CREATE TABLE IF NOT EXISTS `user_source_subscription` (
  `subscription_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订阅主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `config_id` bigint NOT NULL COMMENT '订阅的爬虫配置ID',
  `subscription_status` varchar(50) NOT NULL DEFAULT 'active' COMMENT '订阅状态 (\'active\', \'inactive\', \'paused\')',
  `notification_preference` text COMMENT '通知偏好设置 (JSON格式)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除: 0-未删除, 1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`subscription_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_user_config` (`user_id`, `config_id`), -- 联合索引，方便查询用户对特定源的订阅
  KEY `idx_subscription_status` (`subscription_status`),
  FOREIGN KEY (`config_id`) REFERENCES `crawler_config`(`config_id`) -- 外键约束
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息源订阅表';

-- 爬虫任务执行日志表
CREATE TABLE crawler_task_log (
    log_id BIGINT AUTO_INCREMENT COMMENT '日志主键ID',
    task_id VARCHAR(64) NOT NULL COMMENT '任务唯一ID (来自MQ消息)',
    config_id BIGINT NOT NULL COMMENT '关联的爬虫配置ID',
    target_url TEXT COMMENT '本次任务抓取的主要目标URL',
    start_time DATETIME COMMENT '任务大致开始时间 (由消费者记录)',
    end_time DATETIME COMMENT '任务结束时间 (抓取完成时间，来自MQ消息)',
    status VARCHAR(20) NOT NULL COMMENT '执行状态 (success, failure)',
    error_message TEXT COMMENT '错误信息 (如果 status=failure)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除 (0:否, 1:是)',
    PRIMARY KEY (log_id),
    INDEX idx_task_id (task_id),
    INDEX idx_config_id_status_endtime (config_id, status, end_time) -- 常用查询索引
) COMMENT = '爬虫任务执行日志表'; 

-- 用户建议表
DROP TABLE IF EXISTS `user_suggestion`;
CREATE TABLE `user_suggestion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '建议ID',
  `user_id` bigint DEFAULT NULL COMMENT '提交用户ID',
  `suggestion_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '建议类型 (如: NEW_SOURCE, CONFIG_ERROR)',
  `source_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '来源名称 (用户建议时填写)',
  `suggestion_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '建议内容 (通常是URL)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态 (PENDING, APPROVED, REJECTED)',
  `remarks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '建议理由/处理备注',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标志 (0: 未删除, 1: 已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_suggestion_status` (`status`) USING BTREE COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户建议表';

-- 用户产品表
CREATE TABLE IF NOT EXISTS `user_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(255) NOT NULL COMMENT '产品名称',
  `type` varchar(128) NOT NULL COMMENT '产品类型',
  `features` text COMMENT '产品特点',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户产品表';

-- 用户设置表
CREATE TABLE IF NOT EXISTS `user_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `frequency` varchar(32) NOT NULL COMMENT '推送频率 (daily, weekly, monthly, never)',
  `channels` text COMMENT '推送渠道 (JSON格式，如：["wechat", "site", "sms", "email"])',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表'; 