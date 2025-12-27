package com.eagleeye.service.policy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.model.dto.PolicyQueryDTO;
import com.eagleeye.model.entity.PolicyAnalysis;
import com.eagleeye.model.entity.PolicyInfo;
import com.eagleeye.model.entity.PolicySuggestion;
import com.eagleeye.model.vo.PolicyDetailVO;
import com.eagleeye.model.vo.PolicyVO;
import com.eagleeye.model.vo.PolicySuggestionVO;
import com.eagleeye.repository.PolicyAnalysisRepository;
import com.eagleeye.repository.PolicyRepository;
import com.eagleeye.repository.PolicySuggestionRepository;
import com.eagleeye.service.policy.PolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 政策服务实现类
 */
@Slf4j
@Service
public class PolicyServiceImpl implements PolicyService {
    
    @Resource
    private PolicyRepository policyRepository;
    
    @Resource
    private PolicyAnalysisRepository policyAnalysisRepository;
    
    @Resource
    private PolicySuggestionRepository policySuggestionRepository;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Override
    public CommonPage<PolicyVO> listPolicies(PolicyQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<PolicyInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
            queryWrapper.like(PolicyInfo::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(PolicyInfo::getContent, queryDTO.getKeyword());
        }
        
        // 政策类型过滤
        if (StringUtils.isNotBlank(queryDTO.getPolicyType())) {
            queryWrapper.eq(PolicyInfo::getPolicyType, queryDTO.getPolicyType());
        }
        
        // 政策来源过滤
        if (StringUtils.isNotBlank(queryDTO.getSource())) {
            queryWrapper.eq(PolicyInfo::getSource, queryDTO.getSource());
        }
        
        // 重要程度过滤
        if (StringUtils.isNotBlank(queryDTO.getImportance())) {
            queryWrapper.eq(PolicyInfo::getImportance, queryDTO.getImportance());
        }
        
        // 相关领域过滤
        if (StringUtils.isNotBlank(queryDTO.getArea())) {
            queryWrapper.like(PolicyInfo::getAreas, queryDTO.getArea());
        }
        
        // 发布时间范围过滤
        if (queryDTO.getPublishStartTime() != null) {
            queryWrapper.ge(PolicyInfo::getPublishTime, queryDTO.getPublishStartTime());
        }
        if (queryDTO.getPublishEndTime() != null) {
            queryWrapper.le(PolicyInfo::getPublishTime, queryDTO.getPublishEndTime());
        }
        
        // 按发布时间倒序排序
        queryWrapper.orderByDesc(PolicyInfo::getPublishTime);
        
        // 执行分页查询
        Page<PolicyInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<PolicyInfo> policyPage = policyRepository.selectPage(page, queryWrapper);
        
        // 转换为VO对象
        List<PolicyVO> policyVOList = policyPage.getRecords().stream()
                .map(this::convertToPolicyVO)
                .collect(Collectors.toList());
        
        // 封装为通用分页结果
        CommonPage<PolicyVO> result = new CommonPage<>();
        result.setPageNum(policyPage.getCurrent());
        result.setPageSize(policyPage.getSize());
        result.setTotal(policyPage.getTotal());
        result.setTotalPage(policyPage.getPages());
        result.setList(policyVOList);
        
        return result;
    }
    
    @Override
    public PolicyDetailVO getPolicyDetail(Long id) {
        // 1. 查询政策信息
        PolicyInfo policyInfo = policyRepository.selectById(id);
        if (policyInfo == null) {
            return null;
        }
        
        // 2. 查询政策分析 (LIMIT 1)
        LambdaQueryWrapper<PolicyAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
        analysisWrapper.eq(PolicyAnalysis::getPolicyId, id);
        analysisWrapper.last("LIMIT 1");
        PolicyAnalysis policyAnalysis = policyAnalysisRepository.selectOne(analysisWrapper);
        
        // 3. 查询政策建议
        LambdaQueryWrapper<PolicySuggestion> suggestionWrapper = new LambdaQueryWrapper<>();
        suggestionWrapper.eq(PolicySuggestion::getPolicyId, id);
        List<PolicySuggestion> policySuggestions = policySuggestionRepository.selectList(suggestionWrapper);
        
        // 4. 直接构建并填充 PolicyDetailVO
        PolicyDetailVO policyDetailVO = new PolicyDetailVO();
        BeanUtils.copyProperties(policyInfo, policyDetailVO); // 拷贝基础信息
        
        // 设置原文
        policyDetailVO.setContent(policyInfo.getContent());
        
        // 设置分析相关字段
        if (policyAnalysis != null) {
            policyDetailVO.setSummary(policyAnalysis.getSummary());
            policyDetailVO.setImpactAnalysis(policyAnalysis.getImpactAnalysis());
            // 解析keyPoints JSON为List
            if (StringUtils.isNotBlank(policyAnalysis.getKeyPoints())) {
                try {
                    List<String> keyPointsList = objectMapper.readValue(policyAnalysis.getKeyPoints(),
                            new TypeReference<List<String>>() {});
                    policyDetailVO.setKeyPoints(keyPointsList);
                } catch (JsonProcessingException e) {
                    log.error("解析政策关键条款JSON出错 (Detail): {}", e.getMessage());
                    policyDetailVO.setKeyPoints(new ArrayList<>());
                }
            } else {
                policyDetailVO.setKeyPoints(new ArrayList<>());
            }
        }
        
         // 解析areas JSON为List (从 PolicyInfo 获取)
        if (StringUtils.isNotBlank(policyInfo.getAreas())) {
            try {
                List<String> areaList = objectMapper.readValue(policyInfo.getAreas(), 
                        new TypeReference<List<String>>() {});
                policyDetailVO.setAreas(areaList);
            } catch (JsonProcessingException e) {
                log.error("解析政策领域JSON出错 (Detail): {}", e.getMessage());
                policyDetailVO.setAreas(new ArrayList<>());
            }
        } else {
            policyDetailVO.setAreas(new ArrayList<>());
        }

        // 转换并设置建议列表
        List<PolicySuggestionVO> suggestionVOList = policySuggestions.stream()
                .map(suggestion -> {
                    PolicySuggestionVO vo = new PolicySuggestionVO();
                    vo.setId(suggestion.getId());
                    vo.setSuggestion(suggestion.getSuggestion());
                    vo.setReason(suggestion.getReason());
                    return vo;
                })
                .collect(Collectors.toList());
        policyDetailVO.setSuggestions(suggestionVOList);
        
        return policyDetailVO;
    }
    
    @Override
    public Long convertToRequirement(Long policyId, Long userId) {
        // TODO: 实现将政策转为需求的功能
        // 这里是一个示意，实际实现需要创建需求表相关代码
        log.info("将政策(id={})转为需求, 用户ID: {}", policyId, userId);
        return -1L;
    }
    
    /**
     * 将政策实体转换为VO对象
     */
    private PolicyVO convertToPolicyVO(PolicyInfo policyInfo) {
        PolicyVO policyVO = new PolicyVO();
        BeanUtils.copyProperties(policyInfo, policyVO);
        
        // 解析areas JSON为List
        if (StringUtils.isNotBlank(policyInfo.getAreas())) {
            try {
                List<String> areaList = objectMapper.readValue(policyInfo.getAreas(), 
                        new TypeReference<List<String>>() {});
                policyVO.setAreas(areaList);
            } catch (JsonProcessingException e) {
                log.error("解析政策领域JSON出错: {}", e.getMessage());
                policyVO.setAreas(new ArrayList<>());
            }
        } else {
            policyVO.setAreas(new ArrayList<>());
        }
        
        // 查询政策分析
        LambdaQueryWrapper<PolicyAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
        analysisWrapper.eq(PolicyAnalysis::getPolicyId, policyInfo.getId());
        analysisWrapper.last("LIMIT 1");
        PolicyAnalysis policyAnalysis = policyAnalysisRepository.selectOne(analysisWrapper);
        
        // 设置摘要和关键条款
        if (policyAnalysis != null) {
            policyVO.setSummary(policyAnalysis.getSummary());
            
            // 解析keyPoints JSON为List
            if (StringUtils.isNotBlank(policyAnalysis.getKeyPoints())) {
                try {
                    List<String> keyPointsList = objectMapper.readValue(policyAnalysis.getKeyPoints(),
                            new TypeReference<List<String>>() {});
                    policyVO.setKeyPoints(keyPointsList);
                } catch (JsonProcessingException e) {
                    log.error("解析政策关键条款JSON出错: {}", e.getMessage());
                    policyVO.setKeyPoints(new ArrayList<>());
                }
            } else {
                policyVO.setKeyPoints(new ArrayList<>());
            }
        }
        
        // 查询政策建议
        LambdaQueryWrapper<PolicySuggestion> suggestionWrapper = new LambdaQueryWrapper<>();
        suggestionWrapper.eq(PolicySuggestion::getPolicyId, policyInfo.getId());
        List<PolicySuggestion> policySuggestions = policySuggestionRepository.selectList(suggestionWrapper);
        
        // 转换建议列表
        List<PolicySuggestionVO> suggestionVOList = policySuggestions.stream()
                .map(suggestion -> {
                    PolicySuggestionVO vo = new PolicySuggestionVO();
                    vo.setId(suggestion.getId());
                    vo.setSuggestion(suggestion.getSuggestion());
                    vo.setReason(suggestion.getReason());
                    return vo;
                })
                .collect(Collectors.toList());
        
        policyVO.setSuggestions(suggestionVOList);
        
        return policyVO;
    }
} 