package com.atguigu.srb.sms.client;

import com.atguigu.srb.sms.client.fallback.CoreUserInfoClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-core",fallback = CoreUserInfoClientFallback.class)
public interface CoreUserInfoClient {

    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    Boolean checkMobile(@PathVariable String mobile);
}
