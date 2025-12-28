import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { type BatchMetadata, type ArticleMetadata } from '../services/fileManager.js';
export declare class CrawlerTool {
    private server;
    private fileManager;
    constructor(server: McpServer, outputDir?: string);
    register(): void;
    private execute;
    /**
     * 保存单篇文章（供外部调用）
     */
    saveArticle(batchPath: string, index: number, metadata: ArticleMetadata): Promise<string>;
    /**
     * 保存批次元数据（供外部调用）
     */
    saveMetadata(batchPath: string, metadata: BatchMetadata): Promise<void>;
    /**
     * 生成并保存汇总报告（供外部调用）
     */
    saveSummary(batchPath: string, metadata: BatchMetadata): Promise<void>;
    /**
     * 获取批次路径
     */
    getBatchPath(): string;
}
