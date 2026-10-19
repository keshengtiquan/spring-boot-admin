package com.dadaz.core.exception;

import com.dadaz.core.enums.ResultCode;

import java.io.Serial;

/**
 * 业务异常
 * <p>
 * 用于表达「可以预期的业务规则冲突」，例如：账号已存在、余额不足、状态不允许该操作。
 * 这类异常不需要记录 error 级别日志，由 {@link GlobalExceptionHandler} 统一捕获并转成
 * {@code R.fail(code, message)} 返回给前端。
 * <p>
 * 使用示例：
 * <pre>
 * if (userMapper.existsByUsername(username)) {
 *     throw new BusinessException("账号已存在");
 * }
 * if (order == null) {
 *     throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
 * }
 * </pre>
 *
 * @author dadaz
 */
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 业务响应码，默认 500（ResultCode.FAIL）
     */
    private final Integer code;

    /**
     * 默认错误码 + 自定义消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    /**
     * 使用预定义响应码，消息取枚举中的默认消息
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用预定义响应码并覆盖消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 完全自定义响应码与消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 默认错误码 + 自定义消息 + 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.FAIL.getCode();
    }

    /**
     * 预定义响应码 + 原始异常
     */
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
    }

    /**
     * 完全自定义响应码、消息与原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    // ==================== 便捷断言 ====================

    /**
     * 条件成立时抛出业务异常
     *
     * @param condition 为 true 时抛出
     * @param message   异常消息
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(message);
        }
    }

    /**
     * 条件成立时抛出业务异常，使用预定义响应码
     */
    public static void throwIf(boolean condition, ResultCode resultCode, String message) {
        if (condition) {
            throw new BusinessException(resultCode, message);
        }
    }
}
