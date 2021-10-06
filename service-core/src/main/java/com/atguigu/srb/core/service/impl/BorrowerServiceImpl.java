package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.enums.BorrowerStatusEnum;
import com.atguigu.srb.core.enums.IntegralEnum;
import com.atguigu.srb.core.mapper.BorrowerAttachMapper;
import com.atguigu.srb.core.mapper.BorrowerMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.mapper.UserIntegralMapper;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.BorrowerAttach;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserIntegral;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerAttachVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerVO;
import com.atguigu.srb.core.service.BorrowerAttachService;
import com.atguigu.srb.core.service.BorrowerService;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private BorrowerAttachMapper borrowerAttachMapper;

    @Resource
    private DictService dictService;

    @Resource
    private BorrowerAttachService borrowerAttachService;

    @Resource
    private UserIntegralMapper userIntegralMapper;


    @Override
    public void saveBorrowerInfo(Long userId, BorrowerVO borrowerVO) {

        UserInfo userInfo = userInfoMapper.selectById(userId);


        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO,borrower);
        borrower.setName(userInfo.getName());
        borrower.setUserId(userId);
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setMobile(userInfo.getMobile());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        baseMapper.insert(borrower);


        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.forEach(borrowerAttach -> {
            borrowerAttach.setBorrowerId(borrower.getId());
            borrowerAttachMapper.insert(borrowerAttach);
        });


        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public Integer getBorrowerStatus(Long userId) {
        QueryWrapper<Borrower> borrowerQueryWrapper = new QueryWrapper<>();
        borrowerQueryWrapper.select("status").eq("user_id",userId);
        List<Object> objects = baseMapper.selectObjs(borrowerQueryWrapper);
        if(objects.size() == 0){
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer) objects.get(0);
        return status;
    }

    @Override
    public Page<Borrower> borrowerList(Page<Borrower> pageModel, String keyword) {
        if(StringUtils.isEmpty(keyword)){
            return baseMapper.selectPage(pageModel,null);
        }
        QueryWrapper<Borrower> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name",keyword)
                .or().like("mobile",keyword)
                .or().like("id_card",keyword)
                .orderByDesc("id");
        return baseMapper.selectPage(pageModel, queryWrapper);
    }

    @Override
    public BorrowerDetailVO getBorrowerDetailInfo(Integer id) {
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        Borrower borrower = baseMapper.selectById(id);
        BeanUtils.copyProperties(borrower,borrowerDetailVO);


        String industry = dictService.getNameByParentDictCodeAndValue("industry",borrower.getIndustry());
        String education = dictService.getNameByParentDictCodeAndValue("education",borrower.getEducation());
        String income = dictService.getNameByParentDictCodeAndValue("income",borrower.getIncome());
        String returnSource = dictService.getNameByParentDictCodeAndValue("returnSource",borrower.getReturnSource());
        String relation = dictService.getNameByParentDictCodeAndValue("relation",borrower.getContactsRelation());
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());


        borrowerDetailVO.setSex(borrower.getSex() == 1 ? "男" : "女");
        borrowerDetailVO.setMarry(borrower.getMarry() == true ? "已婚" : "未婚");
        borrowerDetailVO.setIndustry(industry);
        borrowerDetailVO.setEducation(education);
        borrowerDetailVO.setIncome(income);
        borrowerDetailVO.setReturnSource(returnSource);
        borrowerDetailVO.setContactsRelation(relation);
        borrowerDetailVO.setStatus(status);


        List<BorrowerAttachVO> borrowerAttachVOList =  borrowerAttachService.selectBorrowerAttachVOList(id);

        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approval(BorrowerApprovalVO borrowerApprovalVO) {
        Integer status = borrowerApprovalVO.getStatus();
        Long borrowerId = borrowerApprovalVO.getBorrowerId();
        Borrower borrower = baseMapper.selectById(borrowerId);
        borrower.setStatus(status);
        baseMapper.updateById(borrower);


        Integer infoIntegral = borrowerApprovalVO.getInfoIntegral();
        Boolean checkIdCard = borrowerApprovalVO.getIsIdCardOk();
        Boolean checkHouse = borrowerApprovalVO.getIsHouseOk();
        Boolean checkCar = borrowerApprovalVO.getIsCarOk();


        Long userId = borrower.getUserId();
        UserInfo userInfo = userInfoMapper.selectById(userId);
        Integer originIntegral = userInfo.getIntegral();
        Integer currentIntegral = originIntegral + infoIntegral;


        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(userId);
        userIntegral.setIntegral(infoIntegral);
        userIntegral.setContent("借款人基本信息");
        userIntegralMapper.insert(userIntegral);


        if(checkIdCard){
            currentIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            userIntegralMapper.insert(userIntegral);
        }
        if(checkHouse){
            currentIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            userIntegralMapper.insert(userIntegral);
        }
        if(checkCar){
            currentIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userId);
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            userIntegralMapper.insert(userIntegral);
        }

//        if(status == -1 ){
//            borrower.setStatus(BorrowerStatusEnum.AUTH_FAIL.getStatus());
//            baseMapper.updateById(borrower);
//            userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_FAIL.getStatus());
//            userInfoMapper.updateById(userInfo);
//            return;
//        }


        userInfo.setBorrowAuthStatus(status);
        userInfo.setIntegral(currentIntegral);
        userInfoMapper.updateById(userInfo);
    }
}
