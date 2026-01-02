package com.eagleeye.util;

/**
 * 爬虫工具类
 */
public class CrawlerUtil {

    /**
     * 从 URL 提取 sourceName
     * 例如: https://bank.eastmoney.com/a/czzyh.html -> bank_eastmoney
     * 例如: https://finance.eastmoney.com/a/20251226.html -> finance_eastmoney
     * 例如: https://bank.jrj.com.cn/ -> bank_jrj
     * 例如: https://www.nfra.gov.cn/ -> www_nfra
     */
    public static String extractSourceNameFromUrl(String url) {
        try {
            // 移除协议前缀
            String cleanUrl = url.replaceFirst("^https?://", "");

            // 提取域名和路径
            String[] parts = cleanUrl.split("/", 2);
            if (parts.length == 0) {
                return "unknown";
            }

            String domain = parts[0]; // bank.jrj.com.cn

            // 处理域名：提取子域名和主域名
            String[] domainParts = domain.split("\\.");
            if (domainParts.length < 2) {
                return "unknown";
            }

            // 提取有意义的域名部分
            // 跳过 www，跳过顶级域名 (com, cn, net, org等)
            String firstPart = null;
            String secondPart = null;

            for (int i = 0; i < domainParts.length; i++) {
                String part = domainParts[i];
                // 跳过 www 和顶级域名
                if (part.equals("www") || isTLD(part)) {
                    continue;
                }
                if (firstPart == null) {
                    firstPart = part;
                } else if (secondPart == null) {
                    secondPart = part;
                    break; // 找到两个有效部分就停止
                }
            }

            // 构建结果
            if (firstPart != null && secondPart != null) {
                return firstPart + "_" + secondPart;
            } else if (firstPart != null) {
                return firstPart;
            } else {
                return "unknown";
            }

        } catch (Exception e) {
            // 静默处理错误，返回默认值
            return "unknown_source";
        }
    }

    /**
     * 判断是否为顶级域名
     */
    private static boolean isTLD(String part) {
        // 常见顶级域名列表
        return part.matches("com|cn|net|org|gov|edu|io|co|uk|us|jp|kr|de|fr|info|biz");
    }
}
