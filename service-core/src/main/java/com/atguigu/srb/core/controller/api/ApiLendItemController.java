package com.atguigu.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.atguigu.srb.base.hfb.RequestHelper;
import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.vo.InvestVO;
import com.atguigu.srb.core.service.LendItemService;
import com.atguigu.srb.core.service.UserAccountService;
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
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Api(tags = "标的的投资")
@RestController
@RequestMapping("/api/core/lendItem")
@Slf4j
public class ApiLendItemController {

    @Resource
    private LendItemService lendItemService;

    @Resource
    private UserAccountService userAccountService;


    @ApiOperation("会员投资提交数据")
    @PostMapping("/auth/commitInvest")
    public R commitInvest(@RequestBody InvestVO investVO, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String userName = JwtUtils.getUserName(token);
        investVO.setInvestUserId(userId);
        investVO.setInvestName(userName);
        String formStr = lendItemService.commitInvest(investVO);
        return R.ok().data("formStr",formStr);
    }

    @ApiOperation("会员投资异步回调")
    @PostMapping("/notify")
    public String hfbNotify(HttpServletRequest request){

        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户投资异步回调：" + JSON.toJSONString(paramMap));
        //校验签名 P2pInvestNotifyVo
        if(RequestHelper.isSignEquals(paramMap)) {
            if("0001".equals(paramMap.get("resultCode"))) {
                lendItemService.hfbNotify(paramMap);
            } else {
                log.info("用户投资异步回调失败：" + JSON.toJSONString(paramMap));
                return "fail";
            }
        } else {
            log.info("用户投资异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "fail";
        }
        return "success";
    }

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(
        @ApiParam(value = "标的id", required = true)
        @PathVariable Long lendId) {
        List<LendItem> list = lendItemService.listByLendId(lendId);
        return R.ok().data("list", list);
    }

}

