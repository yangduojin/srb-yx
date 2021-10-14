package com.atguigu.srb.sms;

import com.atguigu.srb.base.dto.SmsDTO;
import com.atguigu.srb.rabbitutil.constant.MQConst;
import com.atguigu.srb.rabbitutil.service.MQService;
import com.atguigu.srb.sms.util.SmsProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest(classes = ServiceSmsApplication.class)
@RunWith(SpringRunner.class)
public class test {

    @Resource
    private MQService mqService;

    @Test
    public void test(){
        System.out.println(SmsProperties.KEY_ID);
        System.out.println(SmsProperties.KEY_SECRET);
        System.out.println(SmsProperties.REGION_Id);
    }

    @Test
    public void testMQ(){
        System.out.println(111);
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setMessage("充值成功，来自userAccountService");
        smsDTO.setMobile("18582110666");
        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS, MQConst.ROUTING_SMS_ITEM, smsDTO);
    }
}
