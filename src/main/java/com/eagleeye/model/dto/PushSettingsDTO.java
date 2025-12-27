package com.eagleeye.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 推送设置数据传输对象
 */
@Data
public class PushSettingsDTO {

    /**
     * 推送频率
     */
    @NotBlank(message = "推送频率不能为空")
    private String frequency;

    /**
     * 推送渠道
     */
    private List<String> channels;
}
