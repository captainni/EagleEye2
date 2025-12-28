import fs from 'fs/promises';
import path from 'path';

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

export class FileManager {
  private outputDir: string;

  constructor(outputDir: string) {
    this.outputDir = outputDir;
  }

  /**
   * 创建批次文件夹
   */
  async createBatchFolder(batchName: string): Promise<string> {
    const batchPath = path.join(this.outputDir, batchName);
    await fs.mkdir(batchPath, { recursive: true });
    return batchPath;
  }

  /**
   * 保存文章内容为 Markdown 文件
   */
  async saveArticle(
    batchPath: string,
    index: number,
    title: string,
    category: 'policy' | 'competitor',
    metadata: ArticleMetadata
  ): Promise<string> {
    const sanitizedTitle = this.sanitizeFilename(title);
    const categoryPrefix = category === 'policy' ? '政策' : '竞品';
    const filename = `${String(index).padStart(2, '0')}_${categoryPrefix}_${sanitizedTitle}.md`;
    const filePath = path.join(batchPath, filename);

    const markdown = this.generateMarkdown(title, category, metadata);
    await fs.writeFile(filePath, markdown, 'utf-8');
    return filename;
  }

  /**
   * 保存批次元数据
   */
  async saveMetadata(batchPath: string, metadata: BatchMetadata): Promise<void> {
    const metadataPath = path.join(batchPath, 'metadata.json');
    await fs.writeFile(metadataPath, JSON.stringify(metadata, null, 2), 'utf-8');
  }

  /**
   * 保存汇总报告
   */
  async saveSummary(batchPath: string, summary: string): Promise<void> {
    const summaryPath = path.join(batchPath, 'summary.md');
    await fs.writeFile(summaryPath, summary, 'utf-8');
  }

  /**
   * 生成汇总报告内容
   */
  generateSummary(metadata: BatchMetadata): string {
    const categoryLabel = metadata.category === 'policy' ? '政策' : '竞品';
    const avgWords = metadata.articleCount > 0 ? Math.round(metadata.totalWords / metadata.articleCount) : 0;

    let summary = `# 爬取批次汇总\n\n`;
    summary += `**批次信息**\n`;
    summary += `- 批次ID：${metadata.batchId}\n`;
    summary += `- 分类：${categoryLabel}\n`;
    summary += `- 来源：${metadata.sourceName}\n`;
    summary += `- 列表URL：${metadata.listUrl}\n`;
    summary += `- 爬取时间：${metadata.crawlTime}\n`;
    summary += `- 文章数量：${metadata.articleCount}\n\n`;

    summary += `## 文章列表\n\n`;
    metadata.articles.forEach((article, idx) => {
      summary += `${idx + 1}. [${article.title}](./${article.filename}) (${article.wordCount}字)\n`;
    });

    summary += `\n## 统计信息\n\n`;
    summary += `- 总字数：${metadata.totalWords}\n`;
    summary += `- 平均字数：${avgWords}\n`;

    if (metadata.errors.length > 0) {
      summary += `\n## 错误信息\n\n`;
      metadata.errors.forEach((error) => {
        summary += `- ${error}\n`;
      });
    }

    return summary;
  }

  /**
   * 清理文件名
   */
  private sanitizeFilename(filename: string): string {
    return filename
      .replace(/[<>:"/\\|?*]/g, '')
      .replace(/\s+/g, '_')
      .substring(0, 50);
  }

  /**
   * 生成 Markdown 内容
   */
  private generateMarkdown(
    title: string,
    category: 'policy' | 'competitor',
    metadata: ArticleMetadata
  ): string {
    const categoryLabel = category === 'policy' ? '政策' : '竞品';
    const tags = metadata.tags?.map((t) => `#${t}`).join(' ') || '';

    let markdown = `# ${title}\n\n`;
    markdown += `**元数据**\n`;
    markdown += `- 分类：${categoryLabel}\n`;
    markdown += `- 来源：${metadata.author || '未知'}\n`;
    if (metadata.publishTime) {
      markdown += `- 发布时间：${metadata.publishTime}\n`;
    }
    if (metadata.author) {
      markdown += `- 作者：${metadata.author}\n`;
    }
    if (tags) {
      markdown += `- 标签：${tags}\n`;
    }
    markdown += `\n## 正文\n\n`;
    markdown += metadata.content;

    return markdown;
  }
}
