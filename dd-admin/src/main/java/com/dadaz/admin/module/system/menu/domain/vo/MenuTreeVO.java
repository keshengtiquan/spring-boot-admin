package com.dadaz.admin.module.system.menu.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单树出参（管理端使用，包含按钮节点）
 * <p>
 * 用于"菜单管理"和"给角色分配菜单"，保留全部字段与所有类型（M/C/F）。
 * 层级由 {@code parentId} 组织，{@code children} 已按 {@code orderNum}、{@code menuId} 排好序；
 * 叶子节点返回空数组而不是 null，前端递归渲染时不用判空。
 *
 * @author dadaz
 */
@Data
@Schema(name = "MenuTreeVO", description = "菜单树节点")
public class MenuTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单 ID", example = "2000")
    private Long menuId;

    @Schema(description = "菜单名称", example = "系统管理")
    private String menuName;

    @Schema(description = "父菜单 ID，0 表示顶级", example = "0")
    private Long parentId;

    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    @Schema(description = "路由地址", example = "system")
    private String path;

    @Schema(description = "前端组件路径", example = "system/user/index")
    private String component;

    @Schema(description = "路由参数（JSON 字符串）")
    private String query;

    @Schema(description = "是否外链", example = "false")
    private Boolean isFrame;

    @Schema(description = "是否缓存", example = "false")
    private Boolean isCache;

    @Schema(description = "是否固定标签页（不可关闭）", example = "false")
    private Boolean isAffix;

    @Schema(description = "菜单类型：M 目录、C 菜单、F 按钮", example = "C")
    private String menuType;

    @Schema(description = "显示状态：0 显示、1 隐藏", example = "0")
    private String visible;

    @Schema(description = "菜单状态：0 正常、1 停用", example = "0")
    private String status;

    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    @Schema(description = "菜单图标", example = "user")
    private String icon;

    @Schema(description = "创建时间", example = "2026-10-27 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "子菜单，叶子节点为空数组")
    private List<MenuTreeVO> children;
}
