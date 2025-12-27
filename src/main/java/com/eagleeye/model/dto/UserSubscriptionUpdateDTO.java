package com.eagleeye.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户订阅更新参数")
public class UserSubscriptionUpdateDTO {

    // subscriptionId 由 PathVariable 传入

    @ApiModelProperty(value = "订阅状态 (\'active\', \'inactive\', \'paused\')", example = "paused")
    private String subscriptionStatus;

    @ApiModelProperty(value = "通知偏好设置 (JSON格式)", example = "{\"email\": true, \"appPush\": true}")
    private String notificationPreference;
} 