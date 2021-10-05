package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.RegexValidateUtils;
import com.atguigu.srb.core.pojo.entity.vo.LoginVO;
import com.atguigu.srb.core.pojo.entity.vo.UserInfoVO;
import com.atguigu.srb.core.pojo.entity.vo.UserRegisterInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/api/core/userInfo")
//@CrossOrigin
@Api(tags = "用户信息接口")
public class ApiUserInfoController {

    @Autowired
    UserInfoService userInfoService;



    @ApiOperation("保存用户注册信息")
    @PostMapping("/register")
    public R UserRegister(
            @ApiParam(value = "用户信息表单",required = true)
            @RequestBody UserRegisterInfoVO userRegisterInfoVO
            ){
        String mobile = userRegisterInfoVO.getMobile();
        boolean mobileFormat = RegexValidateUtils.checkCellphone(userRegisterInfoVO.getMobile());
        String password = userRegisterInfoVO.getPassword();
        String VerificationCode = userRegisterInfoVO.getCode();

        Assert.isTrue(mobileFormat,ResponseEnum.MOBILE_ERROR);
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notNull(password, ResponseEnum.PASSWORD_NULL_ERROR);
        Assert.notNull(VerificationCode, ResponseEnum.CODE_NULL_ERROR);

        Assert.isTrue(userInfoService.checkVerificationCode(VerificationCode,mobile),ResponseEnum.CODE_ERROR);
        userInfoService.register(userRegisterInfoVO);
        return R.ok().message("注册成功");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R login(
            @ApiParam(value = "用户登录表单",required = true)
            @RequestBody LoginVO loginVO, HttpServletRequest request){
        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();

        Assert.notEmpty(mobile,ResponseEnum.MOBILE_NULL_ERROR);
        Assert.notEmpty(password,ResponseEnum.PASSWORD_NULL_ERROR);

        String ip = request.getRemoteAddr();
        if(StringUtils.isEmpty(ip) || ip.equals("0:0:0:0:0:0:0:1")){
            ip = request.getHeader("x-forwarded-for");
        }
        UserInfoVO userInfoVO = userInfoService.login(loginVO,ip);

        return R.ok().message("登录成功").data("userInfo",userInfoVO);
    }

    @ApiOperation("校验令牌")
    @GetMapping("/checkToken")
    public R checkToken(HttpServletRequest request){
        String token = request.getHeader("token");
        boolean result = JwtUtils.checkToken(token);

        return result ? R.ok() : R.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
    }

    @ApiOperation("检验手机号是否已注册")
    @GetMapping("/checkMobile/{mobile}")
    public Boolean checkMobile(@PathVariable String mobile){
        boolean mobileFormat = RegexValidateUtils.checkCellphone(mobile);
        Assert.notEmpty(mobile,ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(mobileFormat,ResponseEnum.MOBILE_ERROR);
        return userInfoService.checkMobile(mobile);
    }
}

