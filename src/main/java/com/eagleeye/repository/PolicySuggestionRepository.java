package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.PolicySuggestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 政策建议数据访问接口
 */
@Mapper
public interface PolicySuggestionRepository extends BaseMapper<PolicySuggestion> {
    
} 