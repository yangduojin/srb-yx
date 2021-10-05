package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.util.MD5;
import com.atguigu.srb.core.mapper.UserAccountMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.mapper.UserLoginRecordMapper;
import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.atguigu.srb.core.pojo.entity.query.UserInfoQuery;
import com.atguigu.srb.core.pojo.entity.vo.LoginVO;
import com.atguigu.srb.core.pojo.entity.vo.UserInfoVO;
import com.atguigu.srb.core.pojo.entity.vo.UserRegisterInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    @Autowired
    UserLoginRecordMapper userLoginRecordMapper;

    @Autowired
    RedisTemplate redisTemplate;

    private String redisVerificationCodePrefix = "srb:sms:code:";

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void register(UserRegisterInfoVO userRegisterInfoVO) {

        Integer count = userInfoMapper.selectCount(new QueryWrapper<UserInfo>().eq("mobile", userRegisterInfoVO.getMobile()));
        Assert.isTrue(count == 0,ResponseEnum.MOBILE_ERROR.MOBILE_EXIST_ERROR);

        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userRegisterInfoVO,userInfo);
        userInfo.setNickName(userRegisterInfoVO.getMobile());
        userInfo.setName(userRegisterInfoVO.getMobile());
        userInfo.setPassword(MD5.encrypt(userRegisterInfoVO.getPassword()));
        userInfo.setHeadImg("https://yangxoss.oss-cn-shenzhen.aliyuncs.com/1.jfif");
        userInfo.setStatus(UserInfo.STATUS_NORMAL);

        baseMapper.insert(userInfo);

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);
    }

    @Override
    public boolean checkVerificationCode(String verificationCode, String mobile) {
        String redisCode = (String) redisTemplate.opsForValue().get( redisVerificationCodePrefix + mobile);
        Assert.isTrue(verificationCode == redisCode,ResponseEnum.CODE_ERROR);
        return true;
    }

    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {
        Integer userType = loginVO.getUserType();
        String mobile = loginVO.getMobile();
        String password = MD5.encrypt(loginVO.getPassword());

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        String userInfoPassword = userInfo.getPassword();
        Integer userInfoUserType = userInfo.getUserType();
        Integer UserStatus = userInfo.getStatus();
        Long UserId = userInfo.getId();

        Assert.notNull(userInfo ,ResponseEnum.LOGIN_MOBILE_ERROR);
        Assert.equals(password,userInfoPassword,ResponseEnum.LOGIN_PASSWORD_ERROR);
        Assert.equals(userType,userInfoUserType,ResponseEnum.LOGIN_TYPE_ERROR);
        Assert.notEquals(UserStatus, UserInfo.STATUS_LOCKED,ResponseEnum.LOGIN_LOKED_ERROR);

        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(UserId);
        userLoginRecord.setIp(ip);
        userLoginRecordMapper.insert(userLoginRecord);

        String name = userInfo.getName();
        String nickName = userInfo.getNickName();
        String headImg = userInfo.getHeadImg();
        String token = JwtUtils.createToken(UserId, name);

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setToken(token);
        userInfoVO.setHeadImg(headImg);
        userInfoVO.setMobile(mobile);
        userInfoVO.setName(name);
        userInfoVO.setNickName(nickName);
        userInfoVO.setUserType(userType);
        return userInfoVO;
    }

    @Override
    public Page<UserInfo> listPage(Page<UserInfo> pageModel, UserInfoQuery userInfoQuery) {
        String mobile = userInfoQuery.getMobile();
        Integer status = userInfoQuery.getStatus();
        Integer userType = userInfoQuery.getUserType();
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(!StringUtils.isEmpty(mobile),"mobile",mobile)
                .eq(status != null,"status",status)
                .eq(userType != null,"user_type",userType);

        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageModel, queryWrapper);
        return userInfoPage;
    }

    @Override
    public void lockUser(Long id, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        baseMapper.updateById(userInfo);
    }

    @Override
    public Boolean checkMobile(String mobile) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("mobile",mobile);
        Integer count = baseMapper.selectCount(userInfoQueryWrapper);
        return count > 0 ? true : false;
    }


}
