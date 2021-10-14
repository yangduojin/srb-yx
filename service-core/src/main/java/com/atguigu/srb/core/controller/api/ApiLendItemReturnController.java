package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.base.util.JwtUtils;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.atguigu.srb.core.service.LendItemReturnService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 标的出借回款记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/api/core/lendItemReturn")
public class ApiLendItemReturnController {

    @Resource
    private LendItemReturnService lendItemReturnService;
    @ApiOperation("获取列表")

    @GetMapping("/auth/list/{lendId}")
    public R list(
@ApiParam(value = "标的id", required = true)
        @PathVariable Long lendId, HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        List<LendItemReturn> list = lendItemReturnService.selectByLendId(lendId, userId);
        return R.ok().data("list", list);
    }


}

