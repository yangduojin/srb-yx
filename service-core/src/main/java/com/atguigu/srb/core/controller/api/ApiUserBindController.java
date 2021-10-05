package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.vo.UserBindVO;
import com.atguigu.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("api/core/userBind")
@Api(tags = "会员账号绑定")
@Slf4j
public class ApiUserBindController {

    @Resource
    private UserBindService userBindService;

    @ApiOperation("账户绑定提交数据")
    @PostMapping("/auth/bind")
    public R bind(
            @ApiParam(value = "会员绑定信息表单",required = true)
            @RequestBody UserBindVO userBindVO,
            HttpServletRequest request){
        Assert.notNull(userBindVO, ResponseEnum.USER_BIND_INFO_ERROR);
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userBindService.commitBindUser(userId,userBindVO);
        return R.ok().data("formStr",formStr);
    }

    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("账户绑定异步回调接受的参数如下 :  " + paramMap);

        return  "success";
    }
}

