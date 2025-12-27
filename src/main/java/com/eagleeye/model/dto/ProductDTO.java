package com.eagleeye.model.dto;

import lombok.Data;

/**
 * 产品数据传输对象
 */
@Data
public class ProductDTO {

    /**
     * 产品ID
     */
    private Long id;

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
