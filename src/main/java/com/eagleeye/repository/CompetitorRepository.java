package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.CompetitorInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞品信息数据访问接口
 */
@Mapper
public interface CompetitorRepository extends BaseMapper<CompetitorInfo> {
} 