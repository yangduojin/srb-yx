package com.atguigu.srb.sms.servcie.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.sms.servcie.SmsService;
import com.atguigu.srb.sms.util.SmsProperties;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void send(String mobile, String templateCode, HashMap<String, Object> param) {

        DefaultProfile profile = DefaultProfile.getProfile(
                SmsProperties.REGION_Id,
                SmsProperties.KEY_ID,
                SmsProperties.KEY_SECRET);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        request.putQueryParameter("RegionId", SmsProperties.REGION_Id);
        request.putQueryParameter("PhoneNumbers", mobile);
        request.putQueryParameter("SignName", SmsProperties.SIGN_NAME);
        request.putQueryParameter("TemplateCode", templateCode);

        Gson gson = new Gson();
        String json = gson.toJson(param);
        request.putQueryParameter("TemplateParam", json);
        try {
            CommonResponse response = client.getCommonResponse(request);
            boolean success = response.getHttpResponse().isSuccess();
            Assert.isTrue(success, ResponseEnum.ALIYUN_SMS_ERROR);
            redisTemplate.opsForValue().set("srb:sms:code:" + mobile,param.get("code"),60*24, TimeUnit.MINUTES);

            String data = response.getData();
            HashMap<String,String> resultMap = gson.fromJson(data, HashMap.class);
            String code = resultMap.get("Code");
            String message = resultMap.get("Message");
            log.info("阿里云短信发送响应结果：");
            log.info("code：" + code);
            log.info("message：" + message);

            Assert.notEquals("isv.BUSINESS_LIMIT_CONTROL",code,ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);
            Assert.equals("OK",code,ResponseEnum.ALIYUN_SMS_ERROR);

        } catch (ServerException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR , e);
        }catch (ClientException e) {
            log.error("阿里云短信发送SDK调用失败：");
            log.error("ErrorCode=" + e.getErrCode());
            log.error("ErrorMessage=" + e.getErrMsg());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR , e);
        }

    }
}
