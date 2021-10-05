package com.atguigu.srb.core.controller;


/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
//@RestController
//@RequestMapping("/api/core/userInfo")
//@CrossOrigin
//@Api(tags = "注册接口")
public class UserInfoController {

//    @Autowired
//    UserInfoService userInfoService;
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @ApiOperation("保存用户注册信息")
//    @PostMapping("/register")
//    public R UserRegister(
//            @ApiParam(value = "用户信息表单",required = true)
//            @RequestBody UserRegisterInfoVO userInfoVO
//            ){
//        Assert.notEmpty(userInfoVO.getMobile(), ResponseEnum.MOBILE_NULL_ERROR);
//        Assert.isTrue(RegexValidateUtils.checkCellphone(userInfoVO.getMobile()),ResponseEnum.MOBILE_ERROR);
//        String redisCode = (String) redisTemplate.opsForValue().get("srb:sms:code:" + userInfoVO.getMobile());
//        Assert.isTrue(userInfoVO.getCode().equals(redisCode),ResponseEnum.CODE_ERROR);
//        userInfoService.register(userInfoVO);
//        return R.ok().message("注册成功");
//    }
}
