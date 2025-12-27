import axiosInstance from '@/utils/axiosInstance';
import type { CommonPage, CommonResult } from '@/types/common';
import type {
    RequirementVO,
    RequirementDetailVO,
    RequirementQueryDTO,
    RequirementCreateDTO,
    RequirementUpdateDTO
} from '@/types/requirement'; // 假设类型文件已存在或即将创建

const BASE_URL = '/v1/requirements';

/**
 * 分页查询需求列表
 * @param params 查询参数
 * @returns 需求列表分页数据
 */
export const listRequirements = (params: RequirementQueryDTO): Promise<CommonResult<CommonPage<RequirementVO>>> => {
    return axiosInstance.get(BASE_URL, { params });
};

/**
 * 获取需求详情
 * @param id 需求ID
 * @returns 需求详情数据
 */
export const getRequirementDetail = (id: number): Promise<CommonResult<RequirementDetailVO>> => {
    return axiosInstance.get(`${BASE_URL}/${id}`);
};

/**
 * 创建需求
 * @param data 创建参数
 * @returns 创建结果
 */
export const createRequirement = (data: RequirementCreateDTO): Promise<CommonResult<number>> => {
    return axiosInstance.post(BASE_URL, data);
};

/**
 * 更新需求
 * @param id 需求ID
 * @param data 更新参数
 * @returns 更新结果
 */
export const updateRequirement = (id: number, data: RequirementUpdateDTO): Promise<CommonResult<string>> => {
    return axiosInstance.put(`${BASE_URL}/${id}`, data);
};

/**
 * 删除需求
 * @param id 需求ID
 * @returns 删除结果
 */
export const deleteRequirement = (id: number): Promise<CommonResult<string>> => {
    return axiosInstance.delete(`${BASE_URL}/${id}`);
};

/**
 * 将政策转化为需求
 * @param policyId 政策ID
 * @returns 转化结果
 */
export const convertPolicyToRequirement = (policyId: number): Promise<CommonResult<number>> => {
    return axiosInstance.post(`/v1/policies/${policyId}/to-requirement`);
};

/**
 * 将竞品动态转化为需求
 * @param competitorId 竞品ID
 * @returns 转化结果
 */
export const convertCompetitorToRequirement = (competitorId: number): Promise<CommonResult<number>> => {
    return axiosInstance.post(`/v1/competitors/${competitorId}/to-requirement`);
}; 