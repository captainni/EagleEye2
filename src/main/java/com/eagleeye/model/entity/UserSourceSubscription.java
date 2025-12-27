package com.eagleeye.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息源订阅表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_source_subscription")
public class UserSourceSubscription implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订阅主键ID
     */
    @TableId(value = "subscription_id", type = IdType.AUTO)
    private Long subscriptionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订阅的爬虫配置ID
     */
    private Long configId;

    /**
     * 订阅状态 (\'active\', \'inactive\', \'paused\')
     */
    private String subscriptionStatus;

    /**
     * 通知偏好设置 (JSON格式)
     */
    private String notificationPreference;

    /**
     * 是否删除: 0-未删除, 1-已删除
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
 