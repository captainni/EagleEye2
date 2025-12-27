package com.eagleeye.service.settings.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eagleeye.model.dto.ProductCreateDTO;
import com.eagleeye.model.dto.ProductDTO;
import com.eagleeye.model.dto.PushSettingsDTO;
import com.eagleeye.model.entity.UserProduct;
import com.eagleeye.model.entity.UserSettings;
import com.eagleeye.model.vo.ProductVO;
import com.eagleeye.model.vo.PushSettingsVO;
import com.eagleeye.model.vo.SettingsDataVO;
import com.eagleeye.model.vo.SourceVO;
import com.eagleeye.repository.UserProductRepository;
import com.eagleeye.repository.UserSettingsRepository;
import com.eagleeye.service.settings.SettingsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 设置服务实现类
 */
@Service
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private UserProductRepository userProductRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public SettingsDataVO getSettingsData(Long userId) {
        SettingsDataVO settingsDataVO = new SettingsDataVO();

        // 设置政策信息源（模拟数据）
        List<SourceVO> policySources = new ArrayList<>();
        policySources.add(new SourceVO() {{
            setId("1");
            setName("中国人民银行");
            setChecked(true);
        }});
        policySources.add(new SourceVO() {{
            setId("2");
            setName("银保监会");
            setChecked(true);
        }});
        settingsDataVO.setPolicySources(policySources);

        // 设置竞品信息源（模拟数据）
        List<SourceVO> competitorSources = new ArrayList<>();
        competitorSources.add(new SourceVO() {{
            setId("1");
            setName("工商银行");
            setChecked(true);
        }});
        competitorSources.add(new SourceVO() {{
            setId("2");
            setName("建设银行");
            setChecked(false);
        }});
        settingsDataVO.setCompetitorSources(competitorSources);

        // 获取我的产品列表
        LambdaQueryWrapper<UserProduct> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(UserProduct::getUserId, userId)
                      .eq(UserProduct::getIsDeleted, false);
        List<UserProduct> userProducts = userProductRepository.selectList(productWrapper);

        List<ProductVO> productVOS = new ArrayList<>();
        for (UserProduct userProduct : userProducts) {
            ProductVO productVO = new ProductVO();
            BeanUtils.copyProperties(userProduct, productVO);
            productVO.setId(userProduct.getId().toString());
            productVOS.add(productVO);
        }
        settingsDataVO.setMyProducts(productVOS);

        // 获取推送设置
        LambdaQueryWrapper<UserSettings> settingsWrapper = new LambdaQueryWrapper<>();
        settingsWrapper.eq(UserSettings::getUserId, userId)
                      .eq(UserSettings::getIsDeleted, false);
        List<UserSettings> userSettingsList = userSettingsRepository.selectList(settingsWrapper);
        Optional<UserSettings> userSettingsOpt = userSettingsList.stream().findFirst();

        PushSettingsVO pushSettingsVO = new PushSettingsVO();
        if (userSettingsOpt.isPresent()) {
            UserSettings userSettings = userSettingsOpt.get();
            pushSettingsVO.setFrequency(userSettings.getFrequency());
            try {
                List<String> channels = objectMapper.readValue(userSettings.getChannels(), List.class);
                pushSettingsVO.setChannels(channels);
            } catch (JsonProcessingException e) {
                pushSettingsVO.setChannels(new ArrayList<>());
            }
        } else {
            // 默认值
            pushSettingsVO.setFrequency("weekly");
            pushSettingsVO.setChannels(new ArrayList<>());
        }
        settingsDataVO.setPushSettings(pushSettingsVO);

        return settingsDataVO;
    }

    @Override
    @Transactional
    public ProductVO addMyProduct(Long userId, ProductCreateDTO productDTO) {
        UserProduct userProduct = new UserProduct();
        BeanUtils.copyProperties(productDTO, userProduct);
        userProduct.setUserId(userId);
        userProduct.setCreateTime(LocalDateTime.now());
        userProduct.setUpdateTime(LocalDateTime.now());
        userProduct.setIsDeleted(false);

        userProductRepository.insert(userProduct);

        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(userProduct, productVO);
        productVO.setId(userProduct.getId().toString());

        return productVO;
    }

    @Override
    @Transactional
    public boolean updateMyProduct(Long userId, ProductDTO productDTO) {
        LambdaQueryWrapper<UserProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProduct::getUserId, userId)
               .eq(UserProduct::getId, productDTO.getId())
               .eq(UserProduct::getIsDeleted, false);
        List<UserProduct> userProducts = userProductRepository.selectList(wrapper);

        if (userProducts.isEmpty()) {
            return false;
        }

        UserProduct userProduct = userProducts.get(0);
        BeanUtils.copyProperties(productDTO, userProduct);
        userProduct.setUpdateTime(LocalDateTime.now());

        return userProductRepository.updateById(userProduct) > 0;
    }

    @Override
    @Transactional
    public boolean deleteMyProduct(Long userId, Long productId) {
        UserProduct userProduct = userProductRepository.selectById(productId);

        if (userProduct == null || !userProduct.getUserId().equals(userId)) {
            return false;
        }

        userProduct.setIsDeleted(true);
        userProduct.setUpdateTime(LocalDateTime.now());

        return userProductRepository.updateById(userProduct) > 0;
    }

    @Override
    @Transactional
    public boolean savePushSettings(Long userId, PushSettingsDTO pushSettingsDTO) {
        LambdaQueryWrapper<UserSettings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettings::getUserId, userId)
               .eq(UserSettings::getIsDeleted, false);
        List<UserSettings> existingSettings = userSettingsRepository.selectList(wrapper);

        try {
            String channelsJson = objectMapper.writeValueAsString(pushSettingsDTO.getChannels());

            if (existingSettings.isEmpty()) {
                // 创建新的设置
                UserSettings userSettings = new UserSettings();
                userSettings.setUserId(userId);
                userSettings.setFrequency(pushSettingsDTO.getFrequency());
                userSettings.setChannels(channelsJson);
                userSettings.setCreateTime(LocalDateTime.now());
                userSettings.setUpdateTime(LocalDateTime.now());
                userSettings.setIsDeleted(false);

                return userSettingsRepository.insert(userSettings) > 0;
            } else {
                // 更新现有设置
                UserSettings userSettings = existingSettings.get(0);
                userSettings.setFrequency(pushSettingsDTO.getFrequency());
                userSettings.setChannels(channelsJson);
                userSettings.setUpdateTime(LocalDateTime.now());

                return userSettingsRepository.updateById(userSettings) > 0;
            }
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
