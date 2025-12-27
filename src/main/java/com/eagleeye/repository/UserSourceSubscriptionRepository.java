package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.UserSourceSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息源订阅表 Mapper 接口
 */
@Mapper
public interface UserSourceSubscriptionRepository extends BaseMapper<UserSourceSubscription> {

} 