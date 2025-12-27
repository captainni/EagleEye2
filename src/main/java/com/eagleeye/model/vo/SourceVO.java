package com.eagleeye.model.vo;

import lombok.Data;

/**
 * 信息源视图对象
 */
@Data
public class SourceVO {

    /**
     * 信息源ID
     */
    private String id;

    /**
     * 信息源名称
     */
    private String name;

    /**
     * 是否选中
     */
    private Boolean checked;
}
