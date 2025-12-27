package com.eagleeye.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 设置数据视图对象
 */
@Data
public class SettingsDataVO {

    /**
     * 政策信息源列表
     */
    private List<SourceVO> policySources;

    /**
     * 竞品信息源列表
     */
    private List<SourceVO> competitorSources;

    /**
     * 我的产品列表
     */
    private List<ProductVO> myProducts;

    /**
     * 推送设置
     */
    private PushSettingsVO pushSettings;
}
