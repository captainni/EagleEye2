-- 为 CrawlerTaskLog 表添加政策分析相关字段
-- 用于支持政策文章分析入库功能

ALTER TABLE `crawler_task_log`
ADD COLUMN `analysis_status` varchar(32) DEFAULT NULL COMMENT '分析状态: pending/analyzing/completed/failed',
ADD COLUMN `analysis_result` json DEFAULT NULL COMMENT '分析结果: {"total":5,"success":3,"skipped":1,"failed":1}';
