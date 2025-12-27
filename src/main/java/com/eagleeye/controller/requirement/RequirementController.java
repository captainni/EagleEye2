package com.eagleeye.controller.requirement;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.common.api.CommonResult;
import com.eagleeye.model.dto.RequirementCreateDTO;
import com.eagleeye.model.dto.RequirementQueryDTO;
import com.eagleeye.model.dto.RequirementUpdateDTO;
import com.eagleeye.model.vo.RequirementDetailVO;
import com.eagleeye.model.vo.RequirementVO;
import com.eagleeye.service.requirement.RequirementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 需求池控制器
 */
@RestController
@RequestMapping("/v1/requirements")
@Api(tags = "需求池管理接口")
public class RequirementController {

    @Resource
    private RequirementService requirementService;

    @ApiOperation("分页查询需求列表")
    @GetMapping
    public CommonResult<CommonPage<RequirementVO>> listRequirements(
            @ApiParam("查询关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @ApiParam("状态筛选") @RequestParam(value = "status", required = false) String status,
            @ApiParam("优先级筛选") @RequestParam(value = "priority", required = false) String priority,
            @ApiParam("来源类型筛选") @RequestParam(value = "sourceType", required = false) String sourceType,
            @ApiParam("页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @ApiParam("每页记录数") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        
        // 构建查询条件
        RequirementQueryDTO queryDTO = new RequirementQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setStatus(status);
        queryDTO.setPriority(priority);
        queryDTO.setSourceType(sourceType);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        // 执行查询
        Page<RequirementVO> page = requirementService.listRequirements(queryDTO);
        
        return CommonResult.success(CommonPage.restPage(page));
    }

    @ApiOperation("获取需求详情")
    @GetMapping("/{id}")
    public CommonResult<RequirementDetailVO> getRequirementDetail(
            @ApiParam("需求ID") @PathVariable Long id) {
        
        RequirementDetailVO detailVO = requirementService.getRequirementDetail(id);
        
        if (detailVO != null) {
            return CommonResult.success(detailVO);
        } else {
            return CommonResult.failed("需求不存在");
        }
    }

    @ApiOperation("创建需求")
    @PostMapping
    public CommonResult<Long> createRequirement(
            @ApiParam("创建参数") @RequestBody RequirementCreateDTO createDTO) {
        
        Long id = requirementService.createRequirement(createDTO);
        
        if (id != null) {
            return CommonResult.success(id, "创建成功");
        } else {
            return CommonResult.failed("创建失败");
        }
    }

    @ApiOperation("更新需求")
    @PutMapping("/{id}")
    public CommonResult<String> updateRequirement(
            @ApiParam("需求ID") @PathVariable Long id,
            @ApiParam("更新参数") @RequestBody RequirementUpdateDTO updateDTO) {
        
        // 确保ID一致
        updateDTO.setId(id);
        
        boolean result = requirementService.updateRequirement(updateDTO);
        
        if (result) {
            return CommonResult.success("更新成功");
        } else {
            return CommonResult.failed("更新失败");
        }
    }

    @ApiOperation("删除需求")
    @DeleteMapping("/{id}")
    public CommonResult<String> deleteRequirement(
            @ApiParam("需求ID") @PathVariable Long id) {
        
        boolean result = requirementService.deleteRequirement(id);
        
        if (result) {
            return CommonResult.success("删除成功");
        } else {
            return CommonResult.failed("删除失败");
        }
    }

    @ApiOperation("将政策转化为需求")
    @PostMapping("/policy/{policyId}/to-requirement")
    public CommonResult<Long> convertPolicyToRequirement(
            @ApiParam("政策ID") @PathVariable Long policyId) {
        
        Long id = requirementService.convertPolicyToRequirement(policyId);
        
        if (id != null) {
            return CommonResult.success(id, "转化成功");
        } else {
            return CommonResult.failed("转化失败");
        }
    }

    @ApiOperation("将竞品动态转化为需求")
    @PostMapping("/competitor/{competitorId}/to-requirement")
    public CommonResult<Long> convertCompetitorToRequirement(
            @ApiParam("竞品ID") @PathVariable Long competitorId) {
        
        Long id = requirementService.convertCompetitorToRequirement(competitorId);
        
        if (id != null) {
            return CommonResult.success(id, "转化成功");
        } else {
            return CommonResult.failed("转化失败");
        }
    }
} 