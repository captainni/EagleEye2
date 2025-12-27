package com.eagleeye.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 产品创建数据传输对象
 */
@Data
public class ProductCreateDTO {

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    private String name;

    /**
     * 产品类型
     */
    @NotBlank(message = "产品类型不能为空")
    private String type;

    /**
     * 产品特点
     */
    private String features;
}
