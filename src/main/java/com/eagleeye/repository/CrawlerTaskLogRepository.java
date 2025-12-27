package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.CrawlerTaskLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫任务执行日志表 Mapper 接口
 *
 * @author eagleeye
 */
@Mapper
public interface CrawlerTaskLogRepository extends BaseMapper<CrawlerTaskLog> {

} 