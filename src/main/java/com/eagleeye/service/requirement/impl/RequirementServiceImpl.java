package com.eagleeye.service.requirement.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.model.dto.RequirementCreateDTO;
import com.eagleeye.model.dto.RequirementQueryDTO;
import com.eagleeye.model.dto.RequirementUpdateDTO;
import com.eagleeye.model.entity.Requirement;
import com.eagleeye.model.entity.CompetitorInfo;
import com.eagleeye.model.entity.PolicyInfo;
import com.eagleeye.model.entity.PolicyAnalysis;
import com.eagleeye.model.entity.PolicySuggestion;
import com.eagleeye.model.entity.CompetitorAnalysis;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eagleeye.model.vo.AttachmentVO;
import com.eagleeye.model.vo.RequirementDetailVO;
import com.eagleeye.model.vo.RequirementVO;
import com.eagleeye.repository.RequirementRepository;
import com.eagleeye.repository.PolicyRepository;
import com.eagleeye.repository.PolicyAnalysisRepository;
import com.eagleeye.repository.PolicySuggestionRepository;
import com.eagleeye.repository.CompetitorRepository;
import com.eagleeye.repository.CompetitorAnalysisRepository;
import com.eagleeye.service.requirement.RequirementService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 需求服务实现类
 */
@Service
public class RequirementServiceImpl implements RequirementService {

    @Resource
    private RequirementRepository requirementRepository;

    @Resource
    private PolicyRepository policyRepository;

    @Resource
    private PolicyAnalysisRepository policyAnalysisRepository;

    @Resource
    private PolicySuggestionRepository policySuggestionRepository;

    @Resource
    private CompetitorRepository competitorRepository;

    @Resource
    private CompetitorAnalysisRepository competitorAnalysisRepository;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public Page<RequirementVO> listRequirements(RequirementQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Requirement> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（标题或描述）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(Requirement::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Requirement::getDescription, queryDTO.getKeyword());
        }
        
        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(Requirement::getStatus, queryDTO.getStatus());
        }
        
        // 优先级筛选
        if (StringUtils.hasText(queryDTO.getPriority())) {
            queryWrapper.eq(Requirement::getPriority, queryDTO.getPriority());
        }
        
        // 来源类型筛选
        if (StringUtils.hasText(queryDTO.getSourceType())) {
            queryWrapper.eq(Requirement::getSourceType, queryDTO.getSourceType());
        }
        
        // 用户ID筛选
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(Requirement::getUserId, queryDTO.getUserId());
        }
        
        // 默认按创建时间倒序排序
        queryWrapper.orderByDesc(Requirement::getCreateTime);
        
        // 执行分页查询
        Page<Requirement> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Requirement> resultPage = requirementRepository.selectPage(page, queryWrapper);
        
        // 转换为VO对象
        Page<RequirementVO> voPage = new Page<>();
        BeanUtils.copyProperties(resultPage, voPage, "records");
        
        List<RequirementVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public RequirementDetailVO getRequirementDetail(Long id) {
        Requirement requirement = requirementRepository.selectById(id);
        if (requirement == null) {
            return null;
        }
        
        RequirementDetailVO detailVO = new RequirementDetailVO();
        BeanUtils.copyProperties(requirement, detailVO);
        
        // 设置状态文本和类型
        setStatusInfo(detailVO, requirement.getStatus());
        
        // 设置优先级等级
        setPriorityInfo(detailVO, requirement.getPriority());
        
        // 设置来源详情
        setSourceDetail(detailVO, requirement.getSourceType(), requirement.getSourceId());
        
        // 模拟设置一些附件数据
        detailVO.setAttachments(getMockAttachments(requirement.getSourceType()));
        
        // TODO: 设置负责人信息（需要用户服务）
        detailVO.setOwner("模拟负责人");
        
        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRequirement(RequirementCreateDTO createDTO) {
        Requirement requirement = new Requirement();
        BeanUtils.copyProperties(createDTO, requirement);
        
        // 设置默认状态
        if (!StringUtils.hasText(requirement.getStatus())) {
            requirement.setStatus("NEW");
        }
        
        // 设置创建时间等
        requirement.setCreateTime(LocalDateTime.now());
        requirement.setUpdateTime(LocalDateTime.now());
        requirement.setIsDeleted(false);
        
        requirementRepository.insert(requirement);
        return requirement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRequirement(RequirementUpdateDTO updateDTO) {
        Requirement requirement = requirementRepository.selectById(updateDTO.getId());
        if (requirement == null) {
            return false;
        }
        
        BeanUtils.copyProperties(updateDTO, requirement);
        requirement.setUpdateTime(LocalDateTime.now());
        
        return requirementRepository.updateById(requirement) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRequirement(Long id) {
        return requirementRepository.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertPolicyToRequirement(Long policyId) {
        // 获取政策信息
        PolicyInfo policyInfo = policyRepository.selectById(policyId);
        if (policyInfo == null) {
            return null;
        }
        
        // 获取政策分析
        LambdaQueryWrapper<PolicyAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
        analysisWrapper.eq(PolicyAnalysis::getPolicyId, policyId);
        PolicyAnalysis policyAnalysis = policyAnalysisRepository.selectOne(analysisWrapper);
        
        // 获取政策建议
        LambdaQueryWrapper<PolicySuggestion> suggestionWrapper = new LambdaQueryWrapper<>();
        suggestionWrapper.eq(PolicySuggestion::getPolicyId, policyId);
        List<PolicySuggestion> suggestions = policySuggestionRepository.selectList(suggestionWrapper);
        
        // 创建需求
        RequirementCreateDTO createDTO = new RequirementCreateDTO();
        createDTO.setTitle(policyInfo.getTitle());
        createDTO.setSourceType("POLICY");
        createDTO.setSourceId(policyId);
        
        // 设置背景和描述
        StringBuilder background = new StringBuilder();
        background.append("政策来源：").append(policyInfo.getSource()).append("\n");
        background.append("发布时间：").append(policyInfo.getPublishTime()).append("\n");
        background.append("政策类型：").append(policyInfo.getPolicyType()).append("\n\n");
        
        if (policyAnalysis != null) {
            background.append("政策摘要：\n").append(policyAnalysis.getSummary());
        }
        
        createDTO.setBackground(background.toString());
        
        StringBuilder description = new StringBuilder();
        description.append("根据政策内容，需要重点关注以下要点：\n\n");
        
        if (policyAnalysis != null && policyAnalysis.getImpactAnalysis() != null) {
            description.append("影响分析：\n").append(policyAnalysis.getImpactAnalysis()).append("\n\n");
        }
        
        if (suggestions != null && !suggestions.isEmpty()) {
            description.append("建议事项：\n");
            for (int i = 0; i < suggestions.size(); i++) {
                PolicySuggestion suggestion = suggestions.get(i);
                description.append(i + 1).append(". ").append(suggestion.getSuggestion()).append("\n");
                if (StringUtils.hasText(suggestion.getReason())) {
                    description.append("   原因：").append(suggestion.getReason()).append("\n");
                }
            }
        }
        
        createDTO.setDescription(description.toString());
        createDTO.setPriority("MEDIUM"); // 默认优先级为中
        createDTO.setStatus("NEW"); // 默认状态为新建
        
        return createRequirement(createDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertCompetitorToRequirement(Long competitorId) {
        // 获取竞品信息
        CompetitorInfo competitorInfo = competitorRepository.selectById(competitorId);
        if (competitorInfo == null) {
            return null;
        }

        // 获取竞品分析数据
        LambdaQueryWrapper<CompetitorAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
        analysisWrapper.eq(CompetitorAnalysis::getCompetitorId, competitorId)
                .eq(CompetitorAnalysis::getSortOrder, 1);
        CompetitorAnalysis competitorAnalysis = competitorAnalysisRepository.selectOne(analysisWrapper);

        // 创建需求
        RequirementCreateDTO createDTO = new RequirementCreateDTO();
        createDTO.setTitle(competitorInfo.getTitle());
        createDTO.setSourceType("COMPETITOR");
        createDTO.setSourceId(competitorId);

        // 设置背景信息
        StringBuilder background = new StringBuilder();
        background.append("竞品公司：").append(competitorInfo.getCompany()).append("\n");
        background.append("动态类型：").append(competitorInfo.getType()).append("\n");
        background.append("抓取时间：").append(competitorInfo.getCaptureTime()).append("\n\n");

        if (StringUtils.hasText(competitorInfo.getSummary())) {
            background.append("分析摘要：\n").append(competitorInfo.getSummary());
        }

        createDTO.setBackground(background.toString());

        // 设置描述信息 - 参考政策转需求的结构化方式
        StringBuilder description = new StringBuilder();
        description.append("竞品动态分析：\n\n");

        // 市场影响分析
        if (competitorAnalysis != null && StringUtils.hasText(competitorAnalysis.getMarketImpact())) {
            description.append("市场影响：\n").append(competitorAnalysis.getMarketImpact()).append("\n\n");
        } else if (StringUtils.hasText(competitorInfo.getMarketImpact())) {
            description.append("市场影响：\n").append(competitorInfo.getMarketImpact()).append("\n\n");
        }

        // 竞争态势分析
        if (competitorAnalysis != null && StringUtils.hasText(competitorAnalysis.getCompetitiveAnalysis())) {
            description.append("竞争态势：\n").append(competitorAnalysis.getCompetitiveAnalysis()).append("\n\n");
        } else if (StringUtils.hasText(competitorInfo.getRelatedInfo())) {
            description.append("竞争态势：\n").append(competitorInfo.getRelatedInfo()).append("\n\n");
        }

        // 应对建议列表
        description.append("应对建议：\n");

        // 尝试从 competitor_analysis 的 our_suggestions JSON 字段解析建议
        if (competitorAnalysis != null && StringUtils.hasText(competitorAnalysis.getOurSuggestions())) {
            try {
                java.util.List<com.eagleeye.model.dto.CompetitorAnalysisResult.Suggestion> suggestions =
                        objectMapper.readValue(competitorAnalysis.getOurSuggestions(),
                                new TypeReference<java.util.List<com.eagleeye.model.dto.CompetitorAnalysisResult.Suggestion>>() {});

                if (!suggestions.isEmpty()) {
                    for (int i = 0; i < suggestions.size(); i++) {
                        com.eagleeye.model.dto.CompetitorAnalysisResult.Suggestion suggestion = suggestions.get(i);
                        description.append(i + 1).append(". ").append(suggestion.getSuggestion()).append("\n");
                        if (StringUtils.hasText(suggestion.getReason())) {
                            description.append("   理由：").append(suggestion.getReason()).append("\n");
                        }
                    }
                } else {
                    description.append("1. 分析竞品动态对我方产品的影响\n");
                    description.append("2. 评估是否需要调整产品策略\n");
                    description.append("3. 制定相应的产品改进计划\n");
                }
            } catch (Exception e) {
                // JSON 解析失败，使用默认建议
                description.append("1. 分析竞品动态对我方产品的影响\n");
                description.append("2. 评估是否需要调整产品策略\n");
                description.append("3. 制定相应的产品改进计划\n");
            }
        } else {
            description.append("1. 分析竞品动态对我方产品的影响\n");
            description.append("2. 评估是否需要调整产品策略\n");
            description.append("3. 制定相应的产品改进计划\n");
        }

        createDTO.setDescription(description.toString());
        createDTO.setPriority("MEDIUM"); // 默认优先级为中
        createDTO.setStatus("NEW"); // 默认状态为新建

        return createRequirement(createDTO);
    }

    /**
     * 将需求实体转换为VO对象
     */
    private RequirementVO convertToVO(Requirement requirement) {
        RequirementVO vo = new RequirementVO();
        BeanUtils.copyProperties(requirement, vo);
        
        // 设置状态文本和类型
        setStatusInfo(vo, requirement.getStatus());
        
        // 设置优先级等级
        setPriorityInfo(vo, requirement.getPriority());
        
        // 设置来源详情
        setSourceDetail(vo, requirement.getSourceType(), requirement.getSourceId());
        
        // 截取简短描述
        if (StringUtils.hasText(requirement.getDescription())) {
            String desc = requirement.getDescription();
            vo.setBriefDescription(desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);
        }
        
        // TODO: 设置负责人信息（需要用户服务）
        vo.setOwner("模拟负责人");
        
        return vo;
    }

    /**
     * 设置状态文本和类型 (RequirementVO版本)
     */
    private void setStatusInfo(RequirementVO vo, String status) {
        if ("NEW".equals(status)) {
            vo.setStatusText("待处理");
            vo.setStatusType("orange");
        } else if ("PROCESSING".equals(status)) {
            vo.setStatusText("处理中");
            vo.setStatusType("warning");
        } else if ("COMPLETED".equals(status)) {
            vo.setStatusText("已处理");
            vo.setStatusType("success");
        } else if ("REJECTED".equals(status)) {
            vo.setStatusText("已拒绝");
            vo.setStatusType("gray");
        } else {
            vo.setStatusText("未知状态");
            vo.setStatusType("info");
        }
    }
    
    /**
     * 设置状态文本和类型 (RequirementDetailVO版本)
     */
    private void setStatusInfo(RequirementDetailVO vo, String status) {
        if ("NEW".equals(status)) {
            vo.setStatusText("待处理");
            vo.setStatusType("orange");
        } else if ("PROCESSING".equals(status)) {
            vo.setStatusText("处理中");
            vo.setStatusType("warning");
        } else if ("COMPLETED".equals(status)) {
            vo.setStatusText("已处理");
            vo.setStatusType("success");
        } else if ("REJECTED".equals(status)) {
            vo.setStatusText("已拒绝");
            vo.setStatusType("gray");
        } else {
            vo.setStatusText("未知状态");
            vo.setStatusType("info");
        }
    }

    /**
     * 设置优先级等级 (RequirementVO版本)
     */
    private void setPriorityInfo(RequirementVO vo, String priority) {
        if ("HIGH".equals(priority)) {
            vo.setPriority("高");
            vo.setPriorityLevel("high");
        } else if ("MEDIUM".equals(priority)) {
            vo.setPriority("中");
            vo.setPriorityLevel("medium");
        } else if ("LOW".equals(priority)) {
            vo.setPriority("低");
            vo.setPriorityLevel("low");
        } else {
            vo.setPriority("未知");
            vo.setPriorityLevel("medium");
        }
    }
    
    /**
     * 设置优先级等级 (RequirementDetailVO版本)
     */
    private void setPriorityInfo(RequirementDetailVO vo, String priority) {
        if ("HIGH".equals(priority)) {
            vo.setPriority("高");
            vo.setPriorityLevel("high");
        } else if ("MEDIUM".equals(priority)) {
            vo.setPriority("中");
            vo.setPriorityLevel("medium");
        } else if ("LOW".equals(priority)) {
            vo.setPriority("低");
            vo.setPriorityLevel("low");
        } else {
            vo.setPriority("未知");
            vo.setPriorityLevel("medium");
        }
    }
    
    /**
     * 设置来源详情 (RequirementVO版本)
     */
    private void setSourceDetail(RequirementVO vo, String sourceType, Long sourceId) {
        if ("POLICY".equals(sourceType) && sourceId != null) {
            // 获取政策信息
            PolicyInfo policyInfo = policyRepository.selectById(sourceId);
            if (policyInfo != null) {
                vo.setSourceDetail(policyInfo.getSource());
            } else {
                vo.setSourceDetail("监管政策");
            }
        } else if ("COMPETITOR".equals(sourceType) && sourceId != null) {
            // 获取竞品信息
            CompetitorInfo competitorInfo = competitorRepository.selectById(sourceId);
            if (competitorInfo != null) {
                vo.setSourceDetail(competitorInfo.getCompany());
            } else {
                vo.setSourceDetail("竞品动态");
            }
        } else if ("MANUAL".equals(sourceType)) {
            vo.setSourceDetail("");
        } else {
            vo.setSourceDetail("未知来源");
        }
    }
    
    /**
     * 设置来源详情 (RequirementDetailVO版本)
     */
    private void setSourceDetail(RequirementDetailVO vo, String sourceType, Long sourceId) {
        if ("POLICY".equals(sourceType) && sourceId != null) {
            // 获取政策信息
            PolicyInfo policyInfo = policyRepository.selectById(sourceId);
            if (policyInfo != null) {
                vo.setSourceDetail(policyInfo.getSource());
            } else {
                vo.setSourceDetail("监管政策");
            }
        } else if ("COMPETITOR".equals(sourceType) && sourceId != null) {
            // 获取竞品信息
            CompetitorInfo competitorInfo = competitorRepository.selectById(sourceId);
            if (competitorInfo != null) {
                vo.setSourceDetail(competitorInfo.getCompany());
            } else {
                vo.setSourceDetail("竞品动态");
            }
        } else if ("MANUAL".equals(sourceType)) {
            vo.setSourceDetail("");
        } else {
            vo.setSourceDetail("未知来源");
        }
    }
    
    /**
     * 模拟获取附件数据
     */
    private List<AttachmentVO> getMockAttachments(String sourceType) {
        List<AttachmentVO> attachments = new ArrayList<>();
        
        if ("POLICY".equals(sourceType)) {
            AttachmentVO attachment1 = new AttachmentVO();
            attachment1.setId(1L);
            attachment1.setName("相关政策文件.pdf");
            attachment1.setType("file-alt");
            attachment1.setUrl("#");
            
            AttachmentVO attachment2 = new AttachmentVO();
            attachment2.setId(2L);
            attachment2.setName("政策解读文档.docx");
            attachment2.setType("file-word");
            attachment2.setUrl("#");
            
            attachments.add(attachment1);
            attachments.add(attachment2);
        } else if ("COMPETITOR".equals(sourceType)) {
            AttachmentVO attachment = new AttachmentVO();
            attachment.setId(1L);
            attachment.setName("竞品分析报告.docx");
            attachment.setType("file-word");
            attachment.setUrl("#");
            
            attachments.add(attachment);
        }
        
        return attachments;
    }
}