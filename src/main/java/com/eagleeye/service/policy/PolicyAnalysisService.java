package com.eagleeye.service.policy;

/**
 * 政策分析服务接口
 * 处理政策文章的批量分析和入库
 *
 * @author eagleeye
 */
public interface PolicyAnalysisService {

    /**
     * 异步分析指定批次中的政策文章
     *
     * @param taskLogId 任务日志ID
     * @param userId 用户ID（用于获取产品上下文）
     */
    void analyzePoliciesAsync(Long taskLogId, Long userId);

    /**
     * 分析指定批次中的政策文章（同步）
     *
     * @param taskLogId 任务日志ID
     * @param userId 用户ID（用于获取产品上下文）
     * @return 分析结果统计
     */
    AnalysisSummary analyzePolicies(Long taskLogId, Long userId);

    /**
     * 分析结果统计
     */
    class AnalysisSummary {
        private int total;
        private int success;
        private int skipped;
        private int failed;

        public AnalysisSummary(int total, int success, int skipped, int failed) {
            this.total = total;
            this.success = success;
            this.skipped = skipped;
            this.failed = failed;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getSuccess() {
            return success;
        }

        public void setSuccess(int success) {
            this.success = success;
        }

        public int getSkipped() {
            return skipped;
        }

        public void setSkipped(int skipped) {
            this.skipped = skipped;
        }

        public int getFailed() {
            return failed;
        }

        public void setFailed(int failed) {
            this.failed = failed;
        }

        @Override
        public String toString() {
            return "AnalysisSummary{" +
                    "total=" + total +
                    ", success=" + success +
                    ", skipped=" + skipped +
                    ", failed=" + failed +
                    '}';
        }
    }
}
