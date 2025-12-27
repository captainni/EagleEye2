package com.eagleeye.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页数据封装类
 */
@Data
public class CommonPage<T> {
    /**
     * 当前页码
     */
    private Long pageNum;
    /**
     * 每页数量
     */
    private Long pageSize;
    /**
     * 总页数
     */
    private Long totalPage;
    /**
     * 总记录数
     */
    private Long total;
    /**
     * 分页数据
     */
    private List<T> list;

    /**
     * 将MyBatis Plus分页结果转化为通用结果
     */
    public static <T> CommonPage<T> restPage(IPage<T> pageResult) {
        CommonPage<T> result = new CommonPage<T>();
        result.setPageNum(pageResult.getCurrent());
        result.setPageSize(pageResult.getSize());
        result.setTotal(pageResult.getTotal());
        result.setTotalPage(pageResult.getPages());
        result.setList(pageResult.getRecords());
        return result;
    }
} 