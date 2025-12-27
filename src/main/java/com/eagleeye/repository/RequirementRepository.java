package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.Requirement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 需求数据访问接口
 */
@Mapper
public interface RequirementRepository extends BaseMapper<Requirement> {
} 