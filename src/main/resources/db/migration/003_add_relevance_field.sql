-- 添加 relevance（相关度）字段到政策相关表
-- 用于存储政策与用户产品的相关度评估结果

-- 1. 为 policy_info 表添加 relevance 字段
ALTER TABLE `policy_info`
ADD COLUMN `relevance` varchar(16) DEFAULT '中' COMMENT '与产品的相关度: 高|中|低'
AFTER `importance`;

-- 2. 为 policy_analysis 表添加 relevance 字段
ALTER TABLE `policy_analysis`
ADD COLUMN `relevance` varchar(16) DEFAULT '中' COMMENT '与产品的相关度: 高|中|低'
AFTER `impact_analysis`;
