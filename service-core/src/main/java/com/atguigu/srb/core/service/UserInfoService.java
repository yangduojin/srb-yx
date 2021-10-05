package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.query.UserInfoQuery;
import com.atguigu.srb.core.pojo.entity.vo.LoginVO;
import com.atguigu.srb.core.pojo.entity.vo.UserInfoVO;
import com.atguigu.srb.core.pojo.entity.vo.UserRegisterInfoVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(UserRegisterInfoVO userRegisterInfoVO);

    boolean checkVerificationCode(String verificationCode, String mobile);

    UserInfoVO login(LoginVO loginVO, String ip);

    Page<UserInfo> listPage(Page<UserInfo> pageModel, UserInfoQuery userInfoQuery);

    void lockUser(Long id, Integer status);

    Boolean checkMobile(String mobile);
}
