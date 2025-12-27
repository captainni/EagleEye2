package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.PolicyAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 政策分析数据访问接口
 */
@Mapper
public interface PolicyAnalysisRepository extends BaseMapper<PolicyAnalysis> {
    
} 