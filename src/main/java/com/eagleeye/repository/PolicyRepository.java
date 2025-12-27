package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.PolicyInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 政策信息数据访问接口
 */
@Mapper
public interface PolicyRepository extends BaseMapper<PolicyInfo> {
    
} 