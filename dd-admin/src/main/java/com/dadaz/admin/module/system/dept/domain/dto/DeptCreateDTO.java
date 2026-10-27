package com.dadaz.admin.module.system.dept.domain.dto;

import com.dadaz.base.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建部门入参
 * <p>
 * 只承载"客户端可以决定"的字段。以下字段刻意不在本 DTO 暴露：
 * <ul>
 *   <li>{@code deptId}：数据库自增，客户端无权指定；</li>
 *   <li>{@code ancestors}：由 Service 根据父部门计算，客户端传入会破坏树结构一致性；</li>
 *   <li>{@code createBy} / {@code createTime} / {@code updateBy} / {@code updateTime}：
 *       由 {@code MybatisMetaObjectHandler} 自动填充。</li>
 * </ul>
 * <p>
 * {@code parentId} 不传或传 0 都表示顶级部门；传正数时由 Service 校验父部门是否存在。
 * 其余可选字段不传时为 null，由 Service 按业务默认值补齐，校验注解对 null 一律放行。
 *
 * @author dadaz
 */
@Data
@Schema(name = "DeptCreateDTO", description = "创建部门入参")
public class DeptCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父部门 ID。0 或不传表示顶级部门
     */
    @Schema(description = "父部门 ID，0 或不传表示顶级部门", example = "100")
    @Min(value = 0, message = "父部门 ID 不能为负数")
    private Long parentId;

    /**
     * 部门名称。同一父部门下不允许重名
     */
    @Schema(description = "部门名称", example = "研发部", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过 30 个字符")
    private String deptName;

    /**
     * 显示顺序。不传时 Service 按 0 处理
     */
    @Schema(description = "显示顺序，不传默认 0", example = "1")
    @Min(value = 0, message = "显示顺序不能为负数")
    @Max(value = 9999, message = "显示顺序不能超过 9999")
    private Integer orderNum;

    /**
     * 负责人
     */
    @Schema(description = "负责人", example = "张三")
    @Size(max = 20, message = "负责人长度不能超过 20 个字符")
    private String leader;

    /**
     * 联系电话。允许为空串，非空时必须是 11 位手机号
     */
    @Schema(description = "联系电话", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "rd@dadaz.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    /**
     * 部门状态。不传时 Service 按 0（正常）处理
     */
    @Schema(description = "部门状态：0 正常、1 停用，不传默认 0", example = "0")
    @Pattern(regexp = StatusEnum.PATTERN, message = "部门状态取值只能是 0 或 1")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "研发部")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;
}
