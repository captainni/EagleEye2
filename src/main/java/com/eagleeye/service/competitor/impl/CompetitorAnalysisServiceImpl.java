package com.eagleeye.service.competitor.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eagleeye.model.dto.CompetitorAnalysisResult;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.model.entity.CompetitorAnalysis;
import com.eagleeye.model.entity.CompetitorInfo;
import com.eagleeye.repository.CrawlerTaskLogRepository;
import com.eagleeye.repository.CompetitorAnalysisRepository;
import com.eagleeye.repository.CompetitorRepository;
import com.eagleeye.service.competitor.CompetitorAnalysisService;
import com.eagleeye.service.competitor.CompetitorAnalyzer;
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
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 竞品分析服务实现
 *
 * @author eagleeye
 */
@Service
public class CompetitorAnalysisServiceImpl implements CompetitorAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CompetitorAnalysisServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${crawl-files.base-path}")
    private String crawlFilesBasePath;

    private final CompetitorAnalyzer competitorAnalyzer;
    private final CrawlerTaskLogRepository taskLogRepository;
    private final CompetitorRepository competitorRepository;
    private final CompetitorAnalysisRepository competitorAnalysisRepository;
    private final SettingsService settingsService;

    public CompetitorAnalysisServiceImpl(
            CompetitorAnalyzer competitorAnalyzer,
            CrawlerTaskLogRepository taskLogRepository,
            CompetitorRepository competitorRepository,
            CompetitorAnalysisRepository competitorAnalysisRepository,
            SettingsService settingsService) {
        this.competitorAnalyzer = competitorAnalyzer;
        this.taskLogRepository = taskLogRepository;
        this.competitorRepository = competitorRepository;
        this.competitorAnalysisRepository = competitorAnalysisRepository;
        this.settingsService = settingsService;
    }

    @Async
    @Override
    public void analyzeCompetitorsAsync(Long taskLogId, Long userId) {
        log.info("异步分析竞品文章: taskLogId={}, userId={}", taskLogId, userId);
        try {
            analyzeCompetitors(taskLogId, userId);
        } catch (Exception e) {
            log.error("异步分析失败: taskLogId={}, userId={}", taskLogId, userId, e);
            // 更新任务状态为失败
            updateTaskLogStatus(taskLogId, "failed", null);
        }
    }

    @Override
    @Transactional
    public AnalysisSummary analyzeCompetitors(Long taskLogId, Long userId) {
        log.info("开始分析竞品文章: taskLogId={}, userId={}", taskLogId, userId);

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
        List<ArticleInfo> competitorArticles;
        try {
            competitorArticles = getCompetitorArticles(batchPath);
        } catch (IOException e) {
            log.error("读取 metadata.json 失败: batchPath={}", batchPath, e);
            throw new RuntimeException("读取批次文件失败: " + e.getMessage(), e);
        }
        log.info("找到 {} 篇竞品文章", competitorArticles.size());

        int total = competitorArticles.size();
        int success = 0;
        int skipped = 0;
        int failed = 0;

        // 4. 逐个分析竞品文章
        for (ArticleInfo article : competitorArticles) {
            try {
                processCompetitorArticle(article.filePath, article.url, article.source, userId);
                success++;
            } catch (Exception e) {
                log.error("分析竞品文章失败: {}", article.filePath, e);
                failed++;
            }
        }

        // 5. 更新任务状态为完成
        AnalysisSummary summary = new AnalysisSummary(total, success, skipped, failed);
        updateTaskLogStatus(taskLogId, "completed", summary);

        log.info("竞品分析完成: {}", summary);
        return summary;
    }

    /**
     * 存储文章路径和 URL 映射的内部类
     */
    private static class ArticleInfo {
        String filePath;
        String url;
        String source;

        ArticleInfo(String filePath, String url, String source) {
            this.filePath = filePath;
            this.url = url;
            this.source = source;
        }
    }

    /**
     * 获取批次中所有竞品文章的 Markdown 文件路径和 URL
     */
    private List<ArticleInfo> getCompetitorArticles(String batchPath) throws IOException {
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

        // 从顶层读取 source（批次级别的来源）
        JsonNode topSourceNode = rootNode.get("source");
        String batchSource = (topSourceNode != null) ? topSourceNode.asText() : null;

        // 筛选 category 为 "competitor" 的文章
        for (JsonNode articleNode : articlesNode) {
            JsonNode categoryNode = articleNode.get("category");
            if (categoryNode != null && "competitor".equals(categoryNode.asText())) {
                JsonNode filenameNode = articleNode.get("filename");
                JsonNode urlNode = articleNode.get("url");

                if (filenameNode != null) {
                    File file = new File(batchPath, filenameNode.asText());
                    if (!file.exists()) {
                        log.warn("文件不存在: {}", file.getAbsolutePath());
                        continue;
                    }
                    String url = (urlNode != null) ? urlNode.asText() : null;
                    articles.add(new ArticleInfo(file.getAbsolutePath(), url, batchSource));
                }
            }
        }

        return articles;
    }

    /**
     * 处理单篇竞品文章
     */
    @Transactional
    private void processCompetitorArticle(String markdownPath, String sourceUrl, String source, Long userId) throws IOException {
        log.debug("处理竞品文章: {}", markdownPath);

        // 1. 读取 Markdown 内容
        String markdownContent = Files.readString(Paths.get(markdownPath));

        // 2. 提取 sourceUrl（用于去重检查和存储）
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            sourceUrl = extractSourceUrl(markdownContent);
        }

        // 注意：移除了去重检查，允许重新分析已存在的文章
        // 这样用户点击【竞品分析】按钮时可以真正重新分析

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

        // 4. 调用 AI 分析
        CompetitorAnalysisResult result = competitorAnalyzer.analyze(markdownContent, productsJson);

        // 5. 存储到数据库
        saveCompetitorAnalysisResult(markdownContent, sourceUrl, source, result);
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

        // 2. 尝试从 ## 附 部分提取链接
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("## 附") || line.contains("## 参考")) {
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
     * 检查竞品动态是否已存在
     */
    private boolean isCompetitorExists(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<CompetitorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(CompetitorInfo::getSources, sourceUrl);
        return competitorRepository.selectCount(wrapper) > 0;
    }

    /**
     * 保存竞品分析结果到数据库
     */
    @Transactional
    private void saveCompetitorAnalysisResult(String markdownContent, String sourceUrl, String source, CompetitorAnalysisResult result) {
        // 1. 查找或创建 CompetitorInfo
        CompetitorInfo competitorInfo = findOrCreateCompetitorInfo(sourceUrl);
        competitorInfo.setTitle(extractTitle(markdownContent));
        competitorInfo.setCompany(result.getCompany());
        competitorInfo.setType(result.getType());
        competitorInfo.setImportance(result.getImportance());
        competitorInfo.setRelevance(result.getRelevance());
        competitorInfo.setKeyPoints(convertKeyPointsToJson(result.getKeyPoints()));
        competitorInfo.setCaptureTime(LocalDateTime.now());
        competitorInfo.setTags(convertTagsToJson(result.getTags()));
        competitorInfo.setContent(markdownContent);
        competitorInfo.setSources(convertSourcesToJson(sourceUrl));
        competitorInfo.setSummary(result.getSummary());
        competitorInfo.setMarketImpact(result.getMarketImpact());
        competitorInfo.setRelatedInfo(result.getCompetitiveAnalysis());
        competitorInfo.setUpdateTime(LocalDateTime.now());

        competitorRepository.updateById(competitorInfo);
        Long competitorId = competitorInfo.getId();

        // 2. 保存 CompetitorAnalysis（存储详细分析结果）
        CompetitorAnalysis competitorAnalysis = new CompetitorAnalysis();
        competitorAnalysis.setCompetitorId(competitorId);
        competitorAnalysis.setImportance(result.getImportance());
        competitorAnalysis.setRelevance(result.getRelevance());
        competitorAnalysis.setKeyPoints(convertKeyPointsToJson(result.getKeyPoints()));
        competitorAnalysis.setMarketImpact(result.getMarketImpact());
        competitorAnalysis.setCompetitiveAnalysis(result.getCompetitiveAnalysis());
        competitorAnalysis.setOurSuggestions(convertSuggestionsToJson(result.getOurSuggestions()));

        // content 字段保留作为整合的摘要
        StringBuilder contentBuilder = new StringBuilder();
        if (result.getMarketImpact() != null) {
            contentBuilder.append("## 市场影响\n\n").append(result.getMarketImpact()).append("\n\n");
        }
        if (result.getCompetitiveAnalysis() != null) {
            contentBuilder.append("## 竞争态势\n\n").append(result.getCompetitiveAnalysis()).append("\n\n");
        }
        if (result.getOurSuggestions() != null && !result.getOurSuggestions().isEmpty()) {
            contentBuilder.append("## 应对建议\n\n");
            for (CompetitorAnalysisResult.Suggestion suggestion : result.getOurSuggestions()) {
                contentBuilder.append("- **建议**: ").append(suggestion.getSuggestion()).append("\n");
                contentBuilder.append("  **理由**: ").append(suggestion.getReason()).append("\n\n");
            }
        }
        competitorAnalysis.setContent(contentBuilder.toString());

        competitorAnalysis.setSortOrder(1);
        competitorAnalysis.setUpdateTime(LocalDateTime.now());

        // 查找或创建 CompetitorAnalysis（通过 competitorId 和 sortOrder）
        LambdaQueryWrapper<CompetitorAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
        analysisWrapper.eq(CompetitorAnalysis::getCompetitorId, competitorId)
                .eq(CompetitorAnalysis::getSortOrder, 1);
        CompetitorAnalysis existingAnalysis = competitorAnalysisRepository.selectOne(analysisWrapper);

        if (existingAnalysis != null) {
            // 更新已存在的记录
            existingAnalysis.setImportance(competitorAnalysis.getImportance());
            existingAnalysis.setRelevance(competitorAnalysis.getRelevance());
            existingAnalysis.setKeyPoints(competitorAnalysis.getKeyPoints());
            existingAnalysis.setMarketImpact(competitorAnalysis.getMarketImpact());
            existingAnalysis.setCompetitiveAnalysis(competitorAnalysis.getCompetitiveAnalysis());
            existingAnalysis.setOurSuggestions(competitorAnalysis.getOurSuggestions());
            existingAnalysis.setContent(competitorAnalysis.getContent());
            existingAnalysis.setUpdateTime(LocalDateTime.now());
            competitorAnalysisRepository.updateById(existingAnalysis);
        } else {
            // 创建新记录
            competitorAnalysis.setCreateTime(LocalDateTime.now());
            competitorAnalysisRepository.insert(competitorAnalysis);
        }

        log.info("竞品分析结果已保存: competitorId={}", competitorId);
    }

    /**
     * 根据 sourceUrl 查找或创建 CompetitorInfo
     */
    private CompetitorInfo findOrCreateCompetitorInfo(String sourceUrl) {
        if (sourceUrl != null && !sourceUrl.isEmpty()) {
            LambdaQueryWrapper<CompetitorInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.like(CompetitorInfo::getSources, "%" + sourceUrl + "%");
            CompetitorInfo existing = competitorRepository.selectOne(queryWrapper);
            if (existing != null) {
                log.info("找到已存在的竞品记录，将更新: id={}, sourceUrl={}", existing.getId(), sourceUrl);
                return existing;
            }
        }

        // 创建新记录
        CompetitorInfo newInfo = new CompetitorInfo();
        newInfo.setCreateTime(LocalDateTime.now());
        return newInfo;
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
     * 将 tags 列表转换为 JSON 字符串
     */
    private String convertTagsToJson(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags != null ? tags : new ArrayList<>());
        } catch (Exception e) {
            log.error("转换 tags 为 JSON 失败", e);
            return "[]";
        }
    }

    /**
     * 将 keyPoints 列表转换为 JSON 字符串
     */
    private String convertKeyPointsToJson(List<String> keyPoints) {
        try {
            return objectMapper.writeValueAsString(keyPoints != null ? keyPoints : new ArrayList<>());
        } catch (Exception e) {
            log.error("转换 keyPoints 为 JSON 失败", e);
            return "[]";
        }
    }

    /**
     * 将 suggestions 列表转换为 JSON 字符串
     */
    private String convertSuggestionsToJson(List<CompetitorAnalysisResult.Suggestion> suggestions) {
        try {
            return objectMapper.writeValueAsString(suggestions != null ? suggestions : new ArrayList<>());
        } catch (Exception e) {
            log.error("转换 suggestions 为 JSON 失败", e);
            return "[]";
        }
    }

    /**
     * 将 sourceUrl 转换为 JSON 数组格式
     */
    private String convertSourcesToJson(String sourceUrl) {
        try {
            if (sourceUrl == null || sourceUrl.isEmpty()) {
                return "[]";
            }
            // 创建包含单个 URL 的 JSON 数组
            List<Map<String, String>> sourcesList = new ArrayList<>();
            Map<String, String> urlMap = new HashMap<>();
            urlMap.put("url", sourceUrl);
            sourcesList.add(urlMap);
            return objectMapper.writeValueAsString(sourcesList);
        } catch (Exception e) {
            log.error("转换 sources 为 JSON 失败", e);
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
}
