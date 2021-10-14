package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.srb.base.dto.SmsDTO;
import com.atguigu.srb.base.hfb.FormHelper;
import com.atguigu.srb.base.hfb.HfbConst;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.service.TransFlowService;
import com.atguigu.srb.core.service.UserAccountService;
import com.atguigu.srb.core.service.UserBindService;
import com.atguigu.srb.core.util.LendNoUtils;
import com.atguigu.srb.rabbitutil.constant.MQConst;
import com.atguigu.srb.rabbitutil.service.MQService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
@Slf4j
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private UserBindService userBindService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private MQService mqService;


    @Override
    public String commitCharge(BigDecimal chargeAmt, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        String bindCode = userInfo.getBindCode();
        Assert.notEmpty(bindCode, ResponseEnum.USER_NO_BIND_ERROR);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigDecimal("0"));
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);//检查常量是否正确
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void hfbNotify(Map<String, Object> paramMap) {
        log.info("充值成功：" + JSONObject.toJSONString(paramMap));

        String bindCode = (String) paramMap.get("bindCode");
        String chargeAmt = (String)paramMap.get("chargeAmt");

        String agentBillNo = (String)paramMap.get("agentBillNo");
        boolean saveTransFlow = transFlowService.isSaveTransFlow(agentBillNo);
        if(saveTransFlow){
            log.warn("幂等性返回");
            return ;
        }

        baseMapper.updateAccount(bindCode, new BigDecimal(chargeAmt), new BigDecimal(0));


        TransFlowBO transFlowBO = new TransFlowBO(
            agentBillNo,
            bindCode,
            new BigDecimal(chargeAmt),
            TransTypeEnum.RECHARGE,
            "充值");
        transFlowService.saveTransFlow(transFlowBO);

        // mq
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setMessage("充值成功，来自userAccountService");
        smsDTO.setMobile("18582110666");
        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS, MQConst.ROUTING_SMS_ITEM, smsDTO);

    }

    @Override
    public BigDecimal getAccountAmount(Long userId) {
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id",userId);
        UserAccount userAccount = baseMapper.selectOne(userAccountQueryWrapper);

        return userAccount.getAmount();
    }

    @Override
    public UserAccount getAccountByUserId(Long userId) {
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id",userId);
        return baseMapper.selectOne(userAccountQueryWrapper);
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        BigDecimal accountAmount = this.getAccountAmount(userId);
        Assert.isTrue(accountAmount.doubleValue() >= fetchAmt.doubleValue(),ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        Assert.isTrue(fetchAmt.doubleValue() > 0 , ResponseEnum.BORROW_AMOUNT_ZERO_ERROR);

        String bindCode = userBindService.getBindCodeByUserId(userId);

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId",HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode",bindCode);
        paramMap.put("fetchAmt",fetchAmt);
        paramMap.put("feeAmt",new BigDecimal(0));
        paramMap.put("returnUrl",HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("notifyUrl",HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("timestamp",RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign",sign);

        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);

        return formStr;
    }

    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {

        log.info("提现成功");
        String agentBillNo = (String) paramMap.get("agentBillNo");


        //幂等性
        boolean saveTransFlow = transFlowService.isSaveTransFlow(agentBillNo);
        if(saveTransFlow){
            log.warn("幂等性返回");
            return;
        }


        String bindCode = (String) paramMap.get("bindCode");
        String fetchAmt = (String) paramMap.get("fetchAmt");
        userAccountMapper.updateAccount(bindCode,new BigDecimal(fetchAmt).negate(),new BigDecimal(0));

        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(fetchAmt),
                TransTypeEnum.WITHDRAW,
                "提现"
        );
        transFlowService.saveTransFlow(transFlowBO);

    }
}
