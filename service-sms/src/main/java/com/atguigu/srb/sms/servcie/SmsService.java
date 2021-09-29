package com.atguigu.srb.sms.servcie;


import java.util.HashMap;

public interface SmsService {
    void send(String mobile, String templateCode, HashMap<String, Object> map);
}
