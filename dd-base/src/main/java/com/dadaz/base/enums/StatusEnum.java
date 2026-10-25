package com.dadaz.base.enums;

/**
 * 通用状态枚举：0 正常、1 停用
 * <p>
 * 项目内所有带"状态"语义的字段（{@code sys_user.status}，以及后续部门/角色/菜单等模块的状态）
 * 统一使用这一套取值：
 * <ul>
 *   <li>业务代码里不要再写 {@code "0"} / {@code "1"} 这类魔法字符串，
 *       也不要在各自的 Service 里再定义一份 {@code STATUS_NORMAL} / {@code STATUS_DISABLED} 常量；</li>
 *   <li>DTO 上的 {@code @Pattern} 校验引用 {@link #PATTERN}，
 *       保证"校验规则"和"枚举取值"同源，改取值时只需要改这一处。</li>
 * </ul>
 * <p>
 * 与 {@link ResultCode} 一样属于通用层枚举。某个模块如果需要自己的专属状态（例如审批流转状态），
 * 请单独建枚举，不要往这里塞。
 *
 * @author dadaz
 */
public enum StatusEnum {

    /**
     * 正常
     */
    NORMAL("0", "正常"),

    /**
     * 停用
     */
    DISABLED("1", "停用");

    /**
     * 状态取值的校验正则，供 DTO 字段上的 {@code @Pattern} 引用
     */
    public static final String PATTERN = "^[01]$";

    private final String code;

    private final String description;

    StatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按状态码解析；未知状态码返回 null
     *
     * @param code 状态码，如 "0" / "1"
     * @return 对应枚举，未知时返回 null
     */
    public static StatusEnum of(String code) {
        for (StatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 是否正常状态
     *
     * @param code 状态码
     * @return true 表示正常
     */
    public static boolean isNormal(String code) {
        return of(code) == NORMAL;
    }

    /**
     * 是否停用状态
     *
     * @param code 状态码
     * @return true 表示停用
     */
    public static boolean isDisabled(String code) {
        return of(code) == DISABLED;
    }
}
