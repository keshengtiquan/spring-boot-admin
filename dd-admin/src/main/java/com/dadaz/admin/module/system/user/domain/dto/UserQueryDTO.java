package com.dadaz.admin.module.system.user.domain.dto;

import com.dadaz.base.enums.StatusEnum;
import com.dadaz.base.model.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "UserQueryDTO", description = "用户分页查询参数")
public class UserQueryDTO extends PageParam {
    @Schema(description = "登录账号", example = "zhangsan" )
    private String userName;

    @Schema(description = "用户昵称", example = "张三")
    private String nickName;

    @Schema(description = "账号状态：0 正常、1 停用")
    @Pattern(regexp = StatusEnum.PATTERN, message = "账号状态取值只能是 0 或 1")
    private String status;
}
