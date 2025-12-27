package com.eagleeye.service.competitor.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.eagleeye.common.api.CommonPage;
import com.eagleeye.model.dto.CompetitorQueryDTO;
import com.eagleeye.model.entity.CompetitorAnalysis;
import com.eagleeye.model.entity.CompetitorInfo;
import com.eagleeye.model.entity.CompetitorSource;
import com.eagleeye.model.entity.CompetitorTag;
import com.eagleeye.model.vo.CompetitorDetailVO;
import com.eagleeye.model.vo.CompetitorSourceVO;
import com.eagleeye.model.vo.CompetitorTagVO;
import com.eagleeye.model.vo.CompetitorVO;
import com.eagleeye.repository.CompetitorAnalysisRepository;
import com.eagleeye.repository.CompetitorRepository;
import com.eagleeye.repository.CompetitorSourceRepository;
import com.eagleeye.repository.CompetitorTagRepository;
import com.eagleeye.service.competitor.CompetitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * 竞品动态服务实现类
 */
@Service
public class CompetitorServiceImpl implements CompetitorService {
    
    private static final Logger log = LoggerFactory.getLogger(CompetitorServiceImpl.class);
    
    @Autowired
    private CompetitorRepository competitorRepository;
    
    @Autowired
    private CompetitorAnalysisRepository competitorAnalysisRepository;
    
    @Autowired
    private CompetitorSourceRepository competitorSourceRepository;
    
    @Autowired
    private CompetitorTagRepository competitorTagRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public CommonPage<CompetitorVO> listCompetitors(CompetitorQueryDTO queryDTO) {
        Page<CompetitorInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<CompetitorInfo> queryWrapper = buildQueryWrapper(queryDTO);

        Page<CompetitorInfo> infoPage = competitorRepository.selectPage(page, queryWrapper);

        List<CompetitorVO> competitorVOList = infoPage.getRecords().stream()
                .map(this::convertInfoToVOBase)
                .collect(Collectors.toList());

        log.info("Processing {} competitors for dashboard summary", competitorVOList.size());

        competitorVOList.forEach(vo -> {
            log.info("Fetching suggestion for competitor ID: {}", vo.getId());
            LambdaQueryWrapper<CompetitorAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
            analysisWrapper.eq(CompetitorAnalysis::getCompetitorId, vo.getId())
                    .orderByAsc(CompetitorAnalysis::getSortOrder);
            List<CompetitorAnalysis> analysisList = competitorAnalysisRepository.selectList(analysisWrapper);

            String suggestionContent = "暂无建议";
            if (analysisList != null && !analysisList.isEmpty()) {
                log.info("Found {} analysis records for ID: {}", analysisList.size(), vo.getId());
                CompetitorAnalysis firstAnalysis = analysisList.get(0);
                if (firstAnalysis != null) {
                    log.info("First analysis content for ID {}: '{}'", vo.getId(), firstAnalysis.getContent());
                    if (StringUtils.isNotBlank(firstAnalysis.getContent())) {
                        suggestionContent = firstAnalysis.getContent();
                    } else {
                        log.warn("First analysis content is blank for ID: {}", vo.getId());
        }
                } else {
                     log.warn("First analysis record is null (unexpected) for ID: {}", vo.getId());
                }
            } else {
                log.warn("No analysis records found for ID: {}", vo.getId());
            }
            vo.setSuggestion(suggestionContent);
            log.info("Set suggestion for ID {}: '{}'", vo.getId(), suggestionContent);

            // 填充标签 (逻辑不变)
            LambdaQueryWrapper<CompetitorTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(CompetitorTag::getCompetitorId, vo.getId());
            List<CompetitorTag> tagList = competitorTagRepository.selectList(tagWrapper);
            vo.setTags(tagList.stream()
                    .map(tag -> new CompetitorTagVO(tag.getLabel(), tag.getColor()))
                    .collect(Collectors.toList()));
        });

        IPage<CompetitorVO> voPage = new Page<>(infoPage.getCurrent(), infoPage.getSize(), infoPage.getTotal());
        voPage.setRecords(competitorVOList);
        voPage.setPages(infoPage.getPages());

        return CommonPage.restPage(voPage);
    }
    
    @Override
    public CompetitorDetailVO getCompetitorDetail(Long id) {
        // 获取竞品基本信息
        CompetitorInfo competitorInfo = competitorRepository.selectById(id);
        if (competitorInfo == null) {
            return null;
        }
        
        // 获取关联的分析建议
        LambdaQueryWrapper<CompetitorAnalysis> analysisWrapper = new LambdaQueryWrapper<>();
        analysisWrapper.eq(CompetitorAnalysis::getCompetitorId, id)
                .orderByAsc(CompetitorAnalysis::getSortOrder);
        List<CompetitorAnalysis> analysisList = competitorAnalysisRepository.selectList(analysisWrapper);
        
        // 获取关联的资源链接
        LambdaQueryWrapper<CompetitorSource> sourceWrapper = new LambdaQueryWrapper<>();
        sourceWrapper.eq(CompetitorSource::getCompetitorId, id)
                .orderByAsc(CompetitorSource::getSortOrder);
        List<CompetitorSource> sourceList = competitorSourceRepository.selectList(sourceWrapper);
        
        // 获取关联的标签
        LambdaQueryWrapper<CompetitorTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(CompetitorTag::getCompetitorId, id);
        List<CompetitorTag> tagList = competitorTagRepository.selectList(tagWrapper);
        
        // 构建详情VO
        CompetitorDetailVO detailVO = new CompetitorDetailVO();
        detailVO.setId(competitorInfo.getId());
        detailVO.setTitle(competitorInfo.getTitle());
        detailVO.setCompany(competitorInfo.getCompany());
        detailVO.setType(competitorInfo.getType());
        detailVO.setCaptureTime(competitorInfo.getCaptureTime());
        
        // 转换标签
        detailVO.setTags(tagList.stream().map(tag -> 
            new CompetitorTagVO(tag.getLabel(), tag.getColor())
        ).collect(Collectors.toList()));
        
        // 转换分析建议
        detailVO.setAnalysisAndSuggestions(analysisList.stream()
                .map(CompetitorAnalysis::getContent)
                .collect(Collectors.toList()));
        
        // 转换相关信息
        detailVO.setRelatedInfo(competitorInfo.getRelatedInfo());
        
        // 转换资源链接
        detailVO.setSources(sourceList.stream().map(source -> 
            new CompetitorSourceVO(source.getTitle(), source.getUrl())
        ).collect(Collectors.toList()));
        
        // 处理截图列表（如果有）
        if (StringUtils.isNotBlank(competitorInfo.getSources())) {
            try {
                List<String> screenshots = objectMapper.readValue(competitorInfo.getSources(), 
                        new TypeReference<List<String>>(){});
                detailVO.setScreenshots(screenshots);
            } catch (Exception e) {
                detailVO.setScreenshots(new ArrayList<>());
            }
        } else {
            detailVO.setScreenshots(new ArrayList<>());
        }
        
        return detailVO;
    }
    
    @Override
    public Long convertToRequirement(Long id, Long userId) {
        // 这里实现将竞品动态转为需求的逻辑
        // TODO: 实现实际的需求转化逻辑
        // 暂时仅返回一个模拟的需求ID
        return System.currentTimeMillis() % 1000 + 1000;
    }
    
    /**
     * 提取构建查询条件的逻辑
     */
    private LambdaQueryWrapper<CompetitorInfo> buildQueryWrapper(CompetitorQueryDTO queryDTO) {
        LambdaQueryWrapper<CompetitorInfo> queryWrapper = new LambdaQueryWrapper<>();
        // 关键词搜索
        if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
            queryWrapper.like(CompetitorInfo::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(CompetitorInfo::getContent, queryDTO.getKeyword());
        }
        // 按公司筛选
        if (StringUtils.isNotBlank(queryDTO.getCompany()) && !"全部机构".equals(queryDTO.getCompany())) {
            queryWrapper.eq(CompetitorInfo::getCompany, queryDTO.getCompany());
        }
        // 按类型筛选
        if (StringUtils.isNotBlank(queryDTO.getType()) && !"全部分类".equals(queryDTO.getType())) {
            queryWrapper.eq(CompetitorInfo::getType, queryDTO.getType());
        }
        // 按标签筛选
        if (StringUtils.isNotBlank(queryDTO.getTag())) {
            queryWrapper.like(CompetitorInfo::getTags, queryDTO.getTag());
        }
        // 时间范围筛选
        if (queryDTO.getCaptureStartTime() != null) {
            queryWrapper.ge(CompetitorInfo::getCaptureTime, queryDTO.getCaptureStartTime());
        }
        if (queryDTO.getCaptureEndTime() != null) {
            queryWrapper.le(CompetitorInfo::getCaptureTime, queryDTO.getCaptureEndTime());
        }
        // 按抓取时间倒序排序
        queryWrapper.orderByDesc(CompetitorInfo::getCaptureTime);
        return queryWrapper;
    }
    
    /**
     * 将 CompetitorInfo 转换为 CompetitorVO (仅基础字段)
     */
    private CompetitorVO convertInfoToVOBase(CompetitorInfo competitorInfo) {
        CompetitorVO competitorVO = new CompetitorVO();
        competitorVO.setId(competitorInfo.getId());
        competitorVO.setTitle(competitorInfo.getTitle());
        competitorVO.setBankName(competitorInfo.getCompany());
        competitorVO.setUpdateType(competitorInfo.getType());
        competitorVO.setUpdateTime(competitorInfo.getCaptureTime());
        return competitorVO;
    }
}