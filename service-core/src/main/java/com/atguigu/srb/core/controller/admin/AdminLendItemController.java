package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.service.LendItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/admin/core/lendItem")
@Api("标的的投资")
public class AdminLendItemController {

    @Resource
    private LendItemService lendItemService;

    @ApiOperation("获取标的投资列表")
    @GetMapping("/list/{lendId}")
    public R list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId){

        List<LendItem> list = lendItemService.listByLendId(lendId);
        return R.ok().data("list",list);
    }

}

