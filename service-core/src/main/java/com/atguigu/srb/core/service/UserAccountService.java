package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface UserAccountService extends IService<UserAccount> {

    String commitCharge(BigDecimal chargeAmt, Long userId);

    void hfbNotify(Map<String, Object> paramMap);

    BigDecimal getAccountAmount(Long userId);

    UserAccount getAccountByUserId(Long userId);

    String commitWithdraw(BigDecimal fetchAmt, Long userId);

    void notifyWithdraw(Map<String, Object> paramMap);
}
