package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.UserInfoVO;
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

    void register(UserInfoVO userInfoVO);
}
