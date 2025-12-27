package com.eagleeye.service.policy;

import com.eagleeye.common.api.CommonPage;
import com.eagleeye.model.dto.PolicyQueryDTO;
import com.eagleeye.model.vo.PolicyDetailVO;
import com.eagleeye.model.vo.PolicyVO;

/**
 * 政策服务接口
 */
public interface PolicyService {
    
    /**
     * 分页查询政策列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    CommonPage<PolicyVO> listPolicies(PolicyQueryDTO queryDTO);
    
    /**
     * 获取政策详情
     * 
     * @param id 政策ID
     * @return 政策详情
     */
    PolicyDetailVO getPolicyDetail(Long id);
    
    /**
     * 将政策转为需求
     * 
     * @param policyId 政策ID
     * @param userId 用户ID
     * @return 需求ID
     */
    Long convertToRequirement(Long policyId, Long userId);
} 