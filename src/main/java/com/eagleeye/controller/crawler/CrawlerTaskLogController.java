package com.eagleeye.controller.crawler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.TaskLogQueryDTO;
import com.eagleeye.model.vo.CrawlerTaskLogVO;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import com.eagleeye.service.policy.PolicyAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 爬虫任务日志控制器
 *
 * @author eagleeye
 */
@Slf4j
@RestController
@RequestMapping("/v1/admin/crawler/tasks")
@Api(tags = "爬虫任务监控接口")
public class CrawlerTaskLogController {

    @Resource
    private CrawlerTaskLogService crawlerTaskLogService;

    @Resource
    private PolicyAnalysisService policyAnalysisService;

    @ApiOperation("分页查询爬虫任务日志")
    @GetMapping
    public CommonResult<CommonPage<CrawlerTaskLogVO>> listTaskLogs(
            @ApiParam("任务执行状态") @RequestParam(required = false) String status,
            @ApiParam("配置ID") @RequestParam(required = false) Long configId,
            @ApiParam("开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @ApiParam("结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页记录数") @RequestParam(defaultValue = "10") Integer pageSize) {

        try {
            log.info("接收到爬虫任务日志列表请求: status={}, configId={}, startTime={}, endTime={}, pageNum={}, pageSize={}",
                    status, configId, startTime, endTime, pageNum, pageSize);

            // 构建查询对象
            TaskLogQueryDTO queryDTO = new TaskLogQueryDTO();
            queryDTO.setStatus(status);
            queryDTO.setConfigId(configId);
            queryDTO.setStartTime(startTime);
            queryDTO.setEndTime(endTime);
            queryDTO.setPageNum(pageNum);
            queryDTO.setPageSize(pageSize);

            // 查询任务日志
            Page<CrawlerTaskLogVO> page = crawlerTaskLogService.listTaskLogs(queryDTO);

            // 转换为通用分页结果
            CommonPage<CrawlerTaskLogVO> result = new CommonPage<>();
            result.setPageNum(page.getCurrent());
            result.setPageSize(page.getSize());
            result.setTotal(page.getTotal());
            result.setTotalPage(page.getPages());
            result.setList(page.getRecords());

            log.info("查询爬虫任务日志成功, 共找到 {} 条记录", page.getTotal());

            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("获取爬虫任务日志列表失败", e);
            return CommonResult.failed("获取爬虫任务日志列表失败: " + e.getMessage());
        }
    }

    @ApiOperation("根据任务ID查询任务状态")
    @GetMapping("/{taskId}/status")
    public CommonResult<CrawlerTaskLogVO> getTaskStatus(
            @ApiParam("任务ID") @PathVariable String taskId) {

        try {
            log.info("接收到任务状态查询请求: taskId={}", taskId);

            CrawlerTaskLogVO taskLog = crawlerTaskLogService.getByTaskId(taskId);

            if (taskLog != null) {
                log.info("查询任务状态成功: taskId={}, status={}", taskId, taskLog.getStatus());
                return CommonResult.success(taskLog);
            } else {
                log.warn("任务不存在: taskId={}", taskId);
                return CommonResult.failed("任务不存在: taskId=" + taskId);
            }
        } catch (Exception e) {
            log.error("查询任务状态失败: taskId={}", taskId, e);
            return CommonResult.failed("查询任务状态失败: " + e.getMessage());
        }
    }

    @ApiOperation("触发政策文章分析入库")
    @PostMapping("/{taskId}/analyze-policies")
    public CommonResult<String> triggerPolicyAnalysis(
            @ApiParam("任务ID") @PathVariable String taskId) {

        try {
            log.info("接收到政策分析请求: taskId={}", taskId);

            // 通过 taskId 查找 logId
            CrawlerTaskLogVO taskLog = crawlerTaskLogService.getByTaskId(taskId);
            if (taskLog == null) {
                log.warn("任务不存在: taskId={}", taskId);
                return CommonResult.failed("任务不存在: taskId=" + taskId);
            }

            // 检查任务状态，只有成功的任务才能分析
            if (!"success".equals(taskLog.getStatus())) {
                log.warn("任务状态不是成功，无法分析: taskId={}, status={}", taskId, taskLog.getStatus());
                return CommonResult.failed("任务状态不是成功，无法分析: status=" + taskLog.getStatus());
            }

            // 检查是否已经在分析中
            if ("analyzing".equals(taskLog.getAnalysisStatus())) {
                log.warn("任务正在分析中: taskId={}", taskId);
                return CommonResult.failed("任务正在分析中");
            }

            // 获取当前用户ID（新增）
            Long userId = getCurrentUserId();

            // 异步触发分析（传递 userId）
            policyAnalysisService.analyzePoliciesAsync(taskLog.getLogId(), userId);

            log.info("政策分析任务已触发: taskId={}, userId={}", taskId, userId);
            return CommonResult.success("分析任务已启动");

        } catch (Exception e) {
            log.error("触发政策分析失败: taskId={}", taskId, e);
            return CommonResult.failed("触发政策分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID
     * TODO: 从认证上下文获取当前用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从认证上下文获取当前用户ID
        return 1L; // 临时返回默认值
    }
} 