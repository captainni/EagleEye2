// TypeScript types for Crawler Management

// Single field definition for CSS Schema
export interface CssSchemaField {
  name: string;
  selector: string;
  type: 'text' | 'href' | 'html'; // Example types, adjust as needed
}

// CSS Extraction Schema definition
export interface CssSchema {
  baseSelector?: string; // Optional base selector for elements
  fields: CssSchemaField[];
}

// Represents a single crawler configuration
export interface CrawlerConfig {
  id: string;
  name: string;
  crawlerService: 'legacy' | 'eagleeye';
  strategyType: 'css' | 'llm' | 'none'; // 'none' for just fetching markdown
  schedule: string; // Could be cron string or descriptive like "每15分钟"
  status: 'enabled' | 'disabled';
  urls: string; // Multiline URLs stored as a single string, separated by newlines
  schemaCss?: string; // JSON string representation of CssSchema
  schemaLlm?: string; // JSON string representation of Pydantic/JSON Schema
  llmProvider?: string; // e.g., 'openai/gpt-4o-mini'
  llmInstruction?: string; // Instructions for the LLM
  crawlDepth?: number; // 0 means only start URLs
  advancedConfig?: string; // JSON string for advanced Crawl4AI config overrides
  lastExecutionTime?: string; // ISO date string or descriptive like "2024-07-26 10:30"
  lastExecutionStatus?: 'success' | 'failure'; // Optional status of the last run
}

// Represents a log entry for a crawler task execution
export interface CrawlerTaskLog {
  id: string;
  configName: string;
  targetUrl: string;
  startTime: string; // ISO date string
  endTime: string; // ISO date string
  status: 'success' | 'failure';
  errorMessage?: string; // Only present on failure
}

// Represents a user suggestion for a new source
export interface UserSuggestion {
  id: number;
  userId: number;
  suggestionType: string; // 'NEW_SOURCE', 'CONFIG_ERROR' 等
  sourceName: string;
  suggestionUrl: string;
  status: string; // 'PENDING', 'APPROVED', 'REJECTED'
  remarks: string; // 建议理由
  handlerId: number | null;
  handledAt: string | null;
  createdAt: string;
}

// Interface for query parameters (example for configs, adapt for others)
export interface ConfigQueryParam {
  page?: number;
  pageSize?: number;
  name?: string;
  crawlerService?: 'legacy' | 'eagleeye';
  status?: 'enabled' | 'disabled';
}

// Interface for query parameters for task logs
export interface TaskLogQueryParam {
  pageNum?: number;
  pageSize?: number;
  startTime?: string; // ISO date string
  endTime?: string; // ISO date string
  status?: 'success' | 'failure';
  configName?: string;
}

// Interface for query parameters for user suggestions
export interface SuggestionQueryParam {
  pageNum?: number;
  pageSize?: number;
  status?: string;
  type?: string;
}

// Updated content for web/src/types/admin/crawler.ts

// 对应后端 CommonResult<CommonPage<T>> 中的 CommonPage 部分
export interface PaginatedResult<T> {
  list: T[];       // 数据列表
  total: number;     // 总记录数
  pageNum: number;   // 当前页码
  pageSize: number;  // 每页数量
  pages: number;     // 总页数
}

// 对应后端 CrawlerConfigQueryDTO
export interface CrawlerConfigQueryParam {
  keyword?: string;
  crawlerService?: 'legacy' | 'eagleeye';
  isActive?: boolean;
  pageNum?: number;
  pageSize?: number;
}

// 对应后端 CrawlerConfigVO (列表)
export interface CrawlerConfigVO {
  configId: number;
  targetName: string;
  crawlerService: 'legacy' | 'eagleeye';
  resultPath?: string;
  triggerSchedule?: string;
  extractionStrategyType: 'css' | 'llm';
  isActive: boolean;
  createTime: string; // ISO Date string
  updateTime: string; // ISO Date string
}

// 对应后端 CrawlerConfigDetailVO (详情)
export interface CrawlerConfigDetailVO {
  configId: number;
  targetName: string;
  crawlerService: 'legacy' | 'eagleeye';
  resultPath?: string;
  sourceUrls: string; // JSON string, needs parsing on frontend if needed
  crawlDepth?: number;
  triggerSchedule?: string;
  extractionStrategyType: 'css' | 'llm';
  extractionSchema: string; // JSON string
  llmInstruction?: string;
  llmProviderConfig?: string; // JSON string
  crawl4aiConfigOverride?: string; // JSON string
  isActive: boolean;
  createTime: string; // ISO Date string
  updateTime: string; // ISO Date string
}

// 对应后端 CrawlerConfigCreateDTO
// 注意: 后端 DTO 使用 JSON 字符串表示复杂对象，前端如果需要操作对象，
// 在调用 API 前需要 JSON.stringify()
export interface CrawlerConfigCreateData {
  targetName: string;
  crawlerService: 'legacy' | 'eagleeye';
  sourceUrls: string; // JSON array string expected e.g., '["url1", "url2"]'
  crawlDepth?: number;
  triggerSchedule?: string;
  extractionStrategyType: 'css' | 'llm';
  extractionSchema: string; // JSON object string expected
  llmInstruction?: string;
  llmProviderConfig?: string; // JSON object string expected
  crawl4aiConfigOverride?: string; // JSON object string expected
  isActive?: boolean;
}

// 对应后端 CrawlerConfigUpdateDTO
export interface CrawlerConfigUpdateData {
  targetName?: string;
  crawlerService?: 'legacy' | 'eagleeye';
  sourceUrls?: string; // JSON array string expected
  crawlDepth?: number;
  triggerSchedule?: string;
  extractionStrategyType?: 'css' | 'llm';
  extractionSchema?: string; // JSON object string expected
  llmInstruction?: string;
  llmProviderConfig?: string; // JSON object string expected
  crawl4aiConfigOverride?: string; // JSON object string expected
  isActive?: boolean;
}

// --- Removed old/unused types ---
// export interface CssSchemaField { ... }
// export interface CssSchema { ... }
// export interface CrawlerConfig { ... } // Replaced by VOs/DTOs above
// export interface ConfigQueryParam { ... } // Replaced above

// --- Task Monitoring API Types (Keep as is for now) ---
export interface CrawlerTaskLog {
  id: string;
  configName: string;
  targetUrl: string;
  startTime: string; // ISO date string
  endTime: string; // ISO date string
  status: 'success' | 'failure';
  errorMessage?: string; // Only present on failure
}
export interface TaskLogQueryParam {
  pageNum?: number;
  pageSize?: number;
  startTime?: string; // ISO date string
  endTime?: string; // ISO date string
  status?: 'success' | 'failure';
  configName?: string;
}

// --- User Suggestions API Types (Keep as is for now) ---
export interface UserSuggestion {
  id: number;
  userId: number;
  suggestionType: string; // 'NEW_SOURCE', 'CONFIG_ERROR' 等
  sourceName: string;
  suggestionUrl: string;
  status: string; // 'PENDING', 'APPROVED', 'REJECTED'
  remarks: string; // 建议理由
  handlerId: number | null;
  handledAt: string | null;
  createdAt: string;
}
export interface SuggestionQueryParam {
  pageNum?: number;
  pageSize?: number;
  status?: string;
  type?: string;
}
