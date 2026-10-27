package com.dadaz.admin.module.system.menu.domain.dto;

import com.dadaz.base.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建菜单入参
 * <p>
 * 只承载"客户端可以决定"的字段。{@code menuId}、审计字段不在本 DTO 暴露，
 * 由数据库自增 / {@code MybatisMetaObjectHandler} 负责。
 * <p>
 * 字段校验分两层：
 * <ul>
 *   <li>本 DTO 做单字段格式校验（类型、长度、取值）；</li>
 *   <li>跨字段规则由 {@code MenuServiceImpl#createMenu} 校验：
 *       目录/菜单必须传 path、菜单必须传 component、目录必须不传 component、
 *       按钮会忽略 path/component/query、父节点必须存在且不能是按钮。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@Schema(name = "MenuCreateDTO", description = "创建菜单入参")
public class MenuCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", example = "用户管理", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过 50 个字符")
    private String menuName;

    /**
     * 父菜单 ID。0 或不传表示顶级
     */
    @Schema(description = "父菜单 ID，0 或不传表示顶级", example = "2000")
    @Min(value = 0, message = "父菜单 ID 不能为负数")
    private Long parentId;

    /**
     * 显示顺序。不传时 Service 按 0 处理
     */
    @Schema(description = "显示顺序，不传默认 0", example = "1")
    @Min(value = 0, message = "显示顺序不能为负数")
    @Max(value = 9999, message = "显示顺序不能超过 9999")
    private Integer orderNum;

    /**
     * 路由地址。目录/菜单必填（Service 校验）；外链时传完整 URL
     */
    @Schema(description = "路由地址；外链时为完整 URL", example = "user")
    @Size(max = 200, message = "路由地址长度不能超过 200 个字符")
    private String path;

    /**
     * 前端组件路径。菜单（C）且非外链时必填；目录（M）与外链会忽略
     */
    @Schema(description = "前端组件路径，菜单类型为 C 且非外链时必填", example = "system/user/index")
    @Size(max = 255, message = "组件路径长度不能超过 255 个字符")
    private String component;

    /**
     * 路由参数（JSON 字符串），按钮类型会忽略
     */
    @Schema(description = "路由参数（JSON 字符串）", example = "{\"id\":\"1\"}")
    @Size(max = 255, message = "路由参数长度不能超过 255 个字符")
    private String query;

    /**
     * 是否外链：0 否、1 是。不传按 0 处理
     */
    @Schema(description = "是否外链：0 否、1 是，不传默认 0", example = "0")
    @Pattern(regexp = "^[01]$", message = "是否外链取值只能是 0 或 1")
    private String isFrame;

    /**
     * 是否缓存：0 否、1 是。不传按 0 处理
     */
    @Schema(description = "是否缓存（keep-alive）：0 否、1 是，不传默认 0", example = "0")
    @Pattern(regexp = "^[01]$", message = "是否缓存取值只能是 0 或 1")
    private String isCache;

    /**
     * 是否固定标签页：0 否、1 是。不传按 0 处理，一般只给首页配置
     */
    @Schema(description = "是否固定标签页（不可关闭）：0 否、1 是，不传默认 0", example = "0")
    @Pattern(regexp = "^[01]$", message = "是否固定标签页取值只能是 0 或 1")
    private String isAffix;

    /**
     * 菜单类型：M 目录、C 菜单、F 按钮
     */
    @Schema(description = "菜单类型：M 目录、C 菜单、F 按钮", example = "C", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是 M（目录）、C（菜单）、F（按钮）")
    private String menuType;

    /**
     * 显示状态：0 显示、1 隐藏。不传按 0 处理
     */
    @Schema(description = "显示状态：0 显示、1 隐藏，不传默认 0", example = "0")
    @Pattern(regexp = "^[01]$", message = "显示状态取值只能是 0 或 1")
    private String visible;

    /**
     * 菜单状态：0 正常、1 停用。不传按 0 处理
     */
    @Schema(description = "菜单状态：0 正常、1 停用，不传默认 0", example = "0")
    @Pattern(regexp = StatusEnum.PATTERN, message = "菜单状态取值只能是 0 或 1")
    private String status;

    /**
     * 权限标识，按钮主要靠它做权限控制
     */
    @Schema(description = "权限标识", example = "system:user:list")
    @Size(max = 100, message = "权限标识长度不能超过 100 个字符")
    private String perms;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", example = "user")
    @Size(max = 100, message = "菜单图标长度不能超过 100 个字符")
    private String icon;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "用户管理菜单")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
