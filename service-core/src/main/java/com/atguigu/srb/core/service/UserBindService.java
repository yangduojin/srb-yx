package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserBind;
import com.atguigu.srb.core.pojo.entity.vo.UserBindVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface UserBindService extends IService<UserBind> {

    String commitBindUser(Long userId, UserBindVO userBindVO);
}
