package com.eagleeye.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("用户订阅创建参数")
public class UserSubscriptionCreateDTO {

    @ApiModelProperty(value = "要订阅的爬虫配置ID", required = true, example = "1")
    @NotNull(message = "配置ID不能为空")
    private Long configId;

    // userId 通常从当前登录用户上下文获取，不需要 DTO 传递

    @ApiModelProperty(value = "通知偏好设置 (JSON格式)", example = "{\"email\": true, \"appPush\": false}")
    private String notificationPreference; // 可选

    // 订阅状态默认为 active，不需要 DTO 传递
} 