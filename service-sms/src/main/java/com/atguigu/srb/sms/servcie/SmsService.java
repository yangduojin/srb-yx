package com.atguigu.srb.sms.servcie;


import java.util.Map;

public interface SmsService {
    void send(String mobile, String templateCode, Map<String, Object> map);

    void sendToRedis(String mobile, String templateCode, Map<String, Object> param);
}
