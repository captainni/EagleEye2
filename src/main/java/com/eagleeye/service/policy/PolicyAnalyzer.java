package com.eagleeye.service.policy;

import com.eagleeye.model.dto.AnalysisResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * 政策分析器服务
 * 通过 proxy-service 调用 Claude Code CLI 执行 policy-analyzer skill
 *
 * @author eagleeye
 */
@Service
public class PolicyAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(PolicyAnalyzer.class);

    @Value("${proxy.service.url:http://localhost:8000}")
    private String proxyServiceUrl;

    private final RestTemplate restTemplate;

    public PolicyAnalyzer() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * 分析政策文章
     *
     * @param markdownContent 政策文章的 Markdown 内容
     * @return 分析结果
     * @throws RuntimeException 分析失败时抛出异常
     */
    public AnalysisResult analyze(String markdownContent) {
        return analyze(markdownContent, null);
    }

    /**
     * 分析政策文章（带产品上下文）
     *
     * @param markdownContent 政策文章的 Markdown 内容
     * @param products 用户产品列表的 JSON 字符串格式
     * @return 分析结果
     * @throws RuntimeException 分析失败时抛出异常
     */
    public AnalysisResult analyze(String markdownContent, String products) {
        log.debug("开始分析政策文章，内容长度: {}", markdownContent != null ? markdownContent.length() : 0);

        // 构建请求
        String url = proxyServiceUrl + "/analyze-policy";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体（新增 products 参数）
        PolicyAnalysisRequest request = new PolicyAnalysisRequest(markdownContent, products);

        HttpEntity<PolicyAnalysisRequest> httpEntity = new HttpEntity<>(request, headers);

        try {
            // 发送请求
            log.debug("发送分析请求到 proxy-service: {}", url);
            ResponseEntity<AnalysisResult> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    AnalysisResult.class
            );

            AnalysisResult result = response.getBody();
            if (result != null) {
                log.info("政策分析成功: policyType={}, importance={}, relevance={}, keyPoints数量={}",
                        result.getPolicyType(), result.getImportance(), result.getRelevance(),
                        result.getKeyPoints() != null ? result.getKeyPoints().size() : 0);
            } else {
                log.warn("政策分析返回结果为空");
            }

            return result;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("代理服务返回错误: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("政策分析失败: " + e.getResponseBodyAsString(), e);
        } catch (ResourceAccessException e) {
            log.error("无法连接到代理服务: {}", url, e);
            throw new RuntimeException("无法连接到代理服务，请确认 proxy-service 已启动", e);
        } catch (Exception e) {
            log.error("政策分析发生未知错误", e);
            throw new RuntimeException("政策分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 请求体 DTO
     */
    private static class PolicyAnalysisRequest {
        private final String content;
        private final String products;

        public PolicyAnalysisRequest(String content, String products) {
            this.content = content;
            this.products = products;
        }

        public String getContent() {
            return content;
        }

        public String getProducts() {
            return products;
        }
    }
}
