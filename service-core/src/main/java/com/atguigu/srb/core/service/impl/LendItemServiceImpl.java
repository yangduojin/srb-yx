package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
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
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.vo.InvestVO;
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
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
@Slf4j
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendMapper lendMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private LendService lendService;

    @Resource
    private UserBindService userBindService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String commitInvest(InvestVO investVO) {
        Lend lend = lendMapper.selectById(investVO.getLendId());
        Assert.equals(lend.getStatus(), LendStatusEnum.INVEST_RUN.getStatus(), ResponseEnum.LEND_INVEST_ERROR);


        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(),ResponseEnum.LEND_FULL_SCALE_ERROR);


        Long investUserId = investVO.getInvestUserId();
        BigDecimal account = userAccountService.getAccountAmount(investUserId);

        Assert.isTrue(account.doubleValue() >= Double.parseDouble(investVO.getInvestAmount()),ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);


        LendItem lendItem = new LendItem();
        lendItem.setInvestUserId(investUserId);//投资人id
        lendItem.setInvestName(investVO.getInvestName());//投资人名字
        String lendItemNo = LendNoUtils.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo); //投资条目编号（一个Lend对应一个或多个LendItem）
        lendItem.setLendId(investVO.getLendId());//对应的标的id
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount()));
        lendItem.setLendYearRate(lend.getLendYearRate());
        lendItem.setInvestTime(LocalDateTime.now());
        lendItem.setLendStartDate(lend.getLendStartDate());
        lendItem.setLendEndDate(lend.getLendEndDate());
        BigDecimal expectAmount = lendService.getInterestCount(
                lendItem.getInvestAmount(),
                lendItem.getLendYearRate(),
                lend.getPeriod(),
                lend.getReturnMethod());
        lendItem.setExpectAmount(expectAmount);

        lendItem.setRealAmount(new BigDecimal(0));

        lendItem.setStatus(0);//默认状态：刚刚创建
        baseMapper.insert(lendItem);


        String investorBindCode = userBindService.getBindCodeByUserId(investUserId);
        String borrowerBindCode = userBindService.getBindCodeByUserId(lend.getUserId());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", investorBindCode);
        paramMap.put("benefitBindCode",borrowerBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());


        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        String formStr = FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void hfbNotify(Map<String, Object> paramMap) {
        log.info("投标：" + JSONObject.toJSONString(paramMap));

        String bindCode = (String) paramMap.get("voteBindCode");
        String voteAmt = (String)paramMap.get("voteAmt");

        String agentBillNo = (String)paramMap.get("agentBillNo");
        boolean result = transFlowService.isSaveTransFlow(agentBillNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }


        userAccountMapper.updateAccount(bindCode,new BigDecimal("-" + voteAmt), new BigDecimal(voteAmt));


        LendItem lendItem = this.getByLendItemNo(agentBillNo);
        lendItem.setStatus(1);
        baseMapper.updateById(lendItem);


        Long lendId = lendItem.getLendId();
        Lend lend = lendMapper.selectById(lendId);
        lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));
        lend.setInvestNum(lend.getInvestNum() + 1);
        lendMapper.updateById(lend);

        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                bindCode,
                new BigDecimal(voteAmt),
                TransTypeEnum.INVEST_LOCK,
                "投资者投资冻结资金");
        transFlowService.saveTransFlow(transFlowBO);
    }

    @Override
    public List<LendItem> selectByLendId(Long id, Integer status) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("lend_id",id)
                .eq("status",status);
        List<LendItem> lendItemList = baseMapper.selectList(lendItemQueryWrapper);
        return lendItemList;
    }

    @Override
    public List<LendItem> listByLendId(Long lendId) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("Lend_id",lendId);
        return baseMapper.selectList(lendItemQueryWrapper);
    }

    private LendItem getByLendItemNo(String lendItemNo) {
        QueryWrapper<LendItem> lendItemQueryWrapper = new QueryWrapper<>();
        lendItemQueryWrapper.eq("lend_item_no",lendItemNo);
        return baseMapper.selectOne(lendItemQueryWrapper);
    }
}
