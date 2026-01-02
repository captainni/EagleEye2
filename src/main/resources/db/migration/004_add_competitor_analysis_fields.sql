-- 添加竞品分析相关字段到 competitor_info 表
-- 用于支持竞品文章分析入库功能
-- 日期: 2025-12-31

-- 1. 为 competitor_info 表添加分析相关字段
ALTER TABLE `competitor_info`
ADD COLUMN `importance` varchar(16) DEFAULT '中' COMMENT '重要程度: 高|中|低' AFTER `type`,
ADD COLUMN `relevance` varchar(16) DEFAULT '中' COMMENT '与我方产品的相关度: 高|中|低' AFTER `importance`,
ADD COLUMN `key_points` json DEFAULT NULL COMMENT '关键要点(JSON格式存储，用于详情页高亮)' AFTER `relevance`,
ADD COLUMN `market_impact` text DEFAULT NULL COMMENT '市场影响分析' AFTER `summary`;

-- 2. 为 competitor_analysis 表添加详细分析字段
ALTER TABLE `competitor_analysis`
ADD COLUMN `importance` varchar(16) DEFAULT NULL COMMENT '重要程度: 高|中|低' AFTER `content`,
ADD COLUMN `relevance` varchar(16) DEFAULT NULL COMMENT '相关度: 高|中|低' AFTER `importance`,
ADD COLUMN `key_points` json DEFAULT NULL COMMENT '关键要点(JSON格式存储)' AFTER `relevance`,
ADD COLUMN `market_impact` text DEFAULT NULL COMMENT '市场影响分析' AFTER `key_points`,
ADD COLUMN `competitive_analysis` text DEFAULT NULL COMMENT '竞争态势分析' AFTER `market_impact`,
ADD COLUMN `our_suggestions` json DEFAULT NULL COMMENT '针对性建议列表(JSON格式，每条包含 suggestion 和 reason)' AFTER `competitive_analysis`;
