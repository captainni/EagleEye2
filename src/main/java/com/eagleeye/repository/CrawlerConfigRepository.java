package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.CrawlerConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫配置表 Mapper 接口
 */
@Mapper
public interface CrawlerConfigRepository extends BaseMapper<CrawlerConfig> {

} 