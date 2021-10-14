package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.atguigu.srb.core.service.LendReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 还款记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/api/core/lendReturn")
@Api(tags = "还款计划")
@Slf4j
public class ApiLendReturnController {

    @Resource
    private LendReturnService lendReturnService;

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(
        @ApiParam(value = "标的id", required = true)
        @PathVariable Long lendId) {
        List<LendReturn> list = lendReturnService.selectByLendId(lendId);
        return R.ok().data("list", list);
    }


    @ApiOperation("用户还款")
    @PostMapping("/auth/commitReturn/{lendReturnId}")
    public R commitReturn(
            @ApiParam(value = "还款计划id", required = true)
        @PathVariable Long lendReturnId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = lendReturnService.commitReturn(lendReturnId,userId);
        return R.ok().data("formStr", formStr);
    }


    @ApiOperation("用户还款HFB异步通知返回接口")
    @PostMapping("/notifyUrl")
    public String notifyUrl(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = RequestHelper.switchMap(parameterMap);
        if(RequestHelper.isSignEquals(map)){
            if("0001".equals(map.get("resultCode"))){
                String result = lendReturnService.notifyUrl(map);
                return result;
            }else {
                log.info("还款异步回调失败：" + JSON.toJSONString(map));
                return "fail";
            }
        }else {
            log.info("还款异步回调签名错误：" + JSON.toJSONString(map));
            return "fail";
        }
    }



}

