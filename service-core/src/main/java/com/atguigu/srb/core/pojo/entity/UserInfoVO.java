package com.atguigu.srb.core.pojo.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 用户基本信息
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="UserInfo对象", description="用户基本信息")
public class UserInfoVO implements Serializable {

    @ApiModelProperty(value = "用户类型")
    private Integer userType;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "验证码")
    private String code;

    @ApiModelProperty(value = "密码")
    private String password;
}
