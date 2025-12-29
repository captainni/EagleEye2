package com.eagleeye.service.competitor;

/**
 * 竞品分析服务接口
 *
 * @author eagleeye
 */
public interface CompetitorAnalysisService {

    /**
     * 异步分析指定批次中的竞品文章
     *
     * @param taskLogId 爬虫任务日志ID
     * @param userId 用户ID
     */
    void analyzeCompetitorsAsync(Long taskLogId, Long userId);

    /**
     * 分析指定批次中的竞品文章（同步）
     *
     * @param taskLogId 爬虫任务日志ID
     * @param userId 用户ID
     * @return 分析结果摘要
     */
    AnalysisSummary analyzeCompetitors(Long taskLogId, Long userId);

    /**
     * 分析结果统计
     */
    class AnalysisSummary {
        private int total;      // 总数
        private int success;    // 成功数
        private int skipped;    // 跳过数
        private int failed;     // 失败数

        public AnalysisSummary(int total, int success, int skipped, int failed) {
            this.total = total;
            this.success = success;
            this.skipped = skipped;
            this.failed = failed;
        }

        public int getTotal() { return total; }
        public int getSuccess() { return success; }
        public int getSkipped() { return skipped; }
        public int getFailed() { return failed; }

        @Override
        public String toString() {
            return String.format("总数=%d, 成功=%d, 跳过=%d, 失败=%d", total, success, skipped, failed);
        }
    }
}
