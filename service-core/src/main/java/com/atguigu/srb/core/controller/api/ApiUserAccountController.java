package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/api/core/userAccount")
@Api(tags = "会员账户")
@Slf4j
public class ApiUserAccountController {

    @Resource
    private UserAccountService userAccountService;


    @ApiOperation("充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public R commitCharge(
            @ApiParam(value = "充值金额", required = true)
            @PathVariable BigDecimal chargeAmt, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userAccountService.commitCharge(chargeAmt,userId);

        return R.ok().data("formStr",formStr);
    }


    @ApiOperation("获取会员账户余额")
    @GetMapping("/auth/getAccount")
    public R getAccount(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal accountAmount = userAccountService.getAccountAmount(userId);

        return R.ok().data("account",accountAmount);
    }


    @ApiOperation("充值")
    @PostMapping("/notify")
    public String hfbNotify(HttpServletRequest request){
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));

        if(RequestHelper.isSignEquals(paramMap)){
            if("0001".equals(paramMap.get("resultCode"))){
                log.info("验签成功，开始充值");
                userAccountService.hfbNotify(paramMap);
                return "success" ;
            }else {
                log.info("用户充值异步回调充值失败：" + JSON.toJSONString(paramMap));
                return "success";
            }
        }else {
            log.info("用户充值异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
    }

    @ApiOperation("提现")
    @PostMapping("/auth/commitWithdraw/{fetchAmt}")
    public R commitWithdraw(@PathVariable BigDecimal fetchAmt,HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = userAccountService.commitWithdraw(fetchAmt,userId);
        return R.ok().data("formStr",formStr);
    }

    @ApiOperation("提现消息回调")
    @PostMapping("/notifyWithdraw")
    public String notifyWithdraw(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = RequestHelper.switchMap(parameterMap);
        if(RequestHelper.isSignEquals(paramMap)){
            if("0001".equals(paramMap.get("resultCode"))){
               userAccountService.notifyWithdraw(paramMap);
            }else {
            log.info("提现异步回调充值失败：" + JSON.toJSONString(paramMap));
            return "fail";
            }
        }else {
            log.info("提现异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        return "success";
    }

}

