package com.atguigu.srb.sms;

import com.atguigu.srb.sms.util.SmsProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ServiceSmsApplication.class)
@RunWith(SpringRunner.class)
public class test {



    @Test
    public void test(){
        System.out.println(SmsProperties.KEY_ID);
        System.out.println(SmsProperties.KEY_SECRET);
        System.out.println(SmsProperties.REGION_Id);
    }
}
