package com.atguigu.srb.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.srb.base.hfb.HfbConst;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.core.enums.LendStatusEnum;
import com.atguigu.srb.core.enums.ReturnMethodEnum;
import com.atguigu.srb.core.enums.TransTypeEnum;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.*;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.*;
import com.atguigu.srb.core.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Transactional(rollbackFor = Exception.class)
@Service
@Slf4j
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private UserAccountService userAccountService;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private TransFlowService transFlowService;

    @Resource
    private LendItemService lendItemService;

    @Resource
    private LendReturnService lendReturnService;

    @Resource
    private LendItemReturnService lendItemReturnService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createLend(BorrowInfoApprovalVO borrowInfoApprovalVO, BorrowInfo borrowInfo) {
        Lend lend = new Lend();
        lend.setUserId(borrowInfo.getUserId());
        lend.setBorrowInfoId(borrowInfo.getId());
        lend.setLendNo(LendNoUtils.getLendNo());
        lend.setTitle(borrowInfoApprovalVO.getTitle());
        lend.setAmount(borrowInfo.getAmount());
        lend.setPeriod(borrowInfo.getPeriod());
        lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));
        lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));
        lend.setReturnMethod(borrowInfo.getReturnMethod());
        lend.setLowestAmount(new BigDecimal(100));
        lend.setInvestAmount(new BigDecimal(0));
        lend.setInvestNum(0);
        lend.setPublishDate(LocalDateTime.now());


        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dtf);
        lend.setLendStartDate(lendStartDate);

        LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
        lend.setLendEndDate(lendEndDate);

        lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());

        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12),8, BigDecimal.ROUND_DOWN);
        BigDecimal expectAmount = lend.getAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        lend.setExpectAmount(expectAmount);

        lend.setRealAmount(new BigDecimal(0));
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        lend.setCheckTime(LocalDateTime.now());
        lend.setCheckAdminId(1L);
        baseMapper.insert(lend);

    }

    @Override
    public List<Lend> selectList() {
        List<Lend> lends = baseMapper.selectList(null);
        lends.forEach(lend -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
            String status = LendStatusEnum.getMsgByStatus(lend.getStatus());

            lend.getParam().put("returnMethod",returnMethod);
            lend.getParam().put("status",status);
        });
        return lends;
    }

    @Override
    public Map<String, Object> getLendDetailById(Long id) {
        Lend lend = baseMapper.selectById(id);
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());

        lend.getParam().put("returnMethod",returnMethod);
        lend.getParam().put("status",status);


        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", lend.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());


        HashMap<String, Object> result = new HashMap<>();
        result.put("lend",lend);
        result.put("borrower",borrowerDetailVO);

        return result;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalmonth, Integer returnMethod) {

        BigDecimal interestCount;

        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalmonth);
        } else if(returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalmonth);
        } else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalmonth);
        }
        return interestCount;
    }

//    @Transactional(rollbackFor = Exception.class)
//    @Override
    public void makeLoan111(Long id) {
        Lend lend = baseMapper.selectById(id);

        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentProjectCode", lend.getLendNo());
        String agentBillNo = LendNoUtils.getLoanNo();//放款编号
        paramMap.put("agentBillNo", agentBillNo);
        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12),8,BigDecimal.ROUND_DOWN);
        BigDecimal realAmount = lend.getInvestAmount().multiply(new BigDecimal(lend.getPeriod())).multiply(monthRate);
        paramMap.put("mchFee", realAmount);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);

        log.info("放款参数：" + JSONObject.toJSONString(paramMap));
        JSONObject result = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：" + result.toJSONString());

        if (!"0000".equals(result.getString("resultCode"))) {
            throw new BusinessException(result.getString("resultMsg"));
        }


        lend.setRealAmount(realAmount);
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(lend);


        UserInfo userInfo = userInfoMapper.selectById(lend.getUserId());
        String borrowerBindCode = userInfo.getBindCode();
        BigDecimal voteAmt = new BigDecimal(result.getString("voteAmt"));
        userAccountMapper.updateAccount(borrowerBindCode, voteAmt,new BigDecimal(0));


        TransFlowBO transFlowBO = new TransFlowBO(
                agentBillNo,
                borrowerBindCode,
                voteAmt,
                TransTypeEnum.BORROW_BACK,
                "放款到借款人账户");
        transFlowService.saveTransFlow(transFlowBO);


        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(),LendStatusEnum.INVEST_RUN.getStatus());
        lendItemList.stream().forEach(item -> {
            Long userId = item.getInvestUserId();
            UserInfo investorInfo = userInfoMapper.selectById(userId);
            String investorInfoBindCode = investorInfo.getBindCode();
            BigDecimal investAmount = item.getInvestAmount();

            userAccountMapper.updateAccount(investorInfoBindCode,new BigDecimal(0),investAmount.negate());

            TransFlowBO investorTF = new TransFlowBO(
                    LendNoUtils.getTransNo(),
                    investorInfoBindCode,
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "冻结资金转出，出借放款，编号：" + lend.getLendNo());
            transFlowService.saveTransFlow(investorTF);
        });


        this.repaymentPlan(lend);
    }


    private void repaymentPlan(Lend lend) {
        List<LendReturn> lendReturnList = new ArrayList<>();
        for (int i = 1; i <= lend.getPeriod().intValue(); i++) {
            LendReturn lendReturn = new LendReturn();
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setBaseAmount(lend.getInvestAmount());
            lendReturn.setCurrentPeriod(i);
            lendReturn.setLendYearRate(lend.getLendYearRate());
            Integer returnMethod = lend.getReturnMethod();
            lendReturn.setReturnMethod(returnMethod);
            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i));
            lendReturn.setOverdue(false);
            if(i == lend.getPeriod().intValue()){
                lendReturn.setLast(true);
            }else {
                lendReturn.setLast(false);
            }
            lend.setStatus(0);
            lendReturnList.add(lendReturn);
        }
        lendReturnService.saveBatch(lendReturnList);


        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );


        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), LendStatusEnum.INVEST_RUN.getStatus());
        for (LendItem lendItem : lendItemList) {
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(),lendReturnMap,lend);
            lendItemReturnAllList.addAll(lendItemReturnList);
        }

        for (LendReturn lendReturn : lendReturnList) {

            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal).reduce(BigDecimal.ONE,BigDecimal::add);
            lendReturn.setPrincipal(sumPrincipal);
            lendReturn.setInterest(sumInterest);
            lendReturn.setTotal(sumTotal);
        }
        lendReturnService.updateBatchById(lendReturnList);
    }

    private List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {
        LendItem lendItem = lendItemService.getById(lendItemId);
        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        BigDecimal amount = lendItem.getInvestAmount();
        BigDecimal yearRate = lendItem.getLendYearRate();
        int totalMonth = lend.getPeriod();

        Integer returnMethod = lend.getReturnMethod();
            switch (returnMethod){
                case 1:
                    mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                    mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                    break;
                case 2:
                    mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                    mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                    break;
                case 3:
                    mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                    mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                    break;
                case 4:
                    mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                    mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                    break;
            }


        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();
            Long lendReturnId = lendReturnMap.get(currentPeriod);
            LendItemReturn lendItemReturn = new LendItemReturn();
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setReturnMethod(returnMethod);

            if(lendItemReturnList.size() > 0 && currentPeriod.intValue() == lend.getPeriod().intValue()){
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);
            }else{
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }
            BigDecimal principal = lendItemReturn.getPrincipal();
            BigDecimal interest = lendItemReturn.getInterest();
            lendItemReturn.setTotal(principal.add(interest));
            lendItemReturn.setFee(new BigDecimal(0));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));

            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);

            lendItemReturnList.add(lendItemReturn);
        }
        lendItemReturnService.saveBatch(lendItemReturnList);

        return lendItemReturnList;
    }




    @Transactional(rollbackFor = Exception.class)
    @Override
    public void makeLoan(Long id) {
        Lend lend = baseMapper.selectById(id);

        HashMap<String, Object> map = new HashMap<>();
        map.put("agentId",HfbConst.AGENT_ID);
        map.put("agentProjectCode",lend.getLendNo());
        map.put("agentBillNo",LendNoUtils.getLoanNo());

//        BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12),8,BigDecimal.ROUND_DOWN);
//        BigDecimal realAmount = lend.getInvestAmount().multiply(new BigDecimal(lend.getPeriod())).multiply(monthRate);

        BigDecimal mchFee = lend.getServiceRate().divide(new BigDecimal(12),2,BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal(lend.getPeriod())).multiply(lend.getInvestAmount());
        map.put("mchFee",mchFee);
        map.put("timestamp",LocalDateTime.now());
        map.put("sign",RequestHelper.getSign(map));

        log.info("放款参数：" + JSONObject.toJSONString(map));
        JSONObject result = RequestHelper.sendRequest(map, HfbConst.MAKE_LOAD_URL);
        log.info("放款结果：" + result.toJSONString());

        if(!"0000".equals(result.getString("resultCode"))){
            throw new BusinessException(result.getString("resultMsg"));
        }
        //        （1）标的状态和标的平台收益
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        lend.setRealAmount(mchFee);
        lend.setPaymentTime(LocalDateTime.now());
        baseMapper.updateById(lend);


        UserInfo userInfo = userInfoMapper.selectById(lend.getUserId());
        String bindCode = userInfo.getBindCode();
        BigDecimal realAmount = new BigDecimal((String) result.get("voteAmt"))
                .subtract(new BigDecimal((String) result.get("mchFee")));
        userAccountMapper.updateAccount(bindCode,realAmount,new BigDecimal(0));

        TransFlowBO transFlowBO = new TransFlowBO(
                LendNoUtils.getLoanNo(),
                bindCode,
                realAmount,
                TransTypeEnum.BORROW_BACK,
                "放款，将款放入借款者账号中，扣除手续费"
        );
        transFlowService.saveTransFlow(transFlowBO);

        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(), 1);

        lendItemList.forEach(item -> {
            Long userId = item.getInvestUserId();
            UserInfo investorUserInfo = userInfoMapper.selectById(userId);
            BigDecimal investAmount = item.getInvestAmount();
            userAccountMapper.updateAccount(investorUserInfo.getBindCode(),new BigDecimal(0),investAmount.negate());

            TransFlowBO investorTransFlowBO = new TransFlowBO(
                    LendNoUtils.getLoanNo(),
                    investorUserInfo.getBindCode(),
                    investAmount,
                    TransTypeEnum.INVEST_UNLOCK,
                    "冻结资金转出，出借放款，编号：" + lend.getLendNo());
            transFlowService.saveTransFlow(investorTransFlowBO);
        });

//        （2）给借款账号转入金额
//        （3）增加借款交易流水
//        （4）解冻并扣除投资人资金
//        （5）增加投资人交易流水

//        （6）生成借款人还款计划和出借人回款计划
        this.repaymentPlan11(lend);
    }

    private void repaymentPlan11(Lend lend) {
        ArrayList<LendReturn> lendReturnList = new ArrayList<>();
        for (int i = 1; i <= lend.getPeriod() ; i++) {
            LendReturn lendReturn = new LendReturn();
            lendReturn.setLendId(lend.getId());
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());
            lendReturn.setUserId(lend.getUserId());
            lendReturn.setAmount(lend.getAmount());
            lendReturn.setCurrentPeriod(i);
            lendReturn.setLendYearRate(lend.getLendYearRate());
            lendReturn.setReturnMethod(lend.getReturnMethod());
            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i));
            lendReturn.setOverdue(false);
            lendReturn.setOverdueTotal(new BigDecimal(0));
            lendReturn.setStatus(0);
            lendReturn.setBaseAmount(lend.getInvestAmount());
            if(i == lend.getPeriod()){
                lendReturn.setLast(true);
            }else {
                lendReturn.setLast(false);
            }
            lendReturnList.add(lendReturn);
        }
        lendReturnService.saveBatch(lendReturnList);

        Map<Integer, Long> map = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );

        ArrayList<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        List<LendItem> lendItemList = lendItemService.selectByLendId(lend.getId(),1);

        for (LendItem lendItem : lendItemList) {
            List<LendItemReturn> lendItemReturns = this.returnInvestPlan(lend,map,lendItem.getId());
            lendItemReturnAllList.addAll(lendItemReturns);
        }

        for (LendReturn lendReturn : lendReturnList) {
            BigDecimal totalPrincipal = lendItemReturnAllList.stream().filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getPrincipal).reduce(BigDecimal.ZERO,BigDecimal::add);

            BigDecimal totalInterest = lendItemReturnAllList.stream().filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest).reduce(BigDecimal.ZERO,BigDecimal::add);

            BigDecimal total = lendItemReturnAllList.stream().filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);

            lendReturn.setPrincipal(totalPrincipal);
            lendReturn.setInterest(totalInterest);
            lendReturn.setTotal(total);
        }

        lendReturnService.updateBatchById(lendReturnList);

    }

    private List<LendItemReturn> returnInvestPlan(Lend lend, Map<Integer, Long> map, Long lendItemId) {
        LendItem lendItem = lendItemService.getById(lendItemId);

        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金

        BigDecimal yearRate = lendItem.getLendYearRate();
        BigDecimal amount = lendItem.getInvestAmount();
        Integer totalMonth = lend.getPeriod();

        Integer returnMethod = lend.getReturnMethod();
        switch (returnMethod){
            case 1:
                mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                break;
            case 2:
                mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                break;
            case 3:
                mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                break;
            case 4:
                mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
                mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
                break;
        }

        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();
            Long lendReturnId = map.get(currentPeriod);
            LendItemReturn lendItemReturn = new LendItemReturn();
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setLendId(lend.getId());
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setLendYearRate(lendItem.getLendYearRate());
            lendItemReturn.setReturnMethod(lend.getReturnMethod());
            if(lendItemReturnList.size() > 0 && currentPeriod.intValue() == lend.getPeriod().intValue()){
                BigDecimal sumPrincipal = lendItemReturnList.stream().map(LendItemReturn::getPrincipal).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal,MathContext.UNLIMITED);
                lendItemReturn.setPrincipal(lastPrincipal);

                BigDecimal sumInterest = lendItemReturnList.stream().map(LendItemReturn::getInterest).reduce(BigDecimal.ZERO,BigDecimal::add);
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest, MathContext.UNLIMITED);
                lendItemReturn.setInterest(lastInterest);

            }else {
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            lendItemReturn.setFee(new BigDecimal(0));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            lendItemReturn.setOverdue(false);
            lendItemReturn.setOverdueTotal(new BigDecimal(0));
            lendItemReturn.setStatus(0);

            lendItemReturnList.add(lendItemReturn);
        }

        lendItemReturnService.saveBatch(lendItemReturnList);
        return lendItemReturnList;
    }


}