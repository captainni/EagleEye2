package com.eagleeye.service.crawler.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eagleeye.model.dto.TaskLogQueryDTO;
import com.eagleeye.model.entity.CrawlerConfig;
import com.eagleeye.model.entity.CrawlerTaskLog;
import com.eagleeye.model.vo.CrawlerTaskLogVO;
import com.eagleeye.repository.CrawlerConfigRepository;
import com.eagleeye.repository.CrawlerTaskLogRepository;
import com.eagleeye.service.crawler.CrawlerTaskLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 爬虫任务日志服务实现类
 *
 * @author eagleeye
 */
@Slf4j
@Service
public class CrawlerTaskLogServiceImpl extends ServiceImpl<CrawlerTaskLogRepository, CrawlerTaskLog> implements CrawlerTaskLogService {

    @Resource
    private CrawlerConfigRepository crawlerConfigRepository;

    @Override
    public Page<CrawlerTaskLogVO> listTaskLogs(TaskLogQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<CrawlerTaskLog> queryWrapper = Wrappers.lambdaQuery(CrawlerTaskLog.class)
                .eq(CrawlerTaskLog::getIsDeleted, false)
                .eq(StringUtils.hasText(queryDTO.getStatus()), CrawlerTaskLog::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getConfigId() != null, CrawlerTaskLog::getConfigId, queryDTO.getConfigId())
                .ge(queryDTO.getStartTime() != null, CrawlerTaskLog::getStartTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, CrawlerTaskLog::getEndTime, queryDTO.getEndTime())
                .orderByDesc(CrawlerTaskLog::getEndTime); // 默认按结束时间降序排序

        // 执行分页查询
        Page<CrawlerTaskLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<CrawlerTaskLog> logPage = this.page(page, queryWrapper);

        // 转换为VO
        List<Long> configIds = logPage.getRecords().stream()
                .map(CrawlerTaskLog::getConfigId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询配置名称
        Map<Long, String> configNameMap = getConfigNameMap(configIds);

        // 组装最终结果
        Page<CrawlerTaskLogVO> resultPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<CrawlerTaskLogVO> voList = logPage.getRecords().stream()
                .map(log -> {
                    CrawlerTaskLogVO vo = new CrawlerTaskLogVO();
                    BeanUtils.copyProperties(log, vo);
                    // 填充配置名称
                    vo.setConfigName(configNameMap.getOrDefault(log.getConfigId(), "未知配置"));
                    return vo;
                })
                .collect(Collectors.toList());

        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    public boolean saveTaskLog(CrawlerTaskLog taskLog) {
        return this.save(taskLog);
    }

    @Override
    public boolean updateTaskLog(CrawlerTaskLog taskLog) {
        if (taskLog == null || taskLog.getLogId() == null) {
            log.warn("Attempted to update task log with null object or missing logId.");
            return false;
        }
        return this.updateById(taskLog);
    }

    @Override
    public CrawlerTaskLogVO getByTaskId(String taskId) {
        CrawlerTaskLog log = this.getOne(Wrappers.lambdaQuery(CrawlerTaskLog.class)
                .eq(CrawlerTaskLog::getTaskId, taskId)
                .eq(CrawlerTaskLog::getIsDeleted, false));

        if (log != null) {
            CrawlerTaskLogVO vo = new CrawlerTaskLogVO();
            BeanUtils.copyProperties(log, vo);

            // 查询配置名称
            CrawlerConfig config = crawlerConfigRepository.selectById(log.getConfigId());
            if (config != null) {
                vo.setConfigName(config.getTargetName());
            } else {
                vo.setConfigName("未知配置");
            }

            return vo;
        }
        return null;
    }

    /**
     * 批量获取配置名称Map
     *
     * @param configIds 配置ID列表
     * @return 配置ID到名称的映射
     */
    private Map<Long, String> getConfigNameMap(List<Long> configIds) {
        if (configIds.isEmpty()) {
            return Map.of();
        }

        // 批量查询配置
        List<CrawlerConfig> configs = crawlerConfigRepository.selectList(
                Wrappers.lambdaQuery(CrawlerConfig.class)
                        .in(CrawlerConfig::getConfigId, configIds)
                        .eq(CrawlerConfig::getIsDeleted, false)
                        .select(CrawlerConfig::getConfigId, CrawlerConfig::getTargetName)
        );

        // 转换为ID-名称映射
        return configs.stream()
                .collect(Collectors.toMap(
                        CrawlerConfig::getConfigId,
                        CrawlerConfig::getTargetName
                ));
    }
} 