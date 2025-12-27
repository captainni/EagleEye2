package com.eagleeye.model.vo.dashboard;

import lombok.Data;
import com.eagleeye.model.vo.PolicyVO;
import java.util.List;
import java.util.Map;

/**
 * 政策摘要VO
 */
@Data
public class PolicySummaryVO {
    
    /**
     * 最新重要政策列表
     */
    private List<PolicyVO> latestImportantPolicies;
    
    /**
     * 政策类型分布
     */
    private Map<String, Integer> policyTypeDistribution;
    
    /**
     * 政策来源分布
     */
    private Map<String, Integer> policySourceDistribution;
    
    /**
     * 相关领域分布
     */
    private Map<String, Integer> policyAreaDistribution;
} 