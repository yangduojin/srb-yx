package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.RegexValidateUtils;
import com.atguigu.srb.core.pojo.entity.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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
@CrossOrigin
@Api(tags = "注册接口")
public class ApiUserInfoController {

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation("保存用户注册信息")
    @PostMapping("/register")
    public R UserRegister(
            @ApiParam(value = "用户信息表单",required = true)
            @RequestBody UserInfoVO userInfoVO
            ){
        Assert.notEmpty(userInfoVO.getMobile(), ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(RegexValidateUtils.checkCellphone(userInfoVO.getMobile()),ResponseEnum.MOBILE_ERROR);
        Assert.notEmpty(userInfoVO.getPassword(), ResponseEnum.PASSWORD_NULL_ERROR);
        Assert.notEmpty(userInfoVO.getCode(), ResponseEnum.CODE_NULL_ERROR);

        String redisCode = (String) redisTemplate.opsForValue().get("srb:sms:code:" + userInfoVO.getMobile());
        Assert.isTrue(userInfoVO.getCode().equals(redisCode),ResponseEnum.CODE_ERROR);
        userInfoService.register(userInfoVO);
        return R.ok().message("注册成功");
    }
}

