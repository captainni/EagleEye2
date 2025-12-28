package com.eagleeye.service.policy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eagleeye.model.dto.AnalysisResult;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.model.entity.PolicyAnalysis;
import com.eagleeye.model.entity.PolicyInfo;
import com.eagleeye.model.entity.PolicySuggestion;
import com.eagleeye.repository.CrawlerTaskLogRepository;
import com.eagleeye.repository.PolicyAnalysisRepository;
import com.eagleeye.repository.PolicyRepository;
import com.eagleeye.repository.PolicySuggestionRepository;
import com.eagleeye.service.policy.PolicyAnalysisService;
import com.eagleeye.service.policy.PolicyAnalyzer;
import com.eagleeye.service.settings.SettingsService;
import com.eagleeye.model.vo.ProductVO;
import com.eagleeye.model.vo.SettingsDataVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 政策分析服务实现
 *
 * @author eagleeye
 */
@Service
public class PolicyAnalysisServiceImpl implements PolicyAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(PolicyAnalysisServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${crawl-files.base-path}")
    private String crawlFilesBasePath;

    private final PolicyAnalyzer policyAnalyzer;
    private final CrawlerTaskLogRepository taskLogRepository;
    private final PolicyRepository policyRepository;
    private final PolicyAnalysisRepository policyAnalysisRepository;
    private final PolicySuggestionRepository policySuggestionRepository;
    private final SettingsService settingsService;

    public PolicyAnalysisServiceImpl(
            PolicyAnalyzer policyAnalyzer,
            CrawlerTaskLogRepository taskLogRepository,
            PolicyRepository policyRepository,
            PolicyAnalysisRepository policyAnalysisRepository,
            PolicySuggestionRepository policySuggestionRepository,
            SettingsService settingsService) {
        this.policyAnalyzer = policyAnalyzer;
        this.taskLogRepository = taskLogRepository;
        this.policyRepository = policyRepository;
        this.policyAnalysisRepository = policyAnalysisRepository;
        this.policySuggestionRepository = policySuggestionRepository;
        this.settingsService = settingsService;
    }

    @Async
    @Override
    public void analyzePoliciesAsync(Long taskLogId, Long userId) {
        log.info("异步分析政策文章: taskLogId={}, userId={}", taskLogId, userId);
        try {
            analyzePolicies(taskLogId, userId);
        } catch (Exception e) {
            log.error("异步分析失败: taskLogId={}, userId={}", taskLogId, userId, e);
            // 更新任务状态为失败
            updateTaskLogStatus(taskLogId, "failed", null);
        }
    }

    @Override
    @Transactional
    public AnalysisSummary analyzePolicies(Long taskLogId, Long userId) {
        log.info("开始分析政策文章: taskLogId={}, userId={}", taskLogId, userId);

        // 1. 获取任务日志
        CrawlerTaskLog taskLog = taskLogRepository.selectById(taskLogId);
        if (taskLog == null) {
            throw new RuntimeException("任务日志不存在: " + taskLogId);
        }

        String batchPath = taskLog.getBatchPath();
        if (batchPath == null || batchPath.isEmpty()) {
            throw new RuntimeException("批次路径为空: " + taskLogId);
        }

        // 如果是相对路径，转换为绝对路径
        if (!batchPath.startsWith("/")) {
            batchPath = crawlFilesBasePath + batchPath;
        }
        log.info("使用批次路径: {}", batchPath);

        // 2. 更新任务状态为分析中
        updateTaskLogStatus(taskLogId, "analyzing", null);

        // 3. 读取 metadata.json
        List<ArticleInfo> policyArticles;
        try {
            policyArticles = getPolicyArticles(batchPath);
        } catch (IOException e) {
            log.error("读取 metadata.json 失败: batchPath={}", batchPath, e);
            throw new RuntimeException("读取批次文件失败: " + e.getMessage(), e);
        }
        log.info("找到 {} 篇政策文章", policyArticles.size());

        int total = policyArticles.size();
        int success = 0;
        int skipped = 0;
        int failed = 0;

        // 4. 逐个分析政策文章
        for (ArticleInfo article : policyArticles) {
            try {
                processPolicyArticle(article.filePath, article.url, article.source, userId);
                success++;
            } catch (Exception e) {
                log.error("分析政策文章失败: {}", article.filePath, e);
                failed++;
            }
        }

        // 5. 更新任务状态为完成
        AnalysisSummary summary = new AnalysisSummary(total, success, skipped, failed);
        updateTaskLogStatus(taskLogId, "completed", summary);

        log.info("政策分析完成: {}", summary);
        return summary;
    }

    /**
     * 存储文章路径和 URL 映射的内部类
     */
    private static class ArticleInfo {
        String filePath;
        String url;
        String source;  // 新增：文章来源

        ArticleInfo(String filePath, String url, String source) {
            this.filePath = filePath;
            this.url = url;
            this.source = source;
        }
    }

    /**
     * 获取批次中所有政策文章的 Markdown 文件路径和 URL
     */
    private List<ArticleInfo> getPolicyArticles(String batchPath) throws IOException {
        List<ArticleInfo> articles = new ArrayList<>();

        // 读取 metadata.json
        File metadataFile = new File(batchPath, "metadata.json");
        if (!metadataFile.exists()) {
            log.warn("metadata.json 不存在: {}", metadataFile.getAbsolutePath());
            return articles;
        }

        // 读取文件内容
        String metadataContent = Files.readString(metadataFile.toPath());

        JsonNode rootNode = objectMapper.readTree(metadataContent);
        JsonNode articlesNode = rootNode.get("articles");

        if (articlesNode == null || !articlesNode.isArray()) {
            log.warn("metadata.json 中没有 articles 数组");
            return articles;
        }

        // 【修复】从顶层读取 source（批次级别的来源）
        JsonNode topSourceNode = rootNode.get("source");
        String batchSource = (topSourceNode != null) ? topSourceNode.asText() : null;

        // 筛选 category 为 "policy" 的文章
        for (JsonNode articleNode : articlesNode) {
            JsonNode categoryNode = articleNode.get("category");
            if (categoryNode != null && "policy".equals(categoryNode.asText())) {
                JsonNode filenameNode = articleNode.get("filename");
                JsonNode urlNode = articleNode.get("url");

                if (filenameNode != null) {
                    File file = new File(batchPath, filenameNode.asText());
                    if (!file.exists()) {
                        log.warn("文件不存在: {}", file.getAbsolutePath());
                        continue;
                    }
                    // 提取 URL（从 article）和 source（从顶层）
                    String url = (urlNode != null) ? urlNode.asText() : null;
                    articles.add(new ArticleInfo(file.getAbsolutePath(), url, batchSource));
                }
            }
        }

        return articles;
    }

    /**
     * 处理单篇政策文章
     */
    @Transactional
    private void processPolicyArticle(String markdownPath, String sourceUrl, String source, Long userId) throws IOException {
        log.debug("处理政策文章: {}", markdownPath);

        // 1. 读取 Markdown 内容
        String markdownContent = Files.readString(Paths.get(markdownPath));

        // 2. 检查是否已存在（通过 sourceUrl 去重）
        // 如果传入的 sourceUrl 为空，则尝试从 markdown 中提取
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            sourceUrl = extractSourceUrl(markdownContent);
        }

        if (sourceUrl != null && isPolicyExists(sourceUrl)) {
            log.info("政策已存在，跳过: {}", sourceUrl);
            return;
        }

        // 3. 获取用户产品列表
        String productsJson = null;
        try {
            SettingsDataVO settingsData = settingsService.getSettingsData(userId);
            List<ProductVO> products = settingsData.getMyProducts();
            if (products != null && !products.isEmpty()) {
                List<Map<String, String>> productList = new ArrayList<>();
                for (ProductVO product : products) {
                    Map<String, String> productInfo = new HashMap<>();
                    productInfo.put("name", product.getName());
                    productInfo.put("type", product.getType());
                    productInfo.put("features", product.getFeatures());
                    productList.add(productInfo);
                }
                productsJson = objectMapper.writeValueAsString(productList);
                log.debug("获取到 {} 个产品信息", products.size());
            }
        } catch (Exception e) {
            log.warn("获取用户产品列表失败: userId={}", userId, e);
        }

        // 4. 调用 AI 分析（传递产品上下文）
        AnalysisResult result = policyAnalyzer.analyze(markdownContent, productsJson);

        // 5. 存储到数据库（传入 sourceUrl 和 source）
        savePolicyAnalysisResult(markdownContent, sourceUrl, source, result);
    }

    /**
     * 从 Markdown 内容中提取 sourceUrl
     */
    private String extractSourceUrl(String markdownContent) {
        // 1. 首先尝试从元数据格式提取
        String[] lines = markdownContent.split("\n");
        for (String line : lines) {
            if (line.contains("原文链接:") || line.contains("sourceUrl:")) {
                return line.replace("原文链接:", "")
                        .replace("sourceUrl:", "")
                        .trim();
            }
        }

        // 2. 尝试从 ## 附 部分提取链接 [标题](URL)
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("## 附") || line.contains("## 参考")) {
                // 找到附节后，检查接下来的几行（最多5行）查找链接
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
                for (int j = i + 1; j < Math.min(i + 6, lines.length); j++) {
                    String checkLine = lines[j];
                    java.util.regex.Matcher matcher = pattern.matcher(checkLine);
                    if (matcher.find()) {
                        return matcher.group(2);
                    }
                }
                break;
            }
        }

        // 3. 尝试在整个内容中查找第一个 http 链接
        java.util.regex.Pattern urlPattern = java.util.regex.Pattern.compile("https?://[^\\s\\])\\>]+");
        java.util.regex.Matcher urlMatcher = urlPattern.matcher(markdownContent);
        if (urlMatcher.find()) {
            return urlMatcher.group();
        }

        return null;
    }

    /**
     * 检查政策是否已存在
     */
    private boolean isPolicyExists(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<PolicyInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PolicyInfo::getSourceUrl, sourceUrl);
        return policyRepository.selectCount(wrapper) > 0;
    }

    /**
     * 保存政策分析结果到数据库
     */
    @Transactional
    private void savePolicyAnalysisResult(String markdownContent, String sourceUrl, String source, AnalysisResult result) {
        // 1. 保存 PolicyInfo
        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setTitle(extractTitle(markdownContent));
        // 优先使用传入的 source，否则从 markdown 中提取
        policyInfo.setSource(source != null ? source : extractSource(markdownContent));
        policyInfo.setSourceUrl(sourceUrl);  // 直接使用传入的 sourceUrl
        policyInfo.setPublishTime(extractPublishTime(markdownContent));
        policyInfo.setContent(markdownContent);
        policyInfo.setPolicyType(result.getPolicyType());
        policyInfo.setImportance(result.getImportance());
        policyInfo.setAreas(convertAreasToJson(result.getAreas()));
        policyInfo.setCreateTime(LocalDateTime.now());
        policyInfo.setUpdateTime(LocalDateTime.now());

        policyRepository.insert(policyInfo);

        // 2. 保存 PolicyAnalysis
        PolicyAnalysis policyAnalysis = new PolicyAnalysis();
        policyAnalysis.setPolicyId(policyInfo.getId());
        policyAnalysis.setSummary(result.getSummary());
        policyAnalysis.setKeyPoints(convertKeyPointsToJson(result.getKeyPoints()));
        policyAnalysis.setImpactAnalysis(result.getImpactAnalysis());
        policyAnalysis.setRelevance(result.getRelevance());  // 新增：保存相关度
        policyAnalysis.setCreateTime(LocalDateTime.now());
        policyAnalysis.setUpdateTime(LocalDateTime.now());

        policyAnalysisRepository.insert(policyAnalysis);

        // 3. 保存 PolicySuggestion
        if (result.getSuggestions() != null) {
            for (AnalysisResult.Suggestion suggestion : result.getSuggestions()) {
                PolicySuggestion policySuggestion = new PolicySuggestion();
                policySuggestion.setPolicyId(policyInfo.getId());
                policySuggestion.setAnalysisId(policyAnalysis.getId());
                policySuggestion.setSuggestion(suggestion.getSuggestion());
                policySuggestion.setReason(suggestion.getReason());
                policySuggestion.setCreateTime(LocalDateTime.now());
                policySuggestion.setUpdateTime(LocalDateTime.now());

                policySuggestionRepository.insert(policySuggestion);
            }
        }

        log.info("政策分析结果已保存: policyId={}", policyInfo.getId());
    }

    /**
     * 从 Markdown 内容中提取标题
     */
    private String extractTitle(String markdownContent) {
        String[] lines = markdownContent.split("\n");
        for (String line : lines) {
            if (line.startsWith("# ")) {
                return line.substring(2).trim();
            }
        }
        return "未知标题";
    }

    /**
     * 从 Markdown 内容中提取来源
     */
    private String extractSource(String markdownContent) {
        String[] lines = markdownContent.split("\n");
        for (String line : lines) {
            if (line.contains("来源:") || line.contains("source:")) {
                return line.replace("来源:", "")
                        .replace("source:", "")
                        .trim();
            }
        }
        return "未知来源";
    }

    /**
     * 从 Markdown 内容中提取发布时间
     */
    private LocalDateTime extractPublishTime(String markdownContent) {
        String[] lines = markdownContent.split("\n");
        for (String line : lines) {
            if (line.contains("发布时间:") || line.contains("publishTime:")) {
                String timeStr = line.replace("发布时间:", "")
                        .replace("publishTime:", "")
                        .trim();
                try {
                    return LocalDateTime.parse(timeStr);
                } catch (Exception e) {
                    log.warn("解析发布时间失败: {}", timeStr);
                }
            }
        }
        return LocalDateTime.now();
    }

    /**
     * 将 areas 列表转换为 JSON 字符串
     */
    private String convertAreasToJson(List<String> areas) {
        try {
            return objectMapper.writeValueAsString(areas);
        } catch (Exception e) {
            log.error("转换 areas 为 JSON 失败", e);
            return "[]";
        }
    }

    /**
     * 将 keyPoints 列表转换为 JSON 字符串
     */
    private String convertKeyPointsToJson(List<String> keyPoints) {
        try {
            return objectMapper.writeValueAsString(keyPoints);
        } catch (Exception e) {
            log.error("转换 keyPoints 为 JSON 失败", e);
            return "[]";
        }
    }

    /**
     * 更新任务日志的分析状态
     */
    private void updateTaskLogStatus(Long taskLogId, String status, AnalysisSummary summary) {
        CrawlerTaskLog taskLog = new CrawlerTaskLog();
        taskLog.setLogId(taskLogId);
        taskLog.setAnalysisStatus(status);

        if (summary != null) {
            try {
                String summaryJson = objectMapper.writeValueAsString(summary);
                taskLog.setAnalysisResult(summaryJson);
            } catch (Exception e) {
                log.error("序列化分析结果失败", e);
            }
        }

        taskLogRepository.updateById(taskLog);
    }

    /**
     * 清理 JSON 内容中的格式问题
     * 主要处理未转义的引号等字符
     */
    private String cleanJsonContent(String jsonContent) {
        if (jsonContent == null) return null;

        // 替换字符串值内部的未转义双引号为单引号
        // 例如："title": "银行"开门红"揽储" -> "title": "银行'开门红'揽储"
        // 这个正则匹配 JSON 字符串值中出现的未转义引号
        String cleaned = jsonContent.replaceAll(": \"([^\"]{0,50})\"([^\"]{0,50})\"([^\"]{0,50})\"", ": \"$1'$2'$3\"");

        return cleaned;
    }
}
