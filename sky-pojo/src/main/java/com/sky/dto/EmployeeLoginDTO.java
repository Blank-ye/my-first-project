package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;  // ✅ 新包名
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工登录时传递的数据模型")  // ✅ @ApiModel -> @Schema
public class EmployeeLoginDTO implements Serializable {

    @Schema(description = "用户名")  // ✅ @ApiModelProperty -> @Schema
    private String username;

    @Schema(description = "密码")
    private String password;
}