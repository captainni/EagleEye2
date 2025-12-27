package com.eagleeye.controller.crawler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.TaskLogQueryDTO;
import com.eagleeye.model.vo.CrawlerTaskLogVO;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
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
} 