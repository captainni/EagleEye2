<template>
  <el-dialog
    :model-value="visible"
    :title="isEditing ? '编辑爬虫配置' : '新增爬虫配置'"
    width="800px"
    :before-close="handleClose"
    custom-class="crawler-config-modal"
    :close-on-click-modal="false"
    append-to-body
  >
    <el-form
      ref="configFormRef"
      :model="formData"
      :rules="formRules"
      label-position="top"
      class="space-y-4 modal-body"
    >
      <!-- Basic Info -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="配置名称" prop="targetName">
            <el-input v-model="formData.targetName" placeholder="请输入配置名称"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="爬虫服务" prop="crawlerService">
            <el-select v-model="formData.crawlerService" placeholder="请选择爬虫服务" class="w-full">
              <el-option label="EagleEye (新)" value="eagleeye"></el-option>
              <el-option label="传统服务" value="legacy"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="起始 URL (每行一个)" prop="sourceUrls">
        <el-input
          v-model="formData.sourceUrls"
          type="textarea"
          :rows="3"
          placeholder="https://..."
        ></el-input>
      </el-form-item>
      <!-- 传统服务专用配置 -->
      <template v-if="formData.crawlerService === 'legacy'">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="执行计划 (Cron)" prop="triggerSchedule">
              <el-input v-model="formData.triggerSchedule" placeholder="例如: 0 0 * * * ? (每小时)"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="抓取深度 (可选)" prop="crawlDepth">
              <el-input-number v-model="formData.crawlDepth" :min="0" placeholder="0表示只抓取起始URL"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
      </template>

      <el-form-item label="是否启用" prop="isActive">
        <el-switch v-model="formData.isActive"></el-switch>
      </el-form-item>

      <!-- 传统服务专用：提取策略配置 -->
      <template v-if="formData.crawlerService === 'legacy'">
      <!-- Extraction Strategy -->
      <div class="border-t pt-4 mt-4">
        <h5 class="text-base font-medium text-gray-700 mb-3">提取策略</h5>
        <el-form-item label="策略类型" prop="extractionStrategyType">
          <el-select v-model="formData.extractionStrategyType" placeholder="请选择策略类型" class="w-full md:w-1/2">
            <el-option label="CSS 选择器" value="css"></el-option>
            <el-option label="LLM 模型" value="llm"></el-option>
          </el-select>
        </el-form-item>

        <!-- Unified Schema Field -->
        <el-form-item
          :label="formData.extractionStrategyType === 'css' ? '提取 Schema (JSON - CSS)' : '提取 Schema (Pydantic/JSON Schema)'"
          prop="extractionSchema"
          v-if="formData.extractionStrategyType === 'css' || formData.extractionStrategyType === 'llm'"
          class="w-full"
        >
          <div class="relative w-full">
            <el-input
              v-model="formData.extractionSchema"
              type="textarea"
              :rows="6"
              :placeholder="formData.extractionStrategyType === 'css' ? cssSchemaPlaceholder : jsonSchemaPlaceholder"
              class="font-mono text-xs w-full"
            ></el-input>
            <div class="absolute right-0 top-0 flex space-x-1 m-1">
              <el-button 
                type="primary" 
                size="small" 
                @click="formatJsonField('extractionSchema')"
                class="!rounded-button text-xs"
                :disabled="!formData.extractionSchema"
              >
                格式化
              </el-button>
            </div>
          </div>
          <p class="text-xs text-gray-400 mt-1">
            <el-link type="primary" @click="showJsonExample('extractionSchema')" class="text-xs">查看示例</el-link>
          </p>
        </el-form-item>

        <!-- LLM Specific Fields -->
        <div v-if="formData.extractionStrategyType === 'llm'" class="mt-3 space-y-3">
          <el-form-item label="LLM 模型配置 (JSON, 可选)" prop="llmProviderConfig" class="w-full">
            <div class="relative w-full">
              <el-input
                v-model="formData.llmProviderConfig"
                type="textarea"
                :rows="3"
                placeholder='{ "provider": "openai", "model": "gpt-4o-mini", /* "api_key_env": "MY_API_KEY" */ }'
                class="font-mono text-xs w-full"
              ></el-input>
              <div class="absolute right-0 top-0 flex space-x-1 m-1">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="formatJsonField('llmProviderConfig')"
                  class="!rounded-button text-xs"
                  :disabled="!formData.llmProviderConfig"
                >
                  格式化
                </el-button>
              </div>
            </div>
            <p class="text-xs text-gray-400 mt-1">
              <el-link type="primary" @click="showJsonExample('llmProviderConfig')" class="text-xs">查看示例</el-link>
            </p>
          </el-form-item>
          <el-form-item label="LLM 指令" prop="llmInstruction" class="w-full">
            <el-input
              v-model="formData.llmInstruction"
              type="textarea"
              :rows="3"
              placeholder="例如: Extract title and summary."
              class="w-full"
            ></el-input>
          </el-form-item>
        </div>
      </div>
      </template>

      <!-- 传统服务专用：Crawl4AI 配置 -->
      <template v-if="formData.crawlerService === 'legacy'">
      <!-- Crawl4AI Override -->
      <div class="border-t pt-4 mt-4">
        <el-form-item label="Crawl4AI 高级配置覆盖 (JSON, 可选)" prop="crawl4aiConfigOverride" class="w-full">
          <div class="relative w-full">
            <el-input
              v-model="formData.crawl4aiConfigOverride"
              type="textarea"
              :rows="4"
              placeholder='{ /* 例如: "wait_for": ".article-body" */ }'
              class="font-mono text-xs w-full"
            ></el-input>
            <div class="absolute right-0 top-0 flex space-x-1 m-1">
              <el-button 
                type="primary" 
                size="small" 
                @click="formatJsonField('crawl4aiConfigOverride')"
                class="!rounded-button text-xs"
                :disabled="!formData.crawl4aiConfigOverride"
              >
                格式化
              </el-button>
            </div>
          </div>
          <p class="text-xs text-gray-400 mt-1">
            用于覆盖 CrawlerRunConfig 的默认参数，请谨慎使用。
            <el-link type="primary" @click="showJsonExample('crawl4aiConfigOverride')" class="text-xs">查看示例</el-link>
          </p>
        </el-form-item>
      </div>
      </template>
    </el-form>
    <template #footer>
      <div class="modal-footer">
        <el-button @click="handleClose" class="!rounded-button">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="loading" class="!rounded-button">保存配置</el-button>
      </div>
    </template>
    
    <!-- JSON示例对话框 -->
    <el-dialog
      v-model="jsonExampleDialogVisible"
      :title="jsonExampleTitle"
      width="600px"
      append-to-body
      custom-class="json-example-dialog"
    >
      <pre class="json-example-content">{{ jsonExampleContent }}</pre>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="applyExample" type="primary" class="!rounded-button">应用此示例</el-button>
          <el-button @click="jsonExampleDialogVisible = false" class="!rounded-button">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick } from 'vue';
import type { PropType } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
// 导入新的JSON工具函数
import { formatJSON, validateJSON, safeJSONStringify, fixJSON } from '@/utils/jsonUtils';
// Import new types
import type {
    CrawlerConfigDetailVO,
    CrawlerConfigCreateData,
    CrawlerConfigUpdateData
} from '@/types/admin/crawler';

// 提取占位符文本为变量
const cssSchemaPlaceholder = '{ "baseSelector": "article.post", "fields": [ { "name": "title", "selector": "h1", "type": "text" } ] }';
const jsonSchemaPlaceholder = '{ "title": "Data", "type": "object", "properties": { ... } }';

// Define an interface for the form data structure (Keep as is)
interface ConfigFormData {
    configId?: number;
    targetName?: string;
    crawlerService?: 'legacy' | 'eagleeye';
    sourceUrls?: string;
    crawlDepth?: number;
    triggerSchedule?: string;
    extractionStrategyType?: 'css' | 'llm';
    extractionSchema?: string;
    llmInstruction?: string;
    llmProviderConfig?: string;
    crawl4aiConfigOverride?: string;
    isActive?: boolean;
}

const props = defineProps({
    visible: {
      type: Boolean,
      required: true,
    },
    mode: {
      type: String as PropType<'add' | 'edit'>,
      required: true,
    },
    initialData: {
        type: Object as PropType<CrawlerConfigDetailVO | null>, // Correct type
        default: null,
    },
});

const emit = defineEmits(['update:visible', 'save', 'close']);

const isEditing = computed(() => props.mode === 'edit');

// JSON示例对话框相关状态
const jsonExampleDialogVisible = ref(false);
const jsonExampleTitle = ref('');
const jsonExampleContent = ref('');
const currentJsonField = ref('');

// JSON示例展示函数
const showJsonExample = (fieldName: string) => {
  currentJsonField.value = fieldName;
  
  if (fieldName === 'extractionSchema') {
    if (formData.value.extractionStrategyType === 'css') {
      jsonExampleTitle.value = 'CSS提取Schema示例';
      jsonExampleContent.value = formatJSON({
        "title": ".news-item .news-title",
        "publishTime": ".news-item .news-time",
        "source": ".news-item .news-source",
        "content": ".news-content",
        "author": ".author-info"
      });
    } else {
      jsonExampleTitle.value = 'LLM提取Schema示例';
      jsonExampleContent.value = formatJSON({
        "type": "object",
        "properties": {
          "title": {"type": "string"},
          "content": {"type": "string"},
          "publishTime": {"type": "string"},
          "source": {"type": "string"}
        }
      });
    }
  } else if (fieldName === 'llmProviderConfig') {
    jsonExampleTitle.value = 'LLM模型配置示例';
    jsonExampleContent.value = formatJSON({
      "provider": "dashscope",
      "model": "qwen-max",
      "temperature": 0.1,
      "max_tokens": 1000
    });
  } else if (fieldName === 'crawl4aiConfigOverride') {
    jsonExampleTitle.value = 'Crawl4AI配置覆盖示例';
    jsonExampleContent.value = formatJSON({
      "wait_for": ".article-content",
      "timeout": 30000,
      "retry_count": 3,
      "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    });
  }
  
  jsonExampleDialogVisible.value = true;
};

// 应用示例到表单
const applyExample = () => {
  if (currentJsonField.value && formData.value[currentJsonField.value as keyof ConfigFormData] !== undefined) {
    formData.value[currentJsonField.value as keyof ConfigFormData] = jsonExampleContent.value as any;
  }
  jsonExampleDialogVisible.value = false;
};

// 格式化JSON字段
const formatJsonField = (fieldName: keyof ConfigFormData) => {
  if (!formData.value[fieldName]) return;
  
  try {
    // 尝试格式化JSON
    const formatted = formatJSON(formData.value[fieldName] as string);
    formData.value[fieldName] = formatted as any;
    ElMessage.success('JSON格式化成功');
  } catch (error) {
    // 尝试修复和格式化
    try {
      const fixed = fixJSON(formData.value[fieldName] as string);
      formData.value[fieldName] = fixed as any;
      ElMessage.warning('JSON格式有问题，已尝试修复');
    } catch (e) {
      ElMessage.error('JSON格式无法修复，请检查语法');
    }
  }
};

// Deep watch for initialData changes when editing
watch(() => props.initialData, (newData) => {
  console.log('ConfigModal initialData changed:', newData);
  
  if (props.visible && newData) {
    console.log('ConfigModal initialData详细信息:', JSON.stringify(newData));
    
    // 确保数据类型正确
    const dataWithCorrectTypes = {
      ...newData,
      crawlDepth: newData.crawlDepth !== undefined ? Number(newData.crawlDepth) : 0,
      // 确保必要的默认值
      extractionStrategyType: newData.extractionStrategyType || 'css',
      isActive: newData.isActive !== undefined ? newData.isActive : true,
      triggerSchedule: newData.triggerSchedule || '0 0 * * * ?',
      extractionSchema: newData.extractionSchema || '{}',
      llmProviderConfig: newData.llmProviderConfig || '{}',
      crawl4aiConfigOverride: newData.crawl4aiConfigOverride || '{}'
    };
    
    // 记录初始数据，便于调试
    console.log('设置表单数据:', dataWithCorrectTypes);
    
    formData.value = { ...dataWithCorrectTypes };
    
    // 重置验证状态
    nextTick(() => {
      configFormRef.value?.clearValidate();
    });
  }
}, { immediate: true, deep: true });

const configFormRef = ref<FormInstance>();
const loading = ref(false);

// Ensure visibility changes also reset form correctly for add mode
watch(() => props.visible, (newVal) => {
  console.log('ConfigModal visibility changed:', newVal);
  if (newVal) {
    if (!props.initialData) {
      // 没有初始数据时，使用默认值
      formData.value = {
        crawlerService: 'eagleeye',
        extractionStrategyType: 'css',
        crawlDepth: 0,
        isActive: true,
        targetName: undefined,
        sourceUrls: undefined,
        triggerSchedule: '0 0 * * * ?', // Use default schedule
        extractionSchema: '{}',
        llmInstruction: undefined,
        llmProviderConfig: '{}',
        crawl4aiConfigOverride: '{}'
      };
      console.log('FormData reset to defaults in visibility watch');
    }
    // 如果有initialData，交给initialData的watch处理
  } else {
    // 模态框关闭时的处理
    console.log('Modal closed, form data preserved');
  }
});

// Initialize formData with explicit structure and defaults
const formData = ref<ConfigFormData>({
    configId: undefined,
    targetName: undefined,
    crawlerService: 'eagleeye', // Default to new EagleEye service
    sourceUrls: undefined,
    crawlDepth: 0,
    triggerSchedule: '0 0 * * * ?', // Default: every hour
    extractionStrategyType: 'css',
    extractionSchema: '{}',
    llmInstruction: undefined,
    llmProviderConfig: '{}',
    crawl4aiConfigOverride: '{}',
    isActive: true,
});

const validateJsonString = (rule: any, value: any, callback: any) => {
  if (!value || value.trim() === '{}') {
    callback(); // Allow empty or default empty object
    return;
  }

  if (validateJSON(value)) {
    callback();
  } else {
    callback(new Error('请输入有效的 JSON 格式'));
  }
};

const formRules = ref<FormRules<ConfigFormData>>({
  targetName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  crawlerService: [{ required: true, message: '请选择爬虫服务', trigger: 'change' }],
  sourceUrls: [{ required: true, message: '请输入起始 URL (每行一个)', trigger: 'blur' }],
  // 传统服务专用字段验证
  triggerSchedule: [
    {
      required: computed(() => formData.value.crawlerService === 'legacy').value,
      message: '请输入执行计划 (Cron 格式)',
      trigger: 'blur'
    }
  ],
  extractionStrategyType: [
    {
      required: computed(() => formData.value.crawlerService === 'legacy').value,
      message: '请选择提取策略类型',
      trigger: 'change'
    }
  ],
  // Unified extractionSchema rule with conditional required
  extractionSchema: [
    {
      // Required only if crawlerService is 'legacy' AND extractionStrategyType is 'css' or 'llm'
      required: computed(() => formData.value.crawlerService === 'legacy' && (formData.value.extractionStrategyType === 'css' || formData.value.extractionStrategyType === 'llm')).value,
      message: computed(() => formData.value.extractionStrategyType === 'css' ? '请输入 CSS 提取 Schema (JSON)' : '请输入 LLM 提取 Schema (Pydantic/JSON)').value,
      trigger: 'blur'
    },
    { validator: validateJsonString, trigger: 'blur' },
  ],
  // Conditional validation for LLM specific fields
  llmProviderConfig: [
    {
      required: computed(() => formData.value.crawlerService === 'legacy' && formData.value.extractionStrategyType === 'llm').value,
      message: '请输入 LLM 模型配置 (JSON)',
      trigger: 'blur'
    },
    { validator: validateJsonString, trigger: 'blur' }, // Also validate JSON
  ],
  llmInstruction: [
    {
      required: computed(() => formData.value.crawlerService === 'legacy' && formData.value.extractionStrategyType === 'llm').value,
      message: '请输入 LLM 指令',
      trigger: 'blur'
    }
  ],
  // Optional validation for crawl4aiConfigOverride
  crawl4aiConfigOverride: [
    { validator: validateJsonString, trigger: 'blur' } // Only validate if provided
  ]
});

// Watch strategy type to update rules reactivity and manage validation state
watch(() => formData.value.extractionStrategyType, (newType, oldType) => {
  if (configFormRef.value) {
    nextTick(() => { // Ensure reactivity updates propagate before clearing/validating
      // Fields to potentially clear validation for
      const llmFields = ['llmProviderConfig', 'llmInstruction'];

      // Clear validation for fields that are no longer required
      if (oldType === 'llm' && newType !== 'llm') {
        configFormRef.value?.clearValidate(llmFields);
      }
      // Add similar blocks here if other strategy types have specific fields

      // --- Re-validation Logic ---

      // Always re-validate the schema field as its context might change
      configFormRef.value?.validateField('extractionSchema').catch(err => console.warn("Ignoring validation error during type switch for schema:", err?.message || err));

      // If switched TO a type with specific required fields, validate them explicitly
      if (newType === 'llm') {
        configFormRef.value?.validateField('llmProviderConfig').catch(err => console.warn("Ignoring validation error during type switch for provider:", err?.message || err));
        configFormRef.value?.validateField('llmInstruction').catch(err => console.warn("Ignoring validation error during type switch for instruction:", err?.message || err));
      }
      // Add similar blocks here for other types if needed

      // Note: We catch and log validation errors here because during the type switch,
      // immediate validation might fail legitimately if the user hasn't filled the new fields yet.
      // The final validation happens on submitForm.
    });
  }
});

// Watch crawlerService to clear validation for legacy-specific fields
watch(() => formData.value.crawlerService, (newService, oldService) => {
  if (configFormRef.value) {
    nextTick(() => {
      // Legacy服务专用字段列表
      const legacyFields = ['triggerSchedule', 'extractionStrategyType', 'extractionSchema', 'llmProviderConfig', 'llmInstruction', 'crawl4aiConfigOverride'];

      if (newService === 'eagleeye') {
        // 切换到 EagleEye 服务时，清除所有 legacy 字段的验证
        configFormRef.value?.clearValidate(legacyFields);
      } else if (newService === 'legacy') {
        // 切换到传统服务时，重新验证必填字段
        configFormRef.value?.validateField('triggerSchedule').catch(err => console.warn("Ignoring validation error during service switch:", err?.message || err));
        configFormRef.value?.validateField('extractionStrategyType').catch(err => console.warn("Ignoring validation error during service switch:", err?.message || err));
      }
    });
  }
});

const handleClose = () => {
  emit('update:visible', false);
  emit('close');
  // Don't reset form here, let watcher handle it based on visibility
};

const submitForm = async () => {
  if (!configFormRef.value) return;
  try {
    const valid = await configFormRef.value.validate();
    if (valid) {
      loading.value = true;

      // 准备基础数据（所有服务都需要）
      const baseData = {
        targetName: formData.value.targetName,
        crawlerService: formData.value.crawlerService,
        sourceUrls: formData.value.sourceUrls,
        isActive: formData.value.isActive !== undefined ? formData.value.isActive : true,
      };

      // 移除configId字段，避免后端错误
      const dataToSave = { ...baseData };

      // 根据 crawlerService 添加不同的字段
      if (formData.value.crawlerService === 'eagleeye') {
        // EagleEye 服务：只需要基础字段，不需要其他配置
        console.log('提交 EagleEye 服务配置');
      } else {
        // 传统服务：需要完整的配置字段
        dataToSave.triggerSchedule = formData.value.triggerSchedule;
        dataToSave.crawlDepth = formData.value.crawlDepth !== undefined ? Number(formData.value.crawlDepth) : 0;
        dataToSave.extractionStrategyType = formData.value.extractionStrategyType;
        dataToSave.extractionSchema = safeJSONStringify(formData.value.extractionSchema || '{}');
        dataToSave.llmProviderConfig = safeJSONStringify(formData.value.llmProviderConfig || '{}');
        dataToSave.crawl4aiConfigOverride = safeJSONStringify(formData.value.crawl4aiConfigOverride || '{}');

        // LLM 指令
        if (formData.value.extractionStrategyType === 'llm') {
          dataToSave.llmInstruction = formData.value.llmInstruction || '';
        }

        console.log('提交传统服务配置');
      }

      // 移除configId字段，避免后端错误（如果有）
      if ('configId' in dataToSave) {
        delete dataToSave.configId;
      }

      // 移除createTime和updateTime字段（如果有）
      if ('createTime' in dataToSave) {
        delete dataToSave.createTime;
      }
      if ('updateTime' in dataToSave) {
        delete dataToSave.updateTime;
      }

      // 调试提交的数据 - 只打印关键字段，避免大量数据
      try {
        const debugData = {
          targetName: dataToSave.targetName,
          crawlerService: dataToSave.crawlerService,
          sourceUrlsLength: dataToSave.sourceUrls ? dataToSave.sourceUrls.length : 0,
          isActive: dataToSave.isActive,
        };
        console.log('表单最终提交数据 (摘要):', debugData);
      } catch (e) {
        console.error('打印数据摘要时出错:', e);
      }

      emit('save', dataToSave);
      // Success handling (message, close) should be done in the parent component after API call
    } else {
      ElMessage.error('请检查表单填写是否正确');
      return false;
    }
  } catch (error) {
    console.error('Form validation/submission error:', error);
    // Error handling should ideally be in the parent
  } finally {
    // Loading state might be better controlled by parent based on API call duration
     loading.value = false;
  }
};

// Expose submitForm if needed by parent (optional)
// defineExpose({ submitForm });

</script>

<style>
/* Increase z-index if needed, especially if other elements overlap */
.el-overlay {
  z-index: 2000 !important; 
}
.crawler-config-modal .el-dialog__body {
  padding: 0px 24px 16px 24px; /* Match modal-body style */
  max-height: 65vh; /* Allow scrolling */
  overflow-y: auto;
}
.crawler-config-modal .modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 24px 0px 24px;
  border-top: 1px solid #e5e7eb; /* divide-gray-200 */
  gap: 8px;
}
.crawler-config-modal .el-form-item__label {
    margin-bottom: 4px !important; /* mb-1 */
}
/* Ensure input number takes full width */
.crawler-config-modal .el-input-number {
    width: 100%;
}
/* Match textarea font */
.crawler-config-modal .el-textarea__inner.font-mono {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}
.crawler-config-modal .el-textarea__inner.text-xs {
    font-size: 0.75rem; /* 12px */
    line-height: 1rem; /* 16px */
}

/* Force 4px border-radius on form elements to match !rounded-button */
.crawler-config-modal .el-input__wrapper,
.crawler-config-modal .el-textarea__inner,
.crawler-config-modal .el-input-number,
.crawler-config-modal .el-select .el-input__wrapper {
  border-radius: 4px !important;
}

/* JSON示例对话框样式 */
.json-example-dialog .json-example-content {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 0.875rem;
  line-height: 1.25rem;
  white-space: pre-wrap;
  overflow-x: auto;
}

/* 将格式化按钮放在文本框右上角 */
.crawler-config-modal .el-textarea {
  position: relative;
  width: 100%; /* 确保文本框占满容器宽度 */
}

.crawler-config-modal .el-textarea__inner {
  padding-right: 80px; /* 给按钮留出空间 */
  width: 100% !important; /* 强制宽度100% */
  box-sizing: border-box; /* 确保padding包含在宽度内 */
  min-height: 120px; /* 确保文本区域有足够的高度 */
}

/* 调整格式化按钮大小和位置 */
.crawler-config-modal .el-button.text-xs {
  font-size: 0.7rem;
  padding: 4px 8px;
  height: 28px;
  z-index: 10; /* 确保按钮显示在文本区域之上 */
}

/* 确保JSON输入区域与其他输入框样式一致 */
.crawler-config-modal .relative {
  width: 100%;
  display: block;
}

/* 确保提取Schema和Crawl4AI配置框占满宽度 */
.crawler-config-modal .el-form-item {
  width: 100%;
}

/* 响应式处理 - 针对小屏幕优化 */
@media (max-width: 768px) {
  .crawler-config-modal .el-textarea__inner {
    padding-right: 60px; /* 小屏幕下减少右侧填充 */
  }
  
  .crawler-config-modal .el-button.text-xs {
    padding: 2px 5px;
    font-size: 0.65rem;
  }
}

/* 防止表单元素溢出 */
.crawler-config-modal .modal-body {
  overflow: visible;
}
</style>