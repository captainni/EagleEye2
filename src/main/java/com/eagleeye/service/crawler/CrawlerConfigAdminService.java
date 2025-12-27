package com.eagleeye.service.crawler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.model.dto.CrawlerConfigCreateDTO;
import com.eagleeye.model.dto.CrawlerConfigQueryDTO;
import com.eagleeye.model.dto.CrawlerConfigUpdateDTO;
import com.eagleeye.model.vo.CrawlerConfigDetailVO;
import com.eagleeye.model.vo.CrawlerConfigVO;

/**
 * 爬虫配置管理服务接口 (管理员)
 */
public interface CrawlerConfigAdminService {

    /**
     * 创建爬虫配置
     * @param createDTO 创建参数
     * @return 新配置的ID
     */
    Long createConfig(CrawlerConfigCreateDTO createDTO);

    /**
     * 分页查询爬虫配置列表
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<CrawlerConfigVO> listConfigs(CrawlerConfigQueryDTO queryDTO);

    /**
     * 获取爬虫配置详情
     * @param configId 配置ID
     * @return 配置详情
     */
    CrawlerConfigDetailVO getConfigDetail(Long configId);

    /**
     * 更新爬虫配置
     * @param configId 配置ID
     * @param updateDTO 更新参数
     * @return 是否成功
     */
    boolean updateConfig(Long configId, CrawlerConfigUpdateDTO updateDTO);

    /**
     * 删除爬虫配置
     * @param configId 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(Long configId);

    /**
     * 更新爬虫配置的激活状态
     * @param configId 配置ID
     * @param isActive 是否激活
     * @return 是否成功
     */
    boolean updateConfigStatus(Long configId, Boolean isActive);

    /**
     * 手动触发一次爬虫任务 (发送任务到MQ)
     * @param configId 配置ID
     * @return 是否成功触发 (不代表任务执行成功)
     */
    boolean triggerConfig(Long configId);
} 