package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.CompetitorSource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞品资源数据访问接口
 */
@Mapper
public interface CompetitorSourceRepository extends BaseMapper<CompetitorSource> {
} 