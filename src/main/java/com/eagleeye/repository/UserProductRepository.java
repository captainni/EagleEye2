package com.eagleeye.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eagleeye.model.entity.UserProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户产品数据访问层
 */
@Mapper
public interface UserProductRepository extends BaseMapper<UserProduct> {
}
