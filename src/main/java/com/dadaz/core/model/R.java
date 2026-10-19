package com.dadaz.core.model;

import com.dadaz.core.enums.ResultCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口统一响应对象
 *
 * <pre>
 * 成功：{ "code": 200, "message": "操作成功", "data": {...} }
 * 失败：{ "code": 500, "message": "操作失败", "data": null }
 * </pre>
 *
 * @param <T> 业务数据类型
 * @author dadaz
 */
@Schema(name = "R", description = "接口统一响应对象")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "响应码，200 表示成功", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据，失败时为 null")
    private T data;

    /**
     * 无参构造，供 Jackson 反序列化使用
     */
    public R() {
    }

    public R(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 成功 ====================

    /**
     * 成功，无返回数据
     */
    public static <T> R<T> success() {
        return success(ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功，返回业务数据
     */
    public static <T> R<T> success(T data) {
        return success(ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功，自定义提示消息
     */
    public static <T> R<T> success(String message, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 成功，完全自定义响应码与消息
     */
    public static <T> R<T> success(Integer code, String message, T data) {
        return new R<>(code, message, data);
    }

    // ==================== 失败 ====================

    /**
     * 失败，使用默认错误码与消息
     */
    public static <T> R<T> fail() {
        return fail(ResultCode.FAIL);
    }

    /**
     * 失败，自定义提示消息
     */
    public static <T> R<T> fail(String message) {
        return new R<>(ResultCode.FAIL.getCode(), message, null);
    }

    /**
     * 失败，自定义响应码与消息
     */
    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败，使用预定义响应码
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        return fail(resultCode, resultCode.getMessage());
    }

    /**
     * 失败，使用预定义响应码并覆盖提示消息
     */
    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null);
    }

    // ==================== 辅助方法 ====================

    /**
     * 是否成功。仅供服务端判断，不参与 JSON 序列化
     */
    @JsonIgnore
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    @Override
    public String toString() {
        return "R{code=" + code + ", message='" + message + "', data=" + data
                  + '}';
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
