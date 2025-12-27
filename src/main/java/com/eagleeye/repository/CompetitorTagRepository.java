package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.CompetitorTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞品标签数据访问接口
 */
@Mapper
public interface CompetitorTagRepository extends BaseMapper<CompetitorTag> {
} 