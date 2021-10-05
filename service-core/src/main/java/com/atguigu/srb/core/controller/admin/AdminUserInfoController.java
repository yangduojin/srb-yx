package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.query.UserInfoQuery;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/admin/core/userInfo")
//@CrossOrigin
@Api(tags = "后台会员信息管理接口")
public class AdminUserInfoController {

    @Autowired
    UserInfoService userInfoService;
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
    @ApiOperation("保存用户注册信息")
    @GetMapping("/list/{page}/{limit}")
    public R UserRegister(
            @ApiParam(value = "当前页码",required = true)
            @PathVariable Long page,
            @ApiParam(value = "每页记录数",required = true)
            @PathVariable Long limit,
            @ApiParam(value = "信息查询表单",required = false)
                    UserInfoQuery userInfoQuery
            ){
        Page<UserInfo> pageModel  = new Page<>(page,limit);
        pageModel =  userInfoService.listPage(pageModel,userInfoQuery);

        return R.ok().data("pageModel", pageModel);
    }

    @ApiOperation("根据id锁定用户")
    @PutMapping("/lock/{id}/{status}")
    public R lock(
            @ApiParam(value = "用户id",required = true)
            @PathVariable Long id,
            @ApiParam(value = "锁定状态（0：锁定 1：解锁）",required = true)
            @PathVariable Integer status
            ){
        Assert.isTrue(id != null && status != null, ResponseEnum.USER_ID_OR_STATUS_ERROR);
        userInfoService.lockUser(id,status);
        return R.ok().message(status==1?"解锁成功":"锁定成功");
    }


}
