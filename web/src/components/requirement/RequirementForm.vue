<template>
  <el-form 
    ref="requirementFormRef" 
    :model="formData" 
    :rules="rules" 
    label-width="100px"
    class="p-4 border border-gray-200 rounded-lg bg-white"
  >
    <el-form-item label="需求标题" prop="title">
      <el-input v-model="formData.title" placeholder="请输入需求标题"></el-input>
    </el-form-item>
    
    <el-form-item label="优先级" prop="priority">
      <el-select v-model="formData.priority" placeholder="请选择优先级">
        <el-option label="高" value="HIGH"></el-option>
        <el-option label="中" value="MEDIUM"></el-option>
        <el-option label="低" value="LOW"></el-option>
      </el-select>
    </el-form-item>
    
    <el-form-item label="状态" prop="status">
      <el-select v-model="formData.status" placeholder="请选择状态">
        <el-option label="新建" value="NEW"></el-option>
        <el-option label="处理中" value="PROCESSING"></el-option>
        <el-option label="已完成" value="COMPLETED"></el-option>
        <el-option label="已拒绝" value="REJECTED"></el-option>
      </el-select>
    </el-form-item>
    
    <el-form-item label="计划完成时间" prop="planTime">
      <el-date-picker
        v-model="formData.planTime"
        type="date"
        placeholder="选择日期"
        value-format="YYYY-MM-DDTHH:mm:ss"
      />
    </el-form-item>
    
    <el-form-item label="需求背景" prop="background">
      <el-input 
        type="textarea" 
        :rows="4" 
        v-model="formData.background" 
        placeholder="请输入需求背景信息"
      ></el-input>
    </el-form-item>
    
    <el-form-item label="详细描述" prop="description">
      <el-input 
        type="textarea" 
        :rows="6" 
        v-model="formData.description" 
        placeholder="请输入详细的需求描述"
      ></el-input>
    </el-form-item>
    
    <!-- 附件上传（暂未实现） -->
    <el-form-item label="附件">
      <el-upload
        action="#"
        :auto-upload="false"
        disabled
      >
        <el-button type="primary" plain disabled><i class="fas fa-upload mr-1"></i> 上传附件 (未实现)</el-button>
      </el-upload>
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="submitForm" :loading="loading">提交</el-button>
      <el-button @click="resetForm">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" setup>
import { ref, reactive, watch, toRefs } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import type { RequirementCreateDTO, RequirementUpdateDTO, RequirementDetailVO } from '@/types/requirement';

// 定义属性
const props = defineProps({
  initialData: {
    type: Object as () => RequirementDetailVO | null,
    default: null
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  loading: {
      type: Boolean,
      default: false
  }
});

// 定义事件
const emit = defineEmits(['submit']);

const requirementFormRef = ref<FormInstance>();
const formData = reactive<RequirementCreateDTO | RequirementUpdateDTO>({
  title: '',
  priority: 'MEDIUM',
  status: 'NEW',
  planTime: undefined,
  background: '',
  description: ''
});

const rules = reactive<FormRules>({
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
});

// 监听 initialData 变化，填充表单 (用于编辑模式)
watch(() => props.initialData, (newData) => {
  if (newData && props.isEdit) {
    Object.assign(formData, {
      id: newData.id, // 添加id用于更新
      title: newData.title,
      priority: newData.priorityLevel, // 注意：后端用HIGH/MEDIUM/LOW
      status: newData.status, // 后端用 NEW/PROCESSING/...
      planTime: newData.planTime ? newData.planTime.split('T')[0] + 'T00:00:00' : undefined, // 格式化日期
      background: newData.background || '',
      description: newData.description || ''
    });
  }
}, { immediate: true });

const submitForm = async () => {
  if (!requirementFormRef.value) return;
  await requirementFormRef.value.validate((valid) => {
    if (valid) {
      // 提交数据
      const dataToSubmit = { ...formData };
      // 如果是编辑模式，确保有id
      if (props.isEdit && !(dataToSubmit as RequirementUpdateDTO).id && props.initialData) {
          (dataToSubmit as RequirementUpdateDTO).id = props.initialData.id;
      }
      emit('submit', dataToSubmit);
    } else {
      console.log('表单校验失败!');
    }
  });
};

const resetForm = () => {
  if (!requirementFormRef.value) return;
  requirementFormRef.value.resetFields();
  // 如果是编辑模式，还需要根据 initialData 重新填充
  if (props.isEdit && props.initialData) {
      Object.assign(formData, {
          id: props.initialData.id,
          title: props.initialData.title,
          priority: props.initialData.priorityLevel,
          status: props.initialData.status,
          planTime: props.initialData.planTime ? props.initialData.planTime.split('T')[0] + 'T00:00:00' : undefined,
          background: props.initialData.background || '',
          description: props.initialData.description || ''
      });
  } else {
      // 新增模式重置为默认值
      Object.assign(formData, {
          title: '',
          priority: 'MEDIUM',
          status: 'NEW',
          planTime: undefined,
          background: '',
          description: ''
      });
  }
};
</script> 