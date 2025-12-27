package com.eagleeye.controller.dashboard;

import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.vo.dashboard.DashboardStatsVO;
import com.eagleeye.model.vo.dashboard.PolicySummaryVO;
import com.eagleeye.model.vo.dashboard.CompetitorSummaryVO;
import com.eagleeye.model.vo.dashboard.RequirementSummaryVO;
import com.eagleeye.service.dashboard.DashboardService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 仪表盘控制器
 */
@RestController
@Api(tags = "仪表盘管理")
@RequestMapping("/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     */
    @ApiOperation("获取仪表盘统计数据")
    @GetMapping("/stats")
    public CommonResult<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO statsVO = dashboardService.getDashboardStats();
        return CommonResult.success(statsVO);
    }

    /**
     * 获取政策摘要
     */
    @ApiOperation("获取政策摘要")
    @GetMapping("/policy-summary")
    public CommonResult<PolicySummaryVO> getPolicySummary(
            @ApiParam(value = "限制条数", defaultValue = "5") @RequestParam(value = "limit", required = false) Integer limit) {
        PolicySummaryVO summaryVO = dashboardService.getPolicySummary(limit);
        return CommonResult.success(summaryVO);
    }

    /**
     * 获取竞品摘要
     */
    @ApiOperation("获取竞品摘要")
    @GetMapping("/competitor-summary")
    public CommonResult<CompetitorSummaryVO> getCompetitorSummary(
            @ApiParam(value = "限制条数", defaultValue = "5") @RequestParam(value = "limit", required = false) Integer limit) {
        CompetitorSummaryVO summaryVO = dashboardService.getCompetitorSummary(limit);
        return CommonResult.success(summaryVO);
    }

    /**
     * 获取需求摘要
     */
    @ApiOperation("获取需求摘要")
    @GetMapping("/requirement-summary")
    public CommonResult<RequirementSummaryVO> getRequirementSummary(
            @ApiParam(value = "限制条数", defaultValue = "5") @RequestParam(value = "limit", required = false) Integer limit) {
        RequirementSummaryVO summaryVO = dashboardService.getRequirementSummary(limit);
        return CommonResult.success(summaryVO);
    }
} 