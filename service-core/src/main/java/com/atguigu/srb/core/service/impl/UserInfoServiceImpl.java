package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.MD5;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserAccountMapper userAccountMapper;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void register(UserInfoVO userInfoVO) {

        Integer count = userInfoMapper.selectCount(new QueryWrapper<UserInfo>().eq("mobile", userInfoVO.getMobile()));
        Assert.isTrue(count == 0,ResponseEnum.MOBILE_ERROR.MOBILE_EXIST_ERROR);

        UserInfo userInfo = new UserInfo();
        userInfo.setMobile(userInfoVO.getMobile());
        userInfo.setNickName(userInfoVO.getMobile());
        userInfo.setName(userInfoVO.getMobile());
        userInfo.setPassword(MD5.encrypt(userInfoVO.getPassword()));
        userInfo.setUserType(userInfoVO.getUserType());
        userInfo.setStatus(UserInfo.STATUS_NORMAL);

        userInfoMapper.insert(userInfo);

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);
    }
}
