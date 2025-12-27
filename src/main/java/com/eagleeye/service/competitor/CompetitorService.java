package com.eagleeye.service.competitor;

import com.eagleeye.common.api.CommonPage;
import com.eagleeye.model.dto.CompetitorQueryDTO;
import com.eagleeye.model.vo.CompetitorDetailVO;
import com.eagleeye.model.vo.CompetitorVO;

/**
 * 竞品动态服务接口
 */
public interface CompetitorService {
    
    /**
     * 分页查询竞品动态列表
     * @param queryDTO 查询条件
     * @return 分页竞品信息
     */
    CommonPage<CompetitorVO> listCompetitors(CompetitorQueryDTO queryDTO);
    
    /**
     * 获取竞品详情
     * @param id 竞品ID
     * @return 竞品详情
     */
    CompetitorDetailVO getCompetitorDetail(Long id);
    
    /**
     * 将竞品动态转为需求
     * @param id 竞品ID
     * @param userId 用户ID
     * @return 需求ID
     */
    Long convertToRequirement(Long id, Long userId);
} 