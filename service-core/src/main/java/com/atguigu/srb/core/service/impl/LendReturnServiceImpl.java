package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.srb.base.hfb.FormHelper;
import com.atguigu.srb.base.hfb.HfbConst;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.mapper.*;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.LendNoUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Transactional(rollbackFor = Exception.class)
@Service
@Slf4j
public class LendReturnServiceImpl extends ServiceImpl<LendReturnMapper, LendReturn> implements LendReturnService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserBindService userBindService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Resource
    private LendItemReturnMapper lendItemReturnMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendItemMapper lendItemMapper;


    @Override
    public List<LendReturn> selectByLendId(Long lendId) {
        QueryWrapper<LendReturn> lendReturnQueryWrapper = new QueryWrapper<>();
        lendReturnQueryWrapper.eq("lend_id",lendId);
        return baseMapper.selectList(lendReturnQueryWrapper);
    }

    @Override
    public String commitReturn(Long lendReturnId, Long userId) {
        LendReturn lendReturn = baseMapper.selectById(lendReturnId);

        BigDecimal accountAmount = userAccountService.getAccountAmount(userId);
        Assert.isTrue(accountAmount.doubleValue() >= lendReturn.getTotal().doubleValue(), ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);


        String bindCode = userBindService.getBindCodeByUserId(userId);

        Long lendId = lendReturn.getLendId();
        Lend lend = lendMapper.selectById(lendId);
        HashMap<String, Object> paramMap = new HashMap<>();

        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentGoodsName", lend.getTitle());
        paramMap.put("agentBatchNo",lendReturn.getReturnNo());
        paramMap.put("fromBindCode",bindCode);
        paramMap.put("totalAmt",lendReturn.getTotal());
        paramMap.put("note", "");
        List<Map<String, Object>> lendItemReturnDetailList = lendItemReturnService.addReturnDetail(lendReturnId);
        paramMap.put("data", JSON.toJSON(lendItemReturnDetailList));
        paramMap.put("voteFeeAmt", new BigDecimal(0));
        paramMap.put("returnUrl",HfbConst.BORROW_RETURN_RETURN_URL);
        paramMap.put("notifyUrl",HfbConst.BORROW_RETURN_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        String formStr = FormHelper.buildForm(HfbConst.BORROW_RETURN_URL, paramMap);

        return formStr;
    }

    @Override
    public String notifyUrl(Map<String, Object> map) {
        log.info("还款成功");
        String agentBatchNo = (String) map.get("agentBatchNo");

        boolean result = transFlowService.isSaveTransFlow(agentBatchNo);
        if(result){
            log.warn("幂等性返回");
            return "success";
        }

        String voteFeeAmt = (String)map.get("voteFeeAmt");
        QueryWrapper<LendReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("return_no",agentBatchNo);
        LendReturn lendReturn = baseMapper.selectOne(queryWrapper);
        lendReturn.setRealReturnTime(LocalDateTime.now());
        lendReturn.setStatus(1);
        lendReturn.setFee(new BigDecimal(voteFeeAmt));
        baseMapper.updateById(lendReturn);

        Lend lend = lendMapper.selectById(lendReturn.getLendId());

        if(lendReturn.getLast()){
            lend.setStatus(LendStatusEnum.PAY_OK.getStatus());
            lendMapper.updateById(lend);
        }

        BigDecimal totalAmt = new BigDecimal((String) map.get("totalAmt"));
        String bindCodeByUserId = userBindService.getBindCodeByUserId(lendReturn.getUserId());

        userAccountMapper.updateAccount(bindCodeByUserId,totalAmt.negate(),new BigDecimal(0));

        TransFlowBO transFlowBO = new TransFlowBO(
                agentBatchNo,
                bindCodeByUserId,
                totalAmt,
                TransTypeEnum.RETURN_DOWN,
                "借款人还款扣减，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
        transFlowService.saveTransFlow(transFlowBO);

        this.returnStart(lendReturn.getId(),lend);

        return "success";
    }

    private void returnStart(Long lendReturnId, Lend lend) {
        List<LendItemReturn> lendItemReturns = lendItemReturnService.selectLendItemReturnList(lendReturnId);

        for (LendItemReturn lendItemReturn : lendItemReturns) {
            lendItemReturn.setRealReturnTime(LocalDateTime.now());
            lendItemReturn.setStatus(1);
            lendItemReturnMapper.updateById(lendItemReturn);

            LendItem lendItem = lendItemMapper.selectById(lendItemReturn.getLendItemId());
            lendItem.setRealAmount(lendItem.getRealAmount().add(lendItemReturn.getInterest()));
            lendItemMapper.updateById(lendItem);

            String bindCode = userBindService.getBindCodeByUserId(lendItemReturn.getInvestUserId());
            userAccountMapper.updateAccount(bindCode,lendItemReturn.getTotal(),new BigDecimal(0));;

            TransFlowBO transFlowBO = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    bindCode,
                    lendItemReturn.getTotal(),
                    TransTypeEnum.INVEST_BACK,
                    "还款到账，项目编号：" + lend.getLendNo() + "，项目名称：" + lend.getTitle());
            transFlowService.saveTransFlow(transFlowBO);
        }
    }
}
