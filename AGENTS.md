# AGENTS.md - 代理编码指南

本文文件提供代理编码智能体（如本工具）在 EagleEye2 代码库中工作的详细说明。

---

## 语言
 - 请始终用中文与用户对话！

## 构建与验证命令

### 后端（Java/Spring Boot）
```bash
# 构建项目
./mvnw clean package

# 构建并跳过测试
./mvnw clean package -DskipTests

# 运行应用
./mvnw spring-boot:run

# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=ClassName

# 运行单个测试方法
./mvnw test -Dtest=ClassName#methodName

# 类型检查/验证
./mvnw compile
```

### 前端（Vue 3 + TypeScript）
```bash
cd web

# 安装依赖
npm install

# 启动开发服务器（端口 8088）
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview

# ESLint 代码检查和自动修复
npm run lint

# TypeScript 类型检查
npx vue-tsc --noEmit
```

### 快速启动脚本
```bash
./scripts/start-all.sh      # 一键启动所有服务
./scripts/stop-all.sh       # 一键停止所有服务
./scripts/restart-all.sh    # 一键重启所有服务
./scripts/status.sh         # 查看服务状态
```

---

## 后端代码风格指南

### 导入顺序
```java
package com.eagleeye...;

// 1. Java 标准库
import java.time.LocalDateTime;
import java.util.List;

// 2. 第三方库
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// 3. Spring 框架
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

// 4. 项目内部（按模块分组）
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.model.dto.PolicyQueryDTO;
import com.eagleeye.model.entity.PolicyInfo;
import com.eagleeye.service.policy.PolicyService;
```

### 类与接口命名规范
- **Entity 实体类**：`XxxInfo` 或 `XxxAnalysis`（对应数据库表），使用 `@Data` 和 `@TableName`
- **Repository**：`XxxRepository`，继承 `BaseMapper<Xxx>`，添加 `@Mapper` 注解
- **Service 接口**：`XxxService`，定义业务接口方法
- **Service 实现**：`XxxServiceImpl`，实现接口，添加 `@Service` 和 `@Slf4j` 注解
- **Controller**：`XxxController`，添加 `@RestController` 和 `@RequestMapping`
- **DTO**：`XxxDTO`、`XxxQueryDTO`、`XxxCreateDTO`、`XxxUpdateDTO`
- **VO**：`XxxVO`、`XxxDetailVO`

### 注解与依赖注入
```java
@Slf4j
@Service
public class PolicyServiceImpl implements PolicyService {
    @Resource  // 推荐使用 @Resource（by name）而非 @Autowired（by type）
    private PolicyRepository policyRepository;

    @Resource
    private ObjectMapper objectMapper;
}
```

### 数据库字段与映射
- 数据库字段：`snake_case`（如 `create_time`, `policy_type`）
- Java 字段：`camelCase`（如 `createTime`, `policyType`）
- MyBatis Plus 自动映射，无需手动配置
- 使用 `@TableLogic` 标记逻辑删除字段（Boolean 类型）

### 查询条件构建
```java
LambdaQueryWrapper<PolicyInfo> queryWrapper = new LambdaQueryWrapper<>();
if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
    queryWrapper.like(PolicyInfo::getTitle, queryDTO.getKeyword())
            .or()
            .like(PolicyInfo::getContent, queryDTO.getKeyword());
}
queryWrapper.eq(PolicyInfo::getPolicyType, queryDTO.getPolicyType());
queryWrapper.orderByDesc(PolicyInfo::getPublishTime);
```

### 分页查询
```java
Page<PolicyInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
Page<PolicyInfo> policyPage = repository.selectPage(page, queryWrapper);
```

### API 响应格式
所有接口统一返回 `CommonResult<T>`：
```java
return CommonResult.success(data);
return CommonResult.failed("错误消息");
```

### Swagger 文档注解
```java
@Tag(name = "政策监控", description = "政策监控相关接口")
public class PolicyController {
    @Operation(summary = "政策列表", description = "分页查询政策列表")
    @GetMapping
    public CommonResult<CommonPage<PolicyVO>> listPolicies(@Validated PolicyQueryDTO queryDTO) {
        ...
    }

    @Operation(summary = "政策详情")
    @GetMapping("/{id}")
    public CommonResult<PolicyDetailVO> getPolicyDetail(
        @Parameter(description = "政策ID") @PathVariable Long id) {
        ...
    }
}
```

### 错误处理
```java
// 使用 log 记录错误
log.error("解析政策关键条款JSON出错: {}", e.getMessage());

// 返回失败响应
if (result == null) {
    return CommonResult.failed("资源不存在");
}
```

---

## 前端代码风格指南

### 导入顺序
```typescript
// 1. Vue 框架和第三方库
import { defineProps, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

// 2. 项目内部模块（使用 @ 别名）
import { getPolicyList } from '@/api/policy';
import { CommonPage } from '@/types/common';
```

### 接口定义
- 使用 `interface` 定义数据结构
- API 响应格式：`CommonResult<T>`（已在 request.ts 解析，直接返回 `data`）
- 分页数据：`CommonPage<T>`
```typescript
export interface PolicyVO {
  id: number | string;
  title: string;
  source?: string;
  publishTime?: string;
  [key: string]: any;  // 允许扩展字段
}

export interface PolicyQueryParams {
  keyword?: string;
  pageNum: number;
  pageSize: number;
}
```

### Vue 组件结构
```vue
<template>
  <!-- 模板 -->
</template>

<script lang="ts" setup>
// 1. 导入
import { defineProps, computed, ref } from 'vue';
import { useRouter } from 'vue-router';

// 2. Props 定义
const props = defineProps<{
  policy: Record<string, any>;
}>();

// 3. 响应式状态
const loading = ref(false);

// 4. 计算属性
const formattedDate = computed(() => ...);

// 5. 方法
const handleClick = () => { ... };
</script>

<style scoped>
/* 样式 */
</style>
```

### API 请求封装
在 `src/api/` 目录下按模块组织：
```typescript
import request from './request';
import { CommonPage } from '@/types/common';

export function getPolicyList(params: PolicyQueryParams): Promise<CommonPage<PolicyVO>> {
  return request({
    url: '/v1/policies',
    method: 'get',
    params
  });
}
```

### 路径别名
- `@/` → `web/src/`
- 使用 `@/` 引用项目内部模块，避免相对路径

---

## 通用规范

### 注释
- 类和方法必须有 JavaDoc 注释（中文）
- 实体类字段使用 Javadoc 注释说明
- TODO 注释需说明待完成内容

### 日志
- 使用 `@Slf4j` 注解，通过 `log` 记录日志
- 错误级别：`log.error("错误消息: {}", e.getMessage(), e)`
- 警告级别：`log.warn("警告消息: {}", message)`
- 信息级别：`log.info("操作日志: userId={}, action={}", userId, action)`

### 数据验证
- Controller 方法参数使用 `@Validated` 注解
- DTO 字段使用 JSR-303 验证注解（@NotNull, @NotBlank, @Size 等）

### JSON 处理
- 使用 `ObjectMapper` 进行 JSON 序列化/反序列化
- 使用 `TypeReference<T>` 处理泛型类型
```java
List<String> list = objectMapper.readValue(jsonStr, new TypeReference<List<String>>() {});
```

### 前端类型安全
- 启用严格模式：`tsconfig.json` 中 `"strict": true`
- 使用 TypeScript 接口定义所有数据结构
- 避免使用 `any`，优先使用 `unknown` 或具体类型

---

## 测试指南

### 后端测试
- 测试文件位于 `src/test/java/`
- 使用 JUnit 5 和 Spring Boot Test
- 单个测试运行：`./mvnw test -Dtest=ClassName#methodName`

### 前端测试
- 当前项目未配置前端测试框架
- 手动测试：启动 `npm run dev` 并访问 http://localhost:8088

---

## 常见问题

### 端口配置
- 后端 API：`9090`
- 前端开发服务器：`8088`
- API 代理：`/api` → `http://localhost:9090/api`

### 数据库连接
- MySQL Docker 容器：`my-mysql`
- 连接信息：`localhost:3306` / `eagleeye` / `captain` / `123456`

### API 文档
- Knife4j API 文档：http://localhost:9090/api/doc.html
