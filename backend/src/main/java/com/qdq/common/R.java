package com.qdq.common;

import lombok.Data;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型
 */
@Data
public class R<T> {

    /** 响应码 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    public R() {
        this.timestamp = System.currentTimeMillis();
    }

    public R(int code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public R(int code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> R<T> ok() {
        return new R<>(0, "操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        return new R<>(0, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(0, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> R<T> fail(String message) {
        return new R<>(1, message);
    }

    /**
     * 失败响应（带错误码）
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message);
    }

    /**
     * 未授权
     */
    public static <T> R<T> unauthorized(String message) {
        return new R<>(401, message);
    }

    /**
     * 无权限
     */
    public static <T> R<T> forbidden(String message) {
        return new R<>(403, message);
    }

    /**
     * 资源不存在
     */
    public static <T> R<T> notFound(String message) {
        return new R<>(404, message);
    }

    /**
     * 服务器错误
     */
    public static <T> R<T> error(String message) {
        return new R<>(500, message);
    }
}
