package com.dadaz.admin.module.system.menu.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dadaz.base.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * 菜单权限实体，对应表 {@code sys_menu}
 * <p>
 * 字段来源：{@code sys_menu} 通用结构（与 {@code sys_user} / {@code sys_role} / {@code sys_dept} 同源），
 * 按当前项目约定做了四点调整：
 * <ul>
 *   <li>审计字段（create_by / create_time / update_by / update_time）由 {@link BaseEntity} 承载，
 *       由 {@code MybatisMetaObjectHandler} 自动填充，本类不再重复声明；</li>
 *   <li><b>没有 {@code del_flag}</b>：与 user / role / dept 一致，统一物理删除。
 *       删菜单前必须先校验子菜单和 {@code sys_role_menu} 引用（删除接口后续补）；</li>
 *   <li>路由相关字段（path / component / query / isFrame / isCache）由
 *       {@code MenuServiceImpl} 统一处理和校验，客户端不能随意组合；</li>
 *   <li>列名一律显式写在 {@code @TableField(value = ...)} 上，不依赖驼峰自动映射。</li>
 * </ul>
 * <p>
 * 菜单类型（{@code menuType}）决定字段用法：
 * <table border="1">
 *   <caption>菜单类型与路由</caption>
 *   <tr><th>类型</th><th>含义</th><th>path</th><th>component</th><th>是否下发路由</th></tr>
 *   <tr><td>M</td><td>目录</td><td>必填</td><td>必须为空（前端用布局组件包裹 children）</td><td>是</td></tr>
 *   <tr><td>C</td><td>菜单</td><td>必填</td><td>必填</td><td>是</td></tr>
 *   <tr><td>F</td><td>按钮</td><td>忽略（置空）</td><td>忽略（置空）</td><td>否</td></tr>
 * </table>
 * <p>
 * 树结构约定：{@code parent_id = 0} 表示顶级；目录下挂菜单，菜单下挂按钮；
 * 按钮下不允许再挂子节点。{@code visible = '1'} 表示隐藏（路由下发但侧边栏不显示，
 * 用于详情页）；{@code status = '1'} 表示停用（管理树可见，但前端菜单/路由不应再使用）。
 * <p>
 * 注解说明（与 {@link com.dadaz.admin.module.system.role.domain.entity.RoleEntity} 一致）：
 * {@code @Data} 负责 getter/setter，{@code @EqualsAndHashCode(of = "menuId")} 只按主键判等，
 * {@code @ToString(callSuper = true)} 让日志带上审计字段。不要改成 {@code callSuper = true}——
 * {@link BaseEntity} 没有重写 equals/hashCode，那会退化成引用比较。
 * <p>
 * 数据库侧职责（本类无法表达，需在 DDL 中保证）：
 * <ul>
 *   <li>{@code parent_id}、{@code status} 普通索引：树查询与路由接口的过滤条件；</li>
 *   <li>菜单名称、权限标识<b>不加唯一索引</b>：不同父节点下重名是正常业务
 *       （两个模块都有"查询"按钮），唯一性靠应用层约定。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "menuId")
@TableName("sys_menu")
@Schema(name = "MenuEntity", description = "菜单权限")
public class MenuEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID，主键。数据库自增，插入时不需要赋值
     */
    @Schema(description = "菜单 ID", example = "2000")
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    /**
     * 菜单名称，侧边栏与标签页展示用
     */
    @Schema(description = "菜单名称", example = "系统管理")
    @TableField("menu_name")
    private String menuName;

    /**
     * 父菜单 ID。0 表示顶级；按钮下不允许再挂子节点
     */
    @Schema(description = "父菜单 ID，0 表示顶级", example = "0")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 显示顺序，越小越靠前
     */
    @Schema(description = "显示顺序，越小越靠前", example = "1")
    @TableField("order_num")
    private Integer orderNum;

    /**
     * 路由地址。目录/菜单相对父级；外链时放完整 URL
     */
    @Schema(description = "路由地址；外链时为完整 URL", example = "system")
    @TableField("path")
    private String path;

    /**
     * 前端组件路径。目录为空（由前端布局组件包裹 children），菜单必填
     */
    @Schema(description = "前端组件路径，目录为空", example = "system/user/index")
    @TableField("component")
    private String component;

    /**
     * 路由参数，JSON 字符串，原样透传给前端
     */
    @Schema(description = "路由参数（JSON 字符串）", example = "{\"id\":\"1\"}")
    @TableField("query")
    private String query;

    /**
     * 是否外链。true 时 path 是完整 URL，路由不生成 component，前端用 meta.link 打开
     */
    @Schema(description = "是否外链：0 否、1 是", example = "false")
    @TableField("is_frame")
    private Boolean isFrame;

    /**
     * 是否缓存（keep-alive）。只对菜单（C）有意义。
     * 注意：前端 RouteMeta 用的是相反语义的 {@code noCache}，
     * 下发路由时会取反（{@code meta.noCache = !isCache}）。
     */
    @Schema(description = "是否缓存（keep-alive）", example = "false")
    @TableField("is_cache")
    private Boolean isCache;

    /**
     * 是否固定标签页（前端标签页不可关闭），一般只给首页配置。
     * 对应前端 {@code RouteMeta.affix}
     */
    @Schema(description = "是否固定标签页（不可关闭）", example = "false")
    @TableField("is_affix")
    private Boolean isAffix;

    /**
     * 菜单类型。M 目录、C 菜单、F 按钮
     */
    @Schema(description = "菜单类型：M 目录、C 菜单、F 按钮", example = "M")
    @TableField("menu_type")
    private String menuType;

    /**
     * 显示状态。0 显示、1 隐藏（隐藏菜单仍下发路由，前端侧边栏不渲染）
     */
    @Schema(description = "显示状态：0 显示、1 隐藏", example = "0")
    @TableField("visible")
    private String visible;

    /**
     * 菜单状态。0 正常、1 停用（停用菜单不参与前端菜单展示）
     */
    @Schema(description = "菜单状态：0 正常、1 停用", example = "0")
    @TableField("status")
    private String status;

    /**
     * 权限标识，如 {@code system:user:list}。按钮（F）主要靠它做权限控制
     */
    @Schema(description = "权限标识", example = "system:user:list")
    @TableField("perms")
    private String perms;

    /**
     * 菜单图标，前端图标库的标识
     */
    @Schema(description = "菜单图标", example = "setting")
    @TableField("icon")
    private String icon;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "系统管理目录")
    @TableField("remark")
    private String remark;
}
