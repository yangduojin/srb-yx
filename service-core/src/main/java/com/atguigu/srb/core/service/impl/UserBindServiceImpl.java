package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.base.hfb.FormHelper;
import com.atguigu.srb.base.hfb.HfbConst;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.enums.UserBindEnum;
import com.atguigu.srb.core.mapper.UserBindMapper;
import com.atguigu.srb.core.mapper.UserInfoMapper;
import com.atguigu.srb.core.pojo.entity.UserBind;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.vo.UserBindVO;
import com.atguigu.srb.core.service.UserBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public String commitBindUser(Long userId, UserBindVO userBindVO) {

        QueryWrapper<UserBind> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("id_card",userBindVO.getIdCard())
                .ne("user_id",userId);
        UserBind userBind = baseMapper.selectOne(queryWrapper);
        Assert.isNull(userBind, ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);


        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        userBind = baseMapper.selectOne(queryWrapper);


        if(userBind == null){
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVO,userBind);
            userBind.setUserId(userId);
            userBind.setStatus(UserBindEnum.NO_BIND.getStatus());
            baseMapper.insert(userBind);
        } else {
            BeanUtils.copyProperties(userBindVO,userBind);
            baseMapper.updateById(userBind);
        }


        Map<String, Object> userBindMap = new HashMap<>();
        userBindMap.put("agentId",HfbConst.AGENT_ID);
        userBindMap.put("agentUserId",userId);
        userBindMap.put("idCard",userBindVO.getIdCard());
        userBindMap.put("personalName", userBindVO.getName());
        userBindMap.put("bankType", userBindVO.getBankType());
        userBindMap.put("bankNo", userBindVO.getBankNo());
        userBindMap.put("mobile", userBindVO.getMobile());
        userBindMap.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        userBindMap.put("notifyUrl",HfbConst.USERBIND_NOTIFY_URL);
        userBindMap.put("timestamp", RequestHelper.getTimestamp());
        userBindMap.put("sign", RequestHelper.getSign(userBindMap));

        String formStr = FormHelper.buildForm(HfbConst.USERBIND_URL, userBindMap);

        return formStr;
    }

    @Override
    public void hfbNotify(Map<String, Object> paramMap) {
        String bindCode = (String) paramMap.get("bindCode");
        String agentUserId = (String) paramMap.get("agentUserId");
        QueryWrapper<UserBind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",agentUserId);
        UserBind userBind = baseMapper.selectOne(queryWrapper);
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        baseMapper.updateById(userBind);


        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setName(userBind.getName());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfo.setBindCode(bindCode);
        userInfoMapper.updateById(userInfo);

    }

    @Override
    public String getBindCodeByUserId(Long userId) {
        QueryWrapper<UserBind> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        UserBind userBind = baseMapper.selectOne(queryWrapper);

        return userBind.getBindCode();
    }
}
