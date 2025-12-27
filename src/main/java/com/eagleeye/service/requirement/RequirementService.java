package com.eagleeye.service.requirement;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eagleeye.model.dto.RequirementCreateDTO;
import com.eagleeye.model.dto.RequirementQueryDTO;
import com.eagleeye.model.dto.RequirementUpdateDTO;
import com.eagleeye.model.entity.Requirement;
import com.eagleeye.model.vo.RequirementDetailVO;
import com.eagleeye.model.vo.RequirementVO;

/**
 * 需求服务接口
 */
public interface RequirementService {

    /**
     * 分页查询需求列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<RequirementVO> listRequirements(RequirementQueryDTO queryDTO);

    /**
     * 获取需求详情
     *
     * @param id 需求ID
     * @return 需求详情
     */
    RequirementDetailVO getRequirementDetail(Long id);

    /**
     * 创建需求
     *
     * @param createDTO 创建参数
     * @return 创建的需求ID
     */
    Long createRequirement(RequirementCreateDTO createDTO);

    /**
     * 更新需求
     *
     * @param updateDTO 更新参数
     * @return 是否成功
     */
    boolean updateRequirement(RequirementUpdateDTO updateDTO);

    /**
     * 删除需求
     *
     * @param id 需求ID
     * @return 是否成功
     */
    boolean deleteRequirement(Long id);

    /**
     * 将政策转化为需求
     *
     * @param policyId 政策ID
     * @return 创建的需求ID
     */
    Long convertPolicyToRequirement(Long policyId);

    /**
     * 将竞品动态转化为需求
     *
     * @param competitorId 竞品ID
     * @return 创建的需求ID
     */
    Long convertCompetitorToRequirement(Long competitorId);
} 