package com.eagleeye.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@ApiModel("用户订阅视图对象")
public class UserSubscriptionVO {

    @ApiModelProperty("订阅主键ID")
    private Long subscriptionId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("订阅的爬虫配置ID")
    private Long configId;

    // 可能需要关联查询 CrawlerConfig 的信息以显示给用户
    @ApiModelProperty("订阅的目标名称")
    private String targetName;

    @ApiModelProperty("订阅的目标类型")
    private String targetType;

    @ApiModelProperty("订阅状态")
    private String subscriptionStatus;

    @ApiModelProperty("通知偏好设置 (JSON格式)")
    private String notificationPreference;

    @ApiModelProperty("订阅创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("订阅更新时间")
    private LocalDateTime updateTime;
} 