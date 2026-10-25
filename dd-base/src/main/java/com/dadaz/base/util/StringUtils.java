package com.dadaz.base.util;

import cn.hutool.core.util.StrUtil;

/**
 * 字符串工具类：项目内统一的字符串处理入口
 * <p>
 * 只收「业务语义固定、多个模块会用到」的方法，实现委托给 hutool 的 {@link StrUtil}，
 * 让 null / 空白串的处理只有一套规则，而不是每个 Service 各写一份 trim + 判空。
 * <p>
 * 与 {@code org.springframework.util.StringUtils} 的区别：Spring 的 {@code hasText} 只判断
 * "有没有内容"，不修改原值；本类的 {@link #trimToNull(String)} 会把空白串归一成 null，
 * 配合「插入时 null 走数据库 DEFAULT」的约定使用。
 *
 * @author dadaz
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * 去掉首尾空白；null、空串、全空白串统一返回 null
     * <p>
     * 空串与 null 在库里是两回事（{@code ''} 会占一个长度、也让查询条件多一种写法），
     * 归一成 null 后，插入语句不包含该列，由 DDL 的 DEFAULT 兜底，语义更干净；
     * 查询条件侧则可以用 {@code value != null} 判断"用户确实传了值"。
     *
     * @param value 原始值，可为 null
     * @return 去空白后的值；原始值为 null 或全空白时返回 null
     */
    public static String trimToNull(String value) {
        return StrUtil.trimToNull(value);
    }

    /**
     * 是否为 null 或全空白字符（空格、tab、换行等）
     *
     * @param value 待判断字符串
     * @return true 表示 null 或全空白
     */
    public static boolean isBlank(CharSequence value) {
        return StrUtil.isBlank(value);
    }

    /**
     * 是否非 null 且包含非空白字符
     *
     * @param value 待判断字符串
     * @return true 表示有内容
     */
    public static boolean isNotBlank(CharSequence value) {
        return !isBlank(value);
    }
}
