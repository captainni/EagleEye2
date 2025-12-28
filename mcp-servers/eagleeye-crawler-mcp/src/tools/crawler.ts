import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import * as z from 'zod';
import * as dateFns from 'date-fns';
import { FileManager, type BatchMetadata, type ArticleMetadata } from '../services/fileManager.js';

export class CrawlerTool {
  private fileManager: FileManager;

  constructor(private server: McpServer, outputDir: string = '/home/captain/projects/EagleEye2/crawl_files') {
    this.fileManager = new FileManager(outputDir);
  }

  register(): void {
    this.server.registerTool(
      'crawl_articles',
      {
        title: '爬取金融资讯文章',
        description: '从列表页面爬取文章，生成结构化 Markdown 文件',
        inputSchema: {
          listUrl: z.string().url().describe('列表页面 URL'),
          sourceName: z
            .string()
            .regex(/^[a-z0-9_]+$/, '只能包含小写字母、数字和下划线')
            .describe('来源标识'),
          maxArticles: z.number().int().min(1).max(20).default(3).describe('最大文章数'),
          outputDir: z.string().default('/home/captain/projects/EagleEye2/crawl_files').describe('输出目录')
        },
        outputSchema: {
          batchPath: z.string(),
          articleCount: z.number(),
          articles: z.array(
            z.object({
              filename: z.string(),
              title: z.string(),
              url: z.string(),
              category: z.enum(['policy', 'competitor'])
            })
          )
        }
      },
      async (args, _extra) => {
        return await this.execute(args);
      }
    );
  }

  private async execute(args: {
    listUrl: string;
    sourceName: string;
    maxArticles?: number;
    outputDir?: string;
  }) {
    const { listUrl, sourceName, maxArticles = 3, outputDir } = args;

    // 更新 fileManager 的输出目录
    if (outputDir) {
      this.fileManager = new FileManager(outputDir);
    }

    // 生成批次名称（不带分类）
    const timestamp = dateFns.format(new Date(), 'yyyyMMdd_HHmmss');
    const batchName = `${timestamp}_${sourceName}`;
    const batchPath = await this.fileManager.createBatchFolder(batchName);

    // 注意：实际的爬取逻辑需要调用 webReader MCP tool
    // 这里返回一个占位响应，说明需要由调用者（Claude）来完成实际的爬取工作

    const output = {
      batchPath,
      articleCount: 0,
      articles: [] as Array<{
        filename: string;
        title: string;
        url: string;
        category: 'policy' | 'competitor';
      }>
    };

    return {
      content: [
        {
          type: 'text' as const,
          text: `批次文件夹已创建：${batchPath}\n\n请使用 webReader MCP tool 获取列表页面内容，然后分析提取文章链接，最后批量爬取文章并保存。`
        }
      ],
      structuredContent: output
    };
  }

  /**
   * 保存单篇文章（供外部调用）
   */
  async saveArticle(
    batchPath: string,
    index: number,
    metadata: ArticleMetadata
  ): Promise<string> {
    return await this.fileManager.saveArticle(batchPath, index, metadata.title, metadata.category, metadata);
  }

  /**
   * 保存批次元数据（供外部调用）
   */
  async saveMetadata(batchPath: string, metadata: BatchMetadata): Promise<void> {
    await this.fileManager.saveMetadata(batchPath, metadata);
  }

  /**
   * 生成并保存汇总报告（供外部调用）
   */
  async saveSummary(batchPath: string, metadata: BatchMetadata): Promise<void> {
    const summary = this.fileManager.generateSummary(metadata);
    await this.fileManager.saveSummary(batchPath, summary);
  }

  /**
   * 获取批次路径
   */
  getBatchPath(): string {
    return this.fileManager['outputDir'];
  }
}
