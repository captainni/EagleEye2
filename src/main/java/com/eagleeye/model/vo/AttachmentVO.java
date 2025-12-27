package com.eagleeye.model.vo;

import lombok.Data;

/**
 * 附件视图对象
 */
@Data
public class AttachmentVO {

    /**
     * 附件ID
     */
    private Long id;

    /**
     * 附件名称
     */
    private String name;

    /**
     * 附件类型
     */
    private String type;

    /**
     * 附件URL
     */
    private String url;
} 