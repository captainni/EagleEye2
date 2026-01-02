import apiClient from '@/utils/request';
import type {
  CrawlerConfigQueryParam,
  PaginatedResult,
  CrawlerConfigVO,
  CrawlerConfigDetailVO,
  CrawlerConfigCreateData,
  CrawlerConfigUpdateData,
  CrawlerTaskLog,
  UserSuggestion,
  TaskLogQueryParam,
  SuggestionQueryParam,
  PolicyAnalysisTriggerResult,
  AnalysisResult
} from '@/types/admin/crawler';
import { safeJSONStringify as _safeJSONStringify } from '@/utils/jsonUtils';

/**
 * 安全地将对象转换为JSON字符串
 * 防止空值错误和格式问题
 */
export const safeJSONStringify = _safeJSONStringify;

// --- Config Management API ---

// GET /v1/admin/crawler/configs (List with pagination)
export async function getCrawlerConfigs(params: CrawlerConfigQueryParam): Promise<PaginatedResult<CrawlerConfigVO>> {
  return apiClient.get('/v1/admin/crawler/configs', { params });
}

// GET /v1/admin/crawler/configs/{configId} (Get details)
export async function getCrawlerConfigDetail(configId: number): Promise<CrawlerConfigDetailVO> {
  return apiClient.get(`/v1/admin/crawler/configs/${configId}`);
}

// POST /v1/admin/crawler/configs (Create)
// Backend expects Long for ID, so CommonResult<Long> -> Promise<number>
export async function createCrawlerConfig(data: CrawlerConfigCreateData): Promise<number> {
  // 预处理数据，避免修改原始对象
  const processedData = { ...data };
  
  // 明确移除这些字段，避免后端API错误
  if ('configId' in processedData) delete processedData.configId;
  if ('createTime' in processedData) delete processedData.createTime;
  if ('updateTime' in processedData) delete processedData.updateTime;
  
  // JSON字段安全处理
  if ('extractionSchema' in processedData) {
    processedData.extractionSchema = safeJSONStringify(processedData.extractionSchema);
  }
  
  if ('llmProviderConfig' in processedData) {
    processedData.llmProviderConfig = safeJSONStringify(processedData.llmProviderConfig);
  }
  
  if ('crawl4aiConfigOverride' in processedData) {
    processedData.crawl4aiConfigOverride = safeJSONStringify(processedData.crawl4aiConfigOverride);
  }
  
  // 处理 sourceUrls
  if (typeof processedData.sourceUrls === 'string') {
    try {
      // 移除URL中可能导致问题的特殊字符 (如控制字符等)
      const cleanedUrl = processedData.sourceUrls
        .replace(/[\u0000-\u001F\u007F-\u009F]/g, '') // 移除控制字符
        .trim();
        
      processedData.sourceUrls = cleanedUrl;
    } catch (error) {
      console.error('Failed to clean sourceUrls:', error);
    }
  }
  
  return apiClient.post('/v1/admin/crawler/configs', processedData);
}

// PUT /v1/admin/crawler/configs/{id} (Update)
export async function updateCrawlerConfig(id: number, data: CrawlerConfigUpdateData): Promise<void> {
  // 预处理数据，避免修改原始对象
  const processedData = { ...data };
  
  // 明确移除这些字段，避免后端API错误
  if ('configId' in processedData) delete processedData.configId;
  if ('createTime' in processedData) delete processedData.createTime;
  if ('updateTime' in processedData) delete processedData.updateTime;
  
  // JSON字段安全处理
  if ('extractionSchema' in processedData) {
    processedData.extractionSchema = safeJSONStringify(processedData.extractionSchema);
  }
  
  if ('llmProviderConfig' in processedData) {
    processedData.llmProviderConfig = safeJSONStringify(processedData.llmProviderConfig);
  }
  
  if ('crawl4aiConfigOverride' in processedData) {
    processedData.crawl4aiConfigOverride = safeJSONStringify(processedData.crawl4aiConfigOverride);
  }
  
  // 处理 sourceUrls
  if (typeof processedData.sourceUrls === 'string') {
    try {
      // 移除URL中可能导致问题的特殊字符 (如控制字符等)
      const cleanedUrl = processedData.sourceUrls
        .replace(/[\u0000-\u001F\u007F-\u009F]/g, '') // 移除控制字符
        .trim();
        
      processedData.sourceUrls = cleanedUrl;
    } catch (error) {
      console.error('Failed to clean sourceUrls:', error);
    }
  }
  
  return apiClient.put(`/v1/admin/crawler/configs/${id}`, processedData);
}

// DELETE /v1/admin/crawler/configs/{configId} (Delete)
// Backend returns CommonResult<String> -> Promise<string> (or void)
export async function deleteCrawlerConfig(configId: number): Promise<string> {
  return apiClient.delete(`/v1/admin/crawler/configs/${configId}`);
}

// PATCH /v1/admin/crawler/configs/{configId}/status (Update status)
// Backend returns CommonResult<String> -> Promise<string> (or void)
export async function updateCrawlerConfigStatus(configId: number, isActive: boolean): Promise<string> {
  // Pass isActive as request param
  return apiClient.patch(`/v1/admin/crawler/configs/${configId}/status`, null, { params: { isActive } });
}

// POST /v1/admin/crawler/configs/{configId}/trigger (Trigger manually)
// Backend returns CommonResult<TriggerResult> -> Promise<TriggerResult>
export interface TriggerResult {
  success: boolean;
  taskId?: string;
  message?: string;
}

export async function triggerCrawlerConfig(configId: number): Promise<TriggerResult> {
  return apiClient.post(`/v1/admin/crawler/configs/${configId}/trigger`);
}

// GET /v1/admin/crawler/tasks/{taskId}/status (Get task status)
export interface TaskStatus {
  taskId: string;
  configId: number;
  configName?: string;
  status: string;  // 'processing', 'success', 'failure'
  targetUrl?: string;
  batchPath?: string;
  articleCount?: number;
  startTime?: string;
  endTime?: string;
  errorMessage?: string;
}

export async function getTaskStatus(taskId: string): Promise<TaskStatus> {
  return apiClient.get(`/v1/admin/crawler/tasks/${taskId}/status`);
}

// --- Task Monitoring API ---

export function getCrawlerTaskLogs(params: TaskLogQueryParam): Promise<PaginatedResult<CrawlerTaskLog>> {
  return apiClient.get('/v1/admin/crawler/tasks', { params });
}

// --- User Suggestions API ---

export async function getUserSuggestions(params: SuggestionQueryParam): Promise<PaginatedResult<UserSuggestion>> {
  // 转换参数名以匹配后端接收的参数名
  const apiParams = {
    pageNum: params.pageNum,
    pageSize: params.pageSize,
    status: params.status,
    suggestionType: params.type,
  };
  
  // Assume backend returns PaginatedResult<UserSuggestion> directly after CommonResult unwrapping
  return apiClient.get('/v1/admin/crawler/suggestions', { params: apiParams });
}

export async function approveUserSuggestion(id: string, handlerId: number, remarks?: string): Promise<null> {
  const params: { handlerId: number; remarks?: string } = { handlerId };
  if (remarks) {
    params.remarks = remarks;
  }
  return apiClient.post(`/v1/admin/crawler/suggestions/${id}/approve`, null, {
    params // 将 handlerId 和 remarks 作为 RequestParam 发送
  });
}

export async function rejectUserSuggestion(id: string, handlerId: number, remarks: string): Promise<null> {
  return apiClient.post(`/v1/admin/crawler/suggestions/${id}/reject`, null, {
    params: { handlerId, remarks }
  });
}

// Note: We removed toggleCrawlerConfig as the backend uses PATCH with isActive param now.

// --- Policy Analysis API ---

// POST /v1/admin/crawler/tasks/{taskId}/analyze-policies
export async function triggerPolicyAnalysis(taskId: string): Promise<PolicyAnalysisTriggerResult> {
  return apiClient.post(`/v1/admin/crawler/tasks/${taskId}/analyze-policies`);
}

// POST /v1/admin/crawler/tasks/{taskId}/analyze-competitors
export async function triggerCompetitorAnalysis(taskId: string): Promise<PolicyAnalysisTriggerResult> {
  return apiClient.post(`/v1/admin/crawler/tasks/${taskId}/analyze-competitors`);
}

// GET /v1/admin/crawler/tasks/{taskId}/status (复用现有接口，新增返回分析状态)
export interface PolicyAnalysisTaskStatus extends TaskStatus {
  analysisStatus?: 'pending' | 'analyzing' | 'completed' | 'failed';
  analysisResult?: string;  // JSON 字符串，需要 JSON.parse() 解析
}

