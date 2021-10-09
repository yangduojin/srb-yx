package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.BorrowInfoStatusEnum;
import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.BorrowInfoMapper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.IntegralGradeMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.BorrowInfo;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.atguigu.srb.core.service.LendService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {

    @Resource
    private IntegralGradeMapper integralGradeMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerMapper borrowerMapper;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private LendService lendService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        Integer integral = userInfo.getIntegral();
        QueryWrapper<IntegralGrade> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("integral_start",integral)
                .ge("integral_end",integral);
        IntegralGrade integralGrade = integralGradeMapper.selectOne(queryWrapper);

        if(integralGrade == null){
            return new BigDecimal(0);
        }
        return integralGrade.getBorrowAmount();
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 判断用户绑定状态
        Assert.isTrue(userInfo.getBindStatus().intValue() == UserBindEnum.BIND_OK.getStatus().intValue(),ResponseEnum.USER_NO_BIND_ERROR);
        // 判断借款人额度申请状态
        Assert.isTrue(userInfo.getBorrowAuthStatus().intValue() == BorrowerStatusEnum.AUTH_OK.getStatus().intValue(),ResponseEnum.USER_NO_AMOUNT_ERROR);

        BigDecimal borrowAmount = this.getBorrowAmount(userId);
        Assert.isTrue(borrowInfo.getAmount().doubleValue() <= borrowAmount.doubleValue(),ResponseEnum.USER_AMOUNT_LESS_ERROR);

        borrowInfo.setUserId(userId);
        BigDecimal yearRate = borrowInfo.getBorrowYearRate().divide(new BigDecimal(100));
        borrowInfo.setBorrowYearRate(yearRate);
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        baseMapper.insert(borrowInfo);
    }

    @Override
    public Integer getStatusByUserId(Long userId) {
        QueryWrapper<BorrowInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status").eq("user_id",userId);
        List<Object> objects = baseMapper.selectObjs(queryWrapper);
        if (objects.size() == 0) {
            return BorrowInfoStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer) objects.get(0);
        return status;
    }

    @Override
    public List<BorrowInfo> selectList() {
        List<BorrowInfo> borrowInfoList = baseMapper.selectBorrowInfoList();
        borrowInfoList.forEach(borrowInfo -> {
            String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
            String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
            String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());
            borrowInfo.getParam().put("returnMethod", returnMethod);
            borrowInfo.getParam().put("moneyUse", moneyUse);
            borrowInfo.getParam().put("status", status);
        });

        return borrowInfoList;
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(long id) {
        BorrowInfo borrowInfo = baseMapper.selectById(id);


        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", borrowInfo.getReturnMethod());
        String moneyUse = dictService.getNameByParentDictCodeAndValue("moneyUse", borrowInfo.getMoneyUse());
        String status = BorrowInfoStatusEnum.getMsgByStatus(borrowInfo.getStatus());

        borrowInfo.getParam().put("returnMethod",returnMethod);
        borrowInfo.getParam().put("moneyUse",moneyUse);
        borrowInfo.getParam().put("status",status);


        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.eq("user_id", borrowInfo.getUserId());
        Borrower borrower = borrowerMapper.selectOne(borrowerQueryWrapper);
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrower.getId());


        Map<String, Object> result = new HashMap<>();
        result.put("borrowInfo", borrowInfo);
        result.put("borrower", borrowerDetailVO);
        return result;
    }

    @Override
    public void approval(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        Long borrowInfId = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = baseMapper.selectById(borrowInfId);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        baseMapper.updateById(borrowInfo);


        //审核通过则创建标的
        if (borrowInfoApprovalVO.getStatus().intValue() == BorrowInfoStatusEnum.CHECK_OK.getStatus().intValue()) {
            //创建标的
            //TODO
            lendService.createLend(borrowInfoApprovalVO, borrowInfo);
        }

    }
}
