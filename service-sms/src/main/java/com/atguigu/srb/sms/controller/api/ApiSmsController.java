package com.atguigu.srb.sms.controller.api;


import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.RandomUtils;
import com.atguigu.srb.common.util.RegexValidateUtils;
import com.atguigu.srb.sms.client.CoreUserInfoClient;
import com.atguigu.srb.sms.servcie.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理")
//@CrossOrigin
@Slf4j
public class ApiSmsController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private CoreUserInfoClient coreUserInfoClient ;

    @ApiOperation("根据手机号获取验证码")
    @GetMapping("/send/{mobile}")
    public R send(
            @ApiParam(value = "手机号",required = true)
            @PathVariable("mobile") String mobile){
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile),ResponseEnum.MOBILE_ERROR);

        Boolean exist = coreUserInfoClient.checkMobile(mobile);
        Assert.isTrue(exist == false ,ResponseEnum.MOBILE_EXIST_ERROR);

        String code = RandomUtils.getFourBitRandom();
        HashMap<String, Object> param = new HashMap<>();
        param.put("code",code);
        smsService.send(mobile, SmsProperties.TEMPLATE_CODE,param);
        return R.ok().message("短信发送成功");
    }
}
