package com.dadaz.admin.module.system.menu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 当前用户可访问的单条路由菜单，不包含 children 树结构。 */
@Data
@Schema(name = "MenuRouteVO", description = "当前用户可访问的路由菜单")
public class MenuRouteVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单 ID", example = "2001")
    private Long menuId;

    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    @Schema(description = "父菜单 ID，0 表示顶级", example = "2000")
    private Long parentId;

    @Schema(description = "显示顺序", example = "1")
    private Integer orderNum;

    @Schema(description = "路由地址", example = "user")
    private String path;

    @Schema(description = "前端组件路径", example = "system/user/index")
    private String component;

    @Schema(description = "路由参数（JSON 字符串）")
    private String query;

    @Schema(description = "是否外链", example = "false")
    private Boolean isFrame;

    @Schema(description = "是否缓存", example = "false")
    private Boolean isCache;

    @Schema(description = "是否固定标签页", example = "false")
    private Boolean isAffix;

    @Schema(description = "菜单类型：M 目录、C 菜单", example = "C")
    private String menuType;

    @Schema(description = "显示状态：0 显示、1 隐藏", example = "0")
    private String visible;

    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    @Schema(description = "菜单图标", example = "user")
    private String icon;
}
