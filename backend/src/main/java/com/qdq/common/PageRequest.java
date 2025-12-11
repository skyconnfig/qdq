package com.qdq.common;

import lombok.Data;

/**
 * 分页查询请求参数
 */
@Data
public class PageRequest {

    /** 当前页码（从1开始） */
    private Integer page = 1;

    /** 每页数量 */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String sortField;

    /** 排序方向（asc/desc） */
    private String sortOrder;

    /** 搜索关键字 */
    private String keyword;

    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
