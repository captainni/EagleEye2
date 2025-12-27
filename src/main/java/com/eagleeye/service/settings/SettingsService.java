package com.eagleeye.service.settings;

import com.eagleeye.model.dto.ProductCreateDTO;
import com.eagleeye.model.dto.ProductDTO;
import com.eagleeye.model.dto.PushSettingsDTO;
import com.eagleeye.model.vo.SettingsDataVO;
import com.eagleeye.model.vo.ProductVO;

/**
 * 设置服务接口
 */
public interface SettingsService {

    /**
     * 获取所有设置数据
     *
     * @param userId 用户ID
     * @return 设置数据
     */
    SettingsDataVO getSettingsData(Long userId);

    /**
     * 添加我的产品
     *
     * @param userId 用户ID
     * @param productDTO 产品数据
     * @return 添加结果
     */
    ProductVO addMyProduct(Long userId, ProductCreateDTO productDTO);

    /**
     * 更新我的产品
     *
     * @param userId 用户ID
     * @param productDTO 产品数据
     * @return 更新结果
     */
    boolean updateMyProduct(Long userId, ProductDTO productDTO);

    /**
     * 删除我的产品
     *
     * @param userId 用户ID
     * @param productId 产品ID
     * @return 删除结果
     */
    boolean deleteMyProduct(Long userId, Long productId);

    /**
     * 保存推送设置
     *
     * @param userId 用户ID
     * @param pushSettingsDTO 推送设置
     * @return 保存结果
     */
    boolean savePushSettings(Long userId, PushSettingsDTO pushSettingsDTO);
}
