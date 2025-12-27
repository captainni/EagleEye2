package com.eagleeye.service.dashboard.impl;

import com.eagleeye.model.vo.dashboard.DashboardStatsVO;
import com.eagleeye.model.vo.dashboard.PolicySummaryVO;
import com.eagleeye.model.vo.dashboard.CompetitorSummaryVO;
import com.eagleeye.model.vo.dashboard.RequirementSummaryVO;
import com.eagleeye.service.dashboard.DashboardService;
import com.eagleeye.service.policy.PolicyService;
import com.eagleeye.service.competitor.CompetitorService;
import com.eagleeye.service.requirement.RequirementService;
import com.eagleeye.model.dto.PolicyQueryDTO;
import com.eagleeye.model.dto.CompetitorQueryDTO;
import com.eagleeye.model.dto.RequirementQueryDTO;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.model.vo.PolicyVO;
import com.eagleeye.model.vo.CompetitorVO;
import com.eagleeye.model.vo.RequirementVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现类
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private PolicyService policyService;
    
    @Autowired
    private CompetitorService competitorService;
    
    @Autowired
    private RequirementService requirementService;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO statsVO = new DashboardStatsVO();
        
        // 获取政策统计数据
        DashboardStatsVO.PolicyStatVO policyStatVO = new DashboardStatsVO.PolicyStatVO();
        PolicyQueryDTO policyQueryDTO = new PolicyQueryDTO();
        policyQueryDTO.setPageNum(1);
        policyQueryDTO.setPageSize(1);
        CommonPage<PolicyVO> policyPage = policyService.listPolicies(policyQueryDTO);
        policyStatVO.setTotal(Math.toIntExact(policyPage.getTotal()));
        
        // 获取政策重要程度分布
        Map<String, Integer> importanceDistribution = new HashMap<>();
        importanceDistribution.put("高", countPoliciesByImportance("高"));
        importanceDistribution.put("中", countPoliciesByImportance("中"));
        importanceDistribution.put("低", countPoliciesByImportance("低"));
        policyStatVO.setImportanceDistribution(importanceDistribution);
        
        // 获取政策类型分布
        Map<String, Integer> typeDistribution = new HashMap<>();
        typeDistribution.put("监管政策", countPoliciesByType("监管政策"));
        typeDistribution.put("行业标准", countPoliciesByType("行业标准"));
        typeDistribution.put("指导意见", countPoliciesByType("指导意见"));
        policyStatVO.setTypeDistribution(typeDistribution);
        
        statsVO.setPolicyStats(policyStatVO);
        
        // 获取竞品统计数据
        DashboardStatsVO.CompetitorStatVO competitorStatVO = new DashboardStatsVO.CompetitorStatVO();
        CompetitorQueryDTO competitorQueryDTO = new CompetitorQueryDTO();
        competitorQueryDTO.setPageNum(1);
        competitorQueryDTO.setPageSize(1);
        CommonPage<CompetitorVO> competitorPage = competitorService.listCompetitors(competitorQueryDTO);
        competitorStatVO.setTotal(Math.toIntExact(competitorPage.getTotal()));
        
        // 获取竞品银行分布
        Map<String, Integer> bankDistribution = new HashMap<>();
        bankDistribution.put("国有银行", countCompetitorsByCompany("国有银行"));
        bankDistribution.put("股份制银行", countCompetitorsByCompany("股份制银行"));
        bankDistribution.put("城商行", countCompetitorsByCompany("城商行"));
        competitorStatVO.setBankDistribution(bankDistribution);
        
        // 获取竞品更新类型分布
        Map<String, Integer> updateTypeDistribution = new HashMap<>();
        updateTypeDistribution.put("功能更新", countCompetitorsByType("功能更新"));
        updateTypeDistribution.put("UI改版", countCompetitorsByType("UI改版"));
        updateTypeDistribution.put("流程优化", countCompetitorsByType("流程优化"));
        competitorStatVO.setUpdateTypeDistribution(updateTypeDistribution);
        
        statsVO.setCompetitorStats(competitorStatVO);
        
        // 获取需求统计数据
        DashboardStatsVO.RequirementStatVO requirementStatVO = new DashboardStatsVO.RequirementStatVO();
        RequirementQueryDTO requirementQueryDTO = new RequirementQueryDTO();
        requirementQueryDTO.setPageNum(1);
        requirementQueryDTO.setPageSize(1);
        // 使用正确的类型转换方式
        Page<RequirementVO> reqPage = requirementService.listRequirements(requirementQueryDTO);
        CommonPage<RequirementVO> requirementPage = CommonPage.restPage(reqPage);
        requirementStatVO.setTotal(Math.toIntExact(requirementPage.getTotal()));
        
        // 获取需求优先级分布
        Map<String, Integer> priorityDistribution = new HashMap<>();
        priorityDistribution.put("高", countRequirementsByPriority("HIGH"));
        priorityDistribution.put("中", countRequirementsByPriority("MEDIUM"));
        priorityDistribution.put("低", countRequirementsByPriority("LOW"));
        requirementStatVO.setPriorityDistribution(priorityDistribution);
        
        // 获取需求状态分布
        Map<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("待处理", countRequirementsByStatus("NEW"));
        statusDistribution.put("进行中", countRequirementsByStatus("PROCESSING"));
        statusDistribution.put("已完成", countRequirementsByStatus("COMPLETED"));
        requirementStatVO.setStatusDistribution(statusDistribution);
        
        statsVO.setRequirementStats(requirementStatVO);
        
        // 获取时间趋势数据
        DashboardStatsVO.TrendStatVO trendStatVO = new DashboardStatsVO.TrendStatVO();
        trendStatVO.setLastWeekData(getLastDaysData(7));
        trendStatVO.setLastMonthData(getLastDaysData(30));
        
        statsVO.setTrendStats(trendStatVO);
        
        return statsVO;
    }

    @Override
    public PolicySummaryVO getPolicySummary(Integer limit) {
        if (limit == null) {
            limit = 5;
        }
        
        PolicySummaryVO summaryVO = new PolicySummaryVO();
        
        // 获取最新重要政策
        PolicyQueryDTO queryDTO = new PolicyQueryDTO();
        queryDTO.setImportance("高");
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(limit);
        CommonPage<PolicyVO> policyPage = policyService.listPolicies(queryDTO);
        summaryVO.setLatestImportantPolicies(policyPage.getList());
        
        // 获取政策类型分布
        Map<String, Integer> typeDistribution = new HashMap<>();
        typeDistribution.put("监管政策", countPoliciesByType("监管政策"));
        typeDistribution.put("行业标准", countPoliciesByType("行业标准"));
        typeDistribution.put("指导意见", countPoliciesByType("指导意见"));
        summaryVO.setPolicyTypeDistribution(typeDistribution);
        
        // 获取政策来源分布
        Map<String, Integer> sourceDistribution = new HashMap<>();
        sourceDistribution.put("人民银行", countPoliciesBySource("人民银行"));
        sourceDistribution.put("银保监会", countPoliciesBySource("银保监会"));
        sourceDistribution.put("其他", countPoliciesBySource("其他"));
        summaryVO.setPolicySourceDistribution(sourceDistribution);
        
        // 获取相关领域分布
        Map<String, Integer> areaDistribution = new HashMap<>();
        areaDistribution.put("支付", countPoliciesByArea("支付"));
        areaDistribution.put("信贷", countPoliciesByArea("信贷"));
        areaDistribution.put("数据安全", countPoliciesByArea("数据安全"));
        summaryVO.setPolicyAreaDistribution(areaDistribution);
        
        return summaryVO;
    }

    @Override
    public CompetitorSummaryVO getCompetitorSummary(Integer limit) {
        if (limit == null) {
            limit = 5;
        }
        
        CompetitorSummaryVO summaryVO = new CompetitorSummaryVO();
        
        // 获取最新竞品动态
        CompetitorQueryDTO queryDTO = new CompetitorQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(limit);
        CommonPage<CompetitorVO> competitorPage = competitorService.listCompetitors(queryDTO);
        summaryVO.setLatestCompetitorUpdates(competitorPage.getList());
        
        // 获取竞品银行分布
        Map<String, Integer> bankDistribution = new HashMap<>();
        bankDistribution.put("国有银行", countCompetitorsByCompany("国有银行"));
        bankDistribution.put("股份制银行", countCompetitorsByCompany("股份制银行"));
        bankDistribution.put("城商行", countCompetitorsByCompany("城商行"));
        summaryVO.setBankDistribution(bankDistribution);
        
        // 获取更新类型分布
        Map<String, Integer> updateTypeDistribution = new HashMap<>();
        updateTypeDistribution.put("功能更新", countCompetitorsByType("功能更新"));
        updateTypeDistribution.put("UI改版", countCompetitorsByType("UI改版"));
        updateTypeDistribution.put("流程优化", countCompetitorsByType("流程优化"));
        summaryVO.setUpdateTypeDistribution(updateTypeDistribution);
        
        // 获取标签分布
        Map<String, Integer> tagDistribution = new HashMap<>();
        tagDistribution.put("支付", countCompetitorsByTag("支付"));
        tagDistribution.put("理财", countCompetitorsByTag("理财"));
        tagDistribution.put("信用卡", countCompetitorsByTag("信用卡"));
        summaryVO.setTagDistribution(tagDistribution);
        
        return summaryVO;
    }

    @Override
    public RequirementSummaryVO getRequirementSummary(Integer limit) {
        if (limit == null) {
            limit = 5;
        }
        
        RequirementSummaryVO summaryVO = new RequirementSummaryVO();
        
        // 获取最新需求
        RequirementQueryDTO queryDTO = new RequirementQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(limit);
        
        // 使用正确的类型转换方式
        Page<RequirementVO> reqPage = requirementService.listRequirements(queryDTO);
        CommonPage<RequirementVO> requirementPage = CommonPage.restPage(reqPage);
        summaryVO.setLatestRequirements(requirementPage.getList());
        
        // 获取需求优先级分布
        Map<String, Integer> priorityDistribution = new HashMap<>();
        priorityDistribution.put("高", countRequirementsByPriority("HIGH"));
        priorityDistribution.put("中", countRequirementsByPriority("MEDIUM"));
        priorityDistribution.put("低", countRequirementsByPriority("LOW"));
        summaryVO.setPriorityDistribution(priorityDistribution);
        
        // 获取需求状态分布
        Map<String, Integer> statusDistribution = new HashMap<>();
        statusDistribution.put("待处理", countRequirementsByStatus("NEW"));
        statusDistribution.put("进行中", countRequirementsByStatus("PROCESSING"));
        statusDistribution.put("已完成", countRequirementsByStatus("COMPLETED"));
        summaryVO.setStatusDistribution(statusDistribution);
        
        // 获取需求来源分布
        Map<String, Integer> sourceDistribution = new HashMap<>();
        sourceDistribution.put("政策转化", countRequirementsBySourceType("POLICY"));
        sourceDistribution.put("竞品转化", countRequirementsBySourceType("COMPETITOR"));
        sourceDistribution.put("手动创建", countRequirementsBySourceType("MANUAL"));
        summaryVO.setSourceDistribution(sourceDistribution);
        
        return summaryVO;
    }

    /**
     * 统计指定重要程度的政策数量
     */
    private Integer countPoliciesByImportance(String importance) {
        PolicyQueryDTO queryDTO = new PolicyQueryDTO();
        queryDTO.setImportance(importance);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<PolicyVO> policyPage = policyService.listPolicies(queryDTO);
        return Math.toIntExact(policyPage.getTotal());
    }
    
    /**
     * 统计指定类型的政策数量
     */
    private Integer countPoliciesByType(String policyType) {
        PolicyQueryDTO queryDTO = new PolicyQueryDTO();
        queryDTO.setPolicyType(policyType);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<PolicyVO> policyPage = policyService.listPolicies(queryDTO);
        return Math.toIntExact(policyPage.getTotal());
    }
    
    /**
     * 统计指定来源的政策数量
     */
    private Integer countPoliciesBySource(String source) {
        PolicyQueryDTO queryDTO = new PolicyQueryDTO();
        queryDTO.setSource(source);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<PolicyVO> policyPage = policyService.listPolicies(queryDTO);
        return Math.toIntExact(policyPage.getTotal());
    }
    
    /**
     * 统计指定领域的政策数量
     */
    private Integer countPoliciesByArea(String area) {
        PolicyQueryDTO queryDTO = new PolicyQueryDTO();
        queryDTO.setArea(area);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<PolicyVO> policyPage = policyService.listPolicies(queryDTO);
        return Math.toIntExact(policyPage.getTotal());
    }
    
    /**
     * 统计指定公司的竞品数量
     */
    private Integer countCompetitorsByCompany(String company) {
        CompetitorQueryDTO queryDTO = new CompetitorQueryDTO();
        queryDTO.setCompany(company);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<CompetitorVO> competitorPage = competitorService.listCompetitors(queryDTO);
        return Math.toIntExact(competitorPage.getTotal());
    }
    
    /**
     * 统计指定更新类型的竞品数量
     */
    private Integer countCompetitorsByType(String type) {
        CompetitorQueryDTO queryDTO = new CompetitorQueryDTO();
        queryDTO.setType(type);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<CompetitorVO> competitorPage = competitorService.listCompetitors(queryDTO);
        return Math.toIntExact(competitorPage.getTotal());
    }
    
    /**
     * 统计指定标签的竞品数量
     */
    private Integer countCompetitorsByTag(String tag) {
        CompetitorQueryDTO queryDTO = new CompetitorQueryDTO();
        queryDTO.setTag(tag);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        CommonPage<CompetitorVO> competitorPage = competitorService.listCompetitors(queryDTO);
        return Math.toIntExact(competitorPage.getTotal());
    }
    
    /**
     * 统计指定优先级的需求数量
     */
    private Integer countRequirementsByPriority(String priority) {
        RequirementQueryDTO queryDTO = new RequirementQueryDTO();
        queryDTO.setPriority(priority);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        
        // 使用正确的类型转换方式
        Page<RequirementVO> reqPage = requirementService.listRequirements(queryDTO);
        CommonPage<RequirementVO> requirementPage = CommonPage.restPage(reqPage);
        return Math.toIntExact(requirementPage.getTotal());
    }
    
    /**
     * 统计指定状态的需求数量
     */
    private Integer countRequirementsByStatus(String status) {
        RequirementQueryDTO queryDTO = new RequirementQueryDTO();
        queryDTO.setStatus(status);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        
        // 使用正确的类型转换方式
        Page<RequirementVO> reqPage = requirementService.listRequirements(queryDTO);
        CommonPage<RequirementVO> requirementPage = CommonPage.restPage(reqPage);
        return Math.toIntExact(requirementPage.getTotal());
    }
    
    /**
     * 统计指定来源类型的需求数量
     */
    private Integer countRequirementsBySourceType(String sourceType) {
        RequirementQueryDTO queryDTO = new RequirementQueryDTO();
        queryDTO.setSourceType(sourceType);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(1);
        
        // 使用正确的类型转换方式
        Page<RequirementVO> reqPage = requirementService.listRequirements(queryDTO);
        CommonPage<RequirementVO> requirementPage = CommonPage.restPage(reqPage);
        return Math.toIntExact(requirementPage.getTotal());
    }
    
    /**
     * 获取最近n天的每日数据
     */
    private List<DashboardStatsVO.DailyDataVO> getLastDaysData(int days) {
        List<DashboardStatsVO.DailyDataVO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1);
            
            DashboardStatsVO.DailyDataVO dailyData = new DashboardStatsVO.DailyDataVO();
            dailyData.setDate(date.format(formatter));
            
            // 获取当天政策数量
            PolicyQueryDTO policyQueryDTO = new PolicyQueryDTO();
            policyQueryDTO.setPublishStartTime(startOfDay);
            policyQueryDTO.setPublishEndTime(endOfDay);
            policyQueryDTO.setPageNum(1);
            policyQueryDTO.setPageSize(1);
            CommonPage<PolicyVO> policyPage = policyService.listPolicies(policyQueryDTO);
            dailyData.setPolicyCount(Math.toIntExact(policyPage.getTotal()));
            
            // 获取当天竞品数量
            CompetitorQueryDTO competitorQueryDTO = new CompetitorQueryDTO();
            competitorQueryDTO.setCaptureStartTime(startOfDay);
            competitorQueryDTO.setCaptureEndTime(endOfDay);
            competitorQueryDTO.setPageNum(1);
            competitorQueryDTO.setPageSize(1);
            CommonPage<CompetitorVO> competitorPage = competitorService.listCompetitors(competitorQueryDTO);
            dailyData.setCompetitorCount(Math.toIntExact(competitorPage.getTotal()));
            
            // 由于RequirementQueryDTO没有时间范围字段，这里使用关键词匹配日期
            RequirementQueryDTO requirementQueryDTO = new RequirementQueryDTO();
            requirementQueryDTO.setKeyword(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            requirementQueryDTO.setPageNum(1);
            requirementQueryDTO.setPageSize(1);
            
            // 使用正确的类型转换方式
            Page<RequirementVO> reqPage = requirementService.listRequirements(requirementQueryDTO);
            CommonPage<RequirementVO> requirementPage = CommonPage.restPage(reqPage);
            dailyData.setRequirementCount(Math.toIntExact(requirementPage.getTotal()));
            
            result.add(dailyData);
        }
        
        return result;
    }
} 