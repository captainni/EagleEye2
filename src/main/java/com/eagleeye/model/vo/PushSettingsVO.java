package com.eagleeye.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 推送设置视图对象
 */
@Data
public class PushSettingsVO {

    /**
     * 推送频率
     */
    private String frequency;

    /**
     * 推送渠道
     */
    private List<String> channels;
}
