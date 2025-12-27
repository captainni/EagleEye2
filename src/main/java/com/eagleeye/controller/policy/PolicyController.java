package com.eagleeye.controller.policy;

import com.eagleeye.common.api.CommonPage;
import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.PolicyQueryDTO;
import com.eagleeye.model.vo.PolicyDetailVO;
import com.eagleeye.model.vo.PolicyVO;
import com.eagleeye.service.policy.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 政策监控控制器
 */
@RestController
@RequestMapping("/v1/policies")
@Tag(name = "政策监控", description = "政策监控相关接口")
public class PolicyController {
    
    @Autowired
    private PolicyService policyService;
    
    @Operation(summary = "政策列表", description = "分页查询政策列表，支持多条件筛选")
    @GetMapping
    public CommonResult<CommonPage<PolicyVO>> listPolicies(@Validated PolicyQueryDTO queryDTO) {
        CommonPage<PolicyVO> result = policyService.listPolicies(queryDTO);
        return CommonResult.success(result);
    }
    
    @Operation(summary = "政策详情", description = "获取政策详细信息，包含原文和分析内容")
    @GetMapping("/{id}")
    public CommonResult<PolicyDetailVO> getPolicyDetail(
            @Parameter(description = "政策ID") @PathVariable Long id) {
        PolicyDetailVO policyDetail = policyService.getPolicyDetail(id);
        if (policyDetail != null) {
            return CommonResult.success(policyDetail);
        } else {
            return CommonResult.failed("政策不存在");
        }
    }
    
    @Operation(summary = "转为需求", description = "将政策转化为需求")
    @PostMapping("/{id}/to-requirement")
    public CommonResult<Long> convertToRequirement(
            @Parameter(description = "政策ID") @PathVariable Long id) {
        // TODO: 从当前登录用户信息中获取用户ID，这里暂时写死
        Long userId = 1L;
        Long requirementId = policyService.convertToRequirement(id, userId);
        if (requirementId > 0) {
            return CommonResult.success(requirementId, "成功转化为需求");
        } else {
            return CommonResult.failed("转化需求失败");
        }
    }
} 