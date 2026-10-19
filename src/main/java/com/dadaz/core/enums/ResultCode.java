package com.dadaz.core.enums;

/**
 * 通用响应码
 * <p>
 * 约定：code 与 HTTP 语义保持一致，200 表示成功，其余为各类失败场景。
 * 此处只存放「通用层」的响应码；具体业务模块的专属码（如 1001 用户不存在）
 * 建议后续单独建枚举，不要与通用码混在一个类里。
 *
 * @author dadaz
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 请求参数错误 */
    PARAM_ERROR(400, "请求参数校验失败"),

    /** 未登录或登录已过期 */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** 无权限 */
    FORBIDDEN(403, "无权访问该资源"),

    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),

    /** 请求方法不支持 */
    METHOD_NOT_ALLOWED(405, "请求方法不被支持"),

    /** 业务处理失败 */
    FAIL(500, "操作失败");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
