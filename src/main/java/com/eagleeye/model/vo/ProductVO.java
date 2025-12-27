package com.eagleeye.model.vo;

import lombok.Data;

/**
 * 产品视图对象
 */
@Data
public class ProductVO {

    /**
     * 产品ID
     */
    private String id;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品类型
     */
    private String type;

    /**
     * 产品特点
     */
    private String features;
}
