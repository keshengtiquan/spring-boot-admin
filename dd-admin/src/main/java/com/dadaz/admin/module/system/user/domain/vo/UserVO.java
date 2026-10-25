package com.dadaz.admin.module.system.user.domain.vo;

import com.dadaz.admin.module.system.user.domain.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户出参
 * <p>
 * 为什么不直接把 {@link UserEntity} 返回给前端：
 * <ul>
 *   <li>实体里的 {@code password} 即使标了 {@code @JsonProperty(WRITE_ONLY)}，
 *       也只是"当前没被序列化"，一旦有人改动或换了序列化框架就会泄露。
 *       出参对象从结构上就不存在这个字段，更安全；</li>
 *   <li>实体字段随表结构变化，VO 可以保持接口契约稳定。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@Schema(name = "UserVO", description = "用户信息")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 ID", example = "100")
    private Long userId;

    @Schema(description = "所属部门 ID", example = "100")
    private Long deptId;

    @Schema(description = "登录账号", example = "zhangsan")
    private String userName;

    @Schema(description = "用户昵称", example = "张三")
    private String nickName;

    @Schema(description = "用户类型：00 系统用户、01 其他注册用户", example = "00")
    private String userType;

    @Schema(description = "邮箱", example = "zhangsan@dadaz.com")
    private String email;

    @Schema(description = "手机号", example = "13800138000")
    private String phoneNumber;

    @Schema(description = "性别：0 男、1 女、2 未知", example = "0")
    private String sex;

    @Schema(description = "头像地址", example = "/profile/avatar/default.png")
    private String avatar;

    @Schema(description = "账号状态：0 正常、1 停用", example = "0")
    private String status;

    @Schema(description = "最后登录 IP", example = "192.168.31.100")
    private String loginIp;

    @Schema(description = "最后登录时间", example = "2026-10-20 14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    @Schema(description = "创建时间", example = "2026-10-20 14:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "备注", example = "语文教研组")
    private String remark;

}
