package com.eagleeye.controller.crawler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.CrawlerConfigCreateDTO;
import com.eagleeye.model.dto.CrawlerConfigQueryDTO;
import com.eagleeye.model.dto.CrawlerConfigUpdateDTO;
import com.eagleeye.model.vo.CrawlerConfigDetailVO;
import com.eagleeye.model.vo.CrawlerConfigVO;
import com.eagleeye.service.crawler.CrawlerConfigAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid; // Ensure JSR-303 validation is triggered

/**
 * 爬虫配置管理控制器 (管理员)
 */
@RestController
@RequestMapping("/v1/admin/crawler/configs")
@Api(tags = "管理员 - 爬虫配置管理接口")
@Validated // For query param validation if needed
public class CrawlerConfigAdminController {

    @Resource
    private CrawlerConfigAdminService crawlerConfigAdminService;

    @ApiOperation("创建爬虫配置")
    @PostMapping
    public CommonResult<Long> createConfig(
            @ApiParam("创建参数") @Valid @RequestBody CrawlerConfigCreateDTO createDTO) {
        Long configId = crawlerConfigAdminService.createConfig(createDTO);
        if (configId != null) {
            return CommonResult.success(configId, "创建成功");
        } else {
            return CommonResult.failed("创建失败");
        }
    }

    @ApiOperation("分页查询爬虫配置列表")
    @GetMapping
    public CommonResult<CommonPage<CrawlerConfigVO>> listConfigs(
            // Use @Valid if query DTO has validation annotations
            @ApiParam("查询参数") CrawlerConfigQueryDTO queryDTO) { // DTO contains pageNum and pageSize
        Page<CrawlerConfigVO> pageResult = crawlerConfigAdminService.listConfigs(queryDTO);
        return CommonResult.success(CommonPage.restPage(pageResult));
    }


    @ApiOperation("获取爬虫配置详情")
    @GetMapping("/{configId}")
    public CommonResult<CrawlerConfigDetailVO> getConfigDetail(
            @ApiParam("配置ID") @PathVariable Long configId) {
        CrawlerConfigDetailVO detailVO = crawlerConfigAdminService.getConfigDetail(configId);
        if (detailVO != null) {
            return CommonResult.success(detailVO);
        } else {
            return CommonResult.failed("配置不存在或已被删除");
        }
    }

    @ApiOperation("更新爬虫配置")
    @PutMapping("/{configId}")
    public CommonResult<String> updateConfig(
            @ApiParam("配置ID") @PathVariable Long configId,
            @ApiParam("更新参数") @Valid @RequestBody CrawlerConfigUpdateDTO updateDTO) {
        boolean success = crawlerConfigAdminService.updateConfig(configId, updateDTO);
        if (success) {
            return CommonResult.success("更新成功");
        } else {
            return CommonResult.failed("更新失败，配置可能不存在或已被删除");
        }
    }

    @ApiOperation("删除爬虫配置")
    @DeleteMapping("/{configId}")
    public CommonResult<String> deleteConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        boolean success = crawlerConfigAdminService.deleteConfig(configId);
        if (success) {
            return CommonResult.success("删除成功");
        } else {
            // Deletion might fail if already deleted, handle gracefully
            return CommonResult.failed("删除失败");
        }
    }

    @ApiOperation("更新爬虫配置状态（启用/禁用）")
    @PatchMapping("/{configId}/status")
    public CommonResult<String> updateConfigStatus(
            @ApiParam("配置ID") @PathVariable Long configId,
            @ApiParam(value = "是否启用", required = true) @RequestParam Boolean isActive) {
        boolean success = crawlerConfigAdminService.updateConfigStatus(configId, isActive);
        if (success) {
            return CommonResult.success("状态更新成功");
        } else {
            return CommonResult.failed("状态更新失败，配置可能不存在");
        }
    }

    @ApiOperation("手动触发一次爬虫任务")
    @PostMapping("/{configId}/trigger")
    public CommonResult<String> triggerConfig(
            @ApiParam("配置ID") @PathVariable Long configId) {
        boolean success = crawlerConfigAdminService.triggerConfig(configId);
        if (success) {
            return CommonResult.success("任务触发成功（已发送到队列）");
        } else {
            return CommonResult.failed("任务触发失败（配置不存在、未激活或发送MQ失败）");
        }
    }
} 