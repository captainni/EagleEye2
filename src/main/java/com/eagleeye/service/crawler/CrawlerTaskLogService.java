package com.eagleeye.service.crawler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.model.dto.TaskLogQueryDTO;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.model.vo.CrawlerTaskLogVO;

/**
 * 爬虫任务日志服务接口
 *
 * @author eagleeye
 */
public interface CrawlerTaskLogService {

    /**
     * 分页查询爬虫任务日志
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<CrawlerTaskLogVO> listTaskLogs(TaskLogQueryDTO queryDTO);

    /**
     * 保存任务日志（供MQ消费者调用）
     *
     * @param taskLog 任务日志实体
     * @return 是否保存成功
     */
    boolean saveTaskLog(CrawlerTaskLog taskLog);

    /**
     * 更新任务日志（供触发器失败时调用）
     *
     * @param taskLog 包含ID的任务日志实体
     * @return 是否更新成功
     */
    boolean updateTaskLog(CrawlerTaskLog taskLog);

    /**
     * 根据任务ID查询日志
     *
     * @param taskId 任务ID
     * @return 日志详情
     */
    CrawlerTaskLogVO getByTaskId(String taskId);
} 