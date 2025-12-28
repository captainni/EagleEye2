-- 爬虫服务集成 - 数据库迁移脚本
-- 日期: 2025-12-28
-- 说明: 支持 eagleeye-crawler 新服务集成

-- ============================================
-- 1. crawler_config 表修改
-- ============================================

-- 1.1 删除 target_type 字段（改为 AI 自动判定）
ALTER TABLE crawler_config DROP COLUMN target_type;

-- 1.2 新增 crawler_service 字段（指定使用的爬虫服务）
ALTER TABLE crawler_config ADD COLUMN crawler_service VARCHAR(50) DEFAULT 'legacy' COMMENT '爬虫服务类型: legacy=旧服务, eagleeye=新服务';

-- 1.3 新增 result_path 字段（记录结果文件路径）
ALTER TABLE crawler_config ADD COLUMN result_path VARCHAR(500) COMMENT '最新爬取结果文件夹路径';

-- 1.4 修改 trigger_schedule 为可空（支持手动触发）
ALTER TABLE crawler_config MODIFY trigger_schedule VARCHAR(100) NULL;

-- ============================================
-- 2. crawler_task_log 表修改
-- ============================================

-- 2.1 新增 batch_path 字段（记录批次文件夹路径）
ALTER TABLE crawler_task_log ADD COLUMN batch_path VARCHAR(500) COMMENT '批次文件夹路径 (如: crawl_files/20251228_100307_eastmoney_bank)';

-- 2.2 新增 article_count 字段（记录爬取文章数）
ALTER TABLE crawler_task_log ADD COLUMN article_count INT DEFAULT 0 COMMENT '本次爬取的文章数量';

-- 2.3 新增 category_stats 字段（记录分类统计）
ALTER TABLE crawler_task_log ADD COLUMN category_stats VARCHAR(200) COMMENT '分类统计 JSON: {"policy":2,"competitor":1}';

-- ============================================
-- 3. 添加索引
-- ============================================

-- 为 crawler_service 添加索引
ALTER TABLE crawler_config ADD INDEX idx_crawler_service (crawler_service);

-- 为 batch_path 添加索引
ALTER TABLE crawler_task_log ADD INDEX idx_batch_path (batch_path);

-- ============================================
-- 4. 示例数据插入
-- ============================================

-- 插入 eagleeye-crawler 新服务配置示例
INSERT INTO crawler_config (
    target_name,
    source_urls,
    crawler_service,
    extraction_strategy_type,
    extraction_schema,
    is_active,
    is_deleted
) VALUES (
    '东方财富银行资讯',
    'https://bank.eastmoney.com/a/czzyh.html',
    'eagleeye',
    'ai',
    '{}',
    1,
    0
);
