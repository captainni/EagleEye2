package com.eagleeye.controller.competitor;

import com.eagleeye.common.api.CommonPage;
import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.CompetitorQueryDTO;
import com.eagleeye.model.vo.CompetitorDetailVO;
import com.eagleeye.model.vo.CompetitorVO;
import com.eagleeye.service.competitor.CompetitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 竞品动态控制器
 */
@RestController
@RequestMapping("/v1/competitors")
@Tag(name = "竞品动态", description = "竞品动态相关接口")
public class CompetitorController {
    
    @Autowired
    private CompetitorService competitorService;
    
    @Operation(summary = "竞品动态列表", description = "分页查询竞品动态列表，支持多条件筛选")
    @GetMapping
    public CommonResult<CommonPage<CompetitorVO>> listCompetitors(@Validated CompetitorQueryDTO queryDTO) {
        CommonPage<CompetitorVO> result = competitorService.listCompetitors(queryDTO);
        return CommonResult.success(result);
    }
    
    @Operation(summary = "竞品详情", description = "获取竞品详细信息，包含原文和分析内容")
    @GetMapping("/{id}")
    public CommonResult<CompetitorDetailVO> getCompetitorDetail(
            @Parameter(description = "竞品ID") @PathVariable Long id) {
        CompetitorDetailVO competitorDetail = competitorService.getCompetitorDetail(id);
        if (competitorDetail != null) {
            return CommonResult.success(competitorDetail);
        } else {
            return CommonResult.failed("竞品信息不存在");
        }
    }
    
    @Operation(summary = "转为需求", description = "将竞品动态转化为需求")
    @PostMapping("/{id}/to-requirement")
    public CommonResult<Long> convertToRequirement(
            @Parameter(description = "竞品ID") @PathVariable Long id) {
        // TODO: 从当前登录用户信息中获取用户ID，这里暂时写死
        Long userId = 1L;
        Long requirementId = competitorService.convertToRequirement(id, userId);
        if (requirementId > 0) {
            return CommonResult.success(requirementId, "成功转化为需求");
        } else {
            return CommonResult.failed("转化需求失败");
        }
    }
} 