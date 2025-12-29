package com.eagleeye.service.competitor;

import com.eagleeye.model.dto.CompetitorAnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 竞品分析器服务
 * 通过 proxy-service 调用 Claude Code CLI 执行 competitor-analyzer skill
 *
 * @author eagleeye
 */
@Service
public class CompetitorAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(CompetitorAnalyzer.class);

    @Value("${proxy.service.url:http://localhost:8000}")
    private String proxyServiceUrl;

    private final RestTemplate restTemplate;

    public CompetitorAnalyzer() {
        this.restTemplate = new RestTemplate();
        // 配置消息转换器使用 UTF-8
        restTemplate.setMessageConverters(java.util.List.of(
            new StringHttpMessageConverter(StandardCharsets.UTF_8),
            new MappingJackson2HttpMessageConverter(),
            new ByteArrayHttpMessageConverter()
        ));
    }

    /**
     * 分析竞品文章
     *
     * @param markdownContent 竞品文章的 Markdown 内容
     * @return 分析结果
     * @throws RuntimeException 分析失败时抛出异常
     */
    public CompetitorAnalysisResult analyze(String markdownContent) {
        return analyze(markdownContent, null);
    }

    /**
     * 分析竞品文章（带产品上下文）
     *
     * @param markdownContent 竞品文章的 Markdown 内容
     * @param userProducts 用户产品列表的 JSON 字符串格式
     * @return 分析结果
     * @throws RuntimeException 分析失败时抛出异常
     */
    public CompetitorAnalysisResult analyze(String markdownContent, String userProducts) {
        log.debug("开始分析竞品文章，内容长度: {}", markdownContent != null ? markdownContent.length() : 0);

        // 构建请求
        String url = proxyServiceUrl + "/analyze-competitor";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));

        // 构建请求体
        CompetitorAnalysisRequest request = new CompetitorAnalysisRequest(markdownContent, userProducts);

        HttpEntity<CompetitorAnalysisRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            // 发送请求
            log.debug("发送竞品分析请求到 proxy-service: {}", url);
            ResponseEntity<CompetitorAnalysisResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    CompetitorAnalysisResult.class
            );

            CompetitorAnalysisResult result = response.getBody();
            if (result != null) {
                log.info("竞品分析成功: company={}, type={}, importance={}, relevance={}, keyPoints数量={}",
                        result.getCompany(), result.getType(), result.getImportance(), result.getRelevance(),
                        result.getKeyPoints() != null ? result.getKeyPoints().size() : 0);
            } else {
                log.warn("竞品分析返回结果为空");
            }

            return result;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("代理服务返回错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("竞品分析失败: " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("无法连接到代理服务: {}", url, e);
            throw new RuntimeException("无法连接到代理服务，请确认 proxy-service 已启动", e);
        } catch (Exception e) {
            log.error("竞品分析发生未知错误", e);
            throw new RuntimeException("竞品分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 请求体 DTO
     */
    private static class CompetitorAnalysisRequest {
        private final String content;
        private final String userProducts;

        public CompetitorAnalysisRequest(String content, String userProducts) {
            this.content = content;
            this.userProducts = userProducts;
        }

        public String getContent() {
            return content;
        }

        public String getUserProducts() {
            return userProducts;
        }
    }
}
