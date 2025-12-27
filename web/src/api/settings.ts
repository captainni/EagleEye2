import { SettingsData, Product, Source } from '@/types/settings';
import apiClient from '@/utils/request';

// 获取所有设置数据
export const getSettingsData = (): Promise<SettingsData> => {
  return apiClient.get('/v1/settings/all');
};

// 添加产品
export const addMyProduct = (product: Omit<Product, 'id'>): Promise<{ success: boolean; message: string, newProduct: Product }> => {
  return apiClient.post('/v1/settings/products', product);
};

// 编辑产品
export const updateMyProduct = (product: Product): Promise<{ success: boolean; message: string }> => {
  return apiClient.put(`/v1/settings/products/${product.id}`, product);
};

// 删除产品
export const deleteMyProduct = (productId: string): Promise<{ success: boolean; message: string }> => {
  return apiClient.delete(`/v1/settings/products/${productId}`);
};

// 保存推送设置
export const savePushSettings = (settings: { frequency: string; channels: string[] }): Promise<{ success: boolean; message: string }> => {
  return apiClient.post('/v1/settings/push', settings);
};
