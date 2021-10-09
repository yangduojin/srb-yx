package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/api/core/lend")
@Api(tags = "标的前台接口")
public class ApiLendController {


    @Resource
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list")
    public R list() {
        List<Lend> lendList = lendService.selectList();
        return R.ok().data("lendList", lendList);
    }

    @ApiOperation("获取标的详情")
    @GetMapping("/show/{id}")
    public R show(
            @ApiParam(value = "标的id",required = true)
            @PathVariable Long id) {
//        String token = request.getHeader("token");
//        Long userId = JwtUtils.getUserId(token);
        Map<String, Object> lendDetailById = lendService.getLendDetailById(id);

        return R.ok().data("lendDetail", lendDetailById);
    }


    @ApiOperation("获取投资金额的预期收入")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalmonth}/{returnMethod}")
    public R getInterestCount(
            @ApiParam(value = "预期投入的金额",required = true)
            @PathVariable BigDecimal invest,
            @ApiParam(value = "年化率",required = true)
            @PathVariable BigDecimal yearRate,
            @ApiParam(value = "投资期数",required = true)
            @PathVariable Integer totalmonth,
            @ApiParam(value = "还款方式",required = true)
            @PathVariable Integer returnMethod) {

        BigDecimal interestCount = lendService.getInterestCount(invest,yearRate,totalmonth,returnMethod);

        return R.ok().data("interestCount", interestCount);
    }

}

