package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.CompetitorAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞品分析数据访问接口
 */
@Mapper
public interface CompetitorAnalysisRepository extends BaseMapper<CompetitorAnalysis> {
} 