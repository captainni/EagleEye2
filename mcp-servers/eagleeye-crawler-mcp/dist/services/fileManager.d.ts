export interface ArticleMetadata {
    title: string;
    url: string;
    category: 'policy' | 'competitor';
    publishTime?: string;
    author?: string;
    tags?: string[];
    content: string;
    wordCount: number;
}
export interface BatchMetadata {
    batchId: string;
    category: 'policy' | 'competitor';
    sourceName: string;
    listUrl: string;
    crawlTime: string;
    articleCount: number;
    totalWords: number;
    articles: {
        filename: string;
        title: string;
        url: string;
        category: 'policy' | 'competitor';
        publishTime?: string;
        wordCount: number;
    }[];
    errors: string[];
}
export declare class FileManager {
    private outputDir;
    constructor(outputDir: string);
    /**
     * 创建批次文件夹
     */
    createBatchFolder(batchName: string): Promise<string>;
    /**
     * 保存文章内容为 Markdown 文件
     */
    saveArticle(batchPath: string, index: number, title: string, category: 'policy' | 'competitor', metadata: ArticleMetadata): Promise<string>;
    /**
     * 保存批次元数据
     */
    saveMetadata(batchPath: string, metadata: BatchMetadata): Promise<void>;
    /**
     * 保存汇总报告
     */
    saveSummary(batchPath: string, summary: string): Promise<void>;
    /**
     * 生成汇总报告内容
     */
    generateSummary(metadata: BatchMetadata): string;
    /**
     * 清理文件名
     */
    private sanitizeFilename;
    /**
     * 生成 Markdown 内容
     */
    private generateMarkdown;
}
