package com.eagleeye.controller;

import com.eagleeye.model.dto.ProductCreateDTO;
import com.eagleeye.model.dto.ProductDTO;
import com.eagleeye.model.dto.PushSettingsDTO;
import com.eagleeye.model.vo.ApiResponseVO;
import com.eagleeye.model.vo.OperationResult;
import com.eagleeye.model.vo.ProductVO;
import com.eagleeye.model.vo.SettingsDataVO;
import com.eagleeye.service.settings.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 设置控制器
 */
@RestController
@RequestMapping("/v1/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    // TODO: 从认证上下文获取当前用户ID
    private Long getCurrentUserId() {
        return 1L; // 临时返回默认值
    }

    /**
     * 获取所有设置数据
     */
    @GetMapping("/all")
    public ApiResponseVO<SettingsDataVO> getSettingsData() {
        Long userId = getCurrentUserId();
        SettingsDataVO settingsData = settingsService.getSettingsData(userId);
        return ApiResponseVO.success(settingsData);
    }

    /**
     * 添加我的产品
     */
    @PostMapping("/products")
    public ApiResponseVO<ProductVO> addMyProduct(@Valid @RequestBody ProductCreateDTO productDTO) {
        Long userId = getCurrentUserId();
        ProductVO newProduct = settingsService.addMyProduct(userId, productDTO);
        return ApiResponseVO.success("产品添加成功", newProduct);
    }

    /**
     * 更新我的产品
     */
    @PutMapping("/products/{productId}")
    public ApiResponseVO<OperationResult> updateMyProduct(@PathVariable Long productId,
                                                          @Valid @RequestBody ProductDTO productDTO) {
        Long userId = getCurrentUserId();
        productDTO.setId(productId);
        boolean success = settingsService.updateMyProduct(userId, productDTO);

        if (success) {
            return ApiResponseVO.success("产品更新成功", OperationResult.success("产品更新成功"));
        } else {
            return ApiResponseVO.success("产品更新失败", OperationResult.error("产品更新失败"));
        }
    }

    /**
     * 删除我的产品
     */
    @DeleteMapping("/products/{productId}")
    public ApiResponseVO<OperationResult> deleteMyProduct(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        boolean success = settingsService.deleteMyProduct(userId, productId);

        if (success) {
            return ApiResponseVO.success("产品删除成功", OperationResult.success("产品删除成功"));
        } else {
            return ApiResponseVO.success("产品删除失败", OperationResult.error("产品删除失败"));
        }
    }

    /**
     * 保存推送设置
     */
    @PostMapping("/push")
    public ApiResponseVO<OperationResult> savePushSettings(@Valid @RequestBody PushSettingsDTO pushSettingsDTO) {
        Long userId = getCurrentUserId();
        boolean success = settingsService.savePushSettings(userId, pushSettingsDTO);

        if (success) {
            return ApiResponseVO.success("推送设置保存成功", OperationResult.success("推送设置保存成功"));
        } else {
            return ApiResponseVO.success("推送设置保存失败", OperationResult.error("推送设置保存失败"));
        }
    }
}
