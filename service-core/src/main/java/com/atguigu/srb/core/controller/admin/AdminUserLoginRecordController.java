package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
import com.atguigu.srb.core.service.UserLoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户登录记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */

//@CrossOrigin
@RestController
@RequestMapping("/admin/core/userLoginRecord")
@Api(tags = "后台会员登录信息管理接口")
public class AdminUserLoginRecordController {


    @Autowired
    UserLoginRecordService userLoginRecordService;

    @ApiOperation("根据id获取会员登录信息")
    @GetMapping("/getuserLoginRecordTop50/{id}")
    public R getuserLoginRecordTop50(
            @ApiParam(value = "用户id",required = true)
            @PathVariable Long id){
        Assert.isTrue(id != null, ResponseEnum.WEIXIN_FETCH_USERINFO_ERROR);
        List<UserLoginRecord> list =  userLoginRecordService.getuserLoginRecordTop50(id);
        return R.ok().message("查询成功").data("loginRecordList",list);
    }
}

