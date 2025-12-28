#!/usr/bin/env node
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { StreamableHTTPServerTransport } from '@modelcontextprotocol/sdk/server/streamableHttp.js';
import express from 'express';
import { CrawlerTool } from './tools/crawler.js';

const PORT = parseInt(process.env.PORT || '3000');
const TRANSPORT = process.env.TRANSPORT || 'stdio';
const OUTPUT_DIR = process.env.OUTPUT_DIR || '/home/captain/projects/EagleEye2/crawl_files';

// 创建 MCP 服务器（官方 API）
const server = new McpServer({
  name: 'eagleeye-crawler-mcp',
  version: '1.0.0'
});

// 注册爬虫工具（使用官方 registerTool API）
const crawlerTool = new CrawlerTool(server, OUTPUT_DIR);
crawlerTool.register();

// stdio 传输模式
async function runStdio(): Promise<void> {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('EagleEye Crawler MCP server running via stdio');
}

// HTTP 传输模式
async function runHTTP(): Promise<void> {
  const app = express();
  app.use(express.json());

  app.post('/mcp', async (req, res) => {
    const transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: undefined,
      enableJsonResponse: true
    });

    res.on('close', () => transport.close());
    await server.connect(transport);
    await transport.handleRequest(req, res, req.body);
  });

  app.listen(PORT, () => {
    console.error(`MCP server running on http://localhost:${PORT}/mcp`);
  });
}

// 启动
if (TRANSPORT === 'http') {
  runHTTP().catch((error) => {
    console.error('Server error:', error);
    process.exit(1);
  });
} else {
  runStdio().catch((error) => {
    console.error('Server error:', error);
    process.exit(1);
  });
}
