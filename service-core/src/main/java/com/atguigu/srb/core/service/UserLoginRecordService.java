package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface UserLoginRecordService extends IService<UserLoginRecord> {

    List<UserLoginRecord> getuserLoginRecordTop50(Long id);

}
