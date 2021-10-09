package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/admin/core/borrower")
@Api(tags = "借款人管理")
public class AdminBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R list(
            @ApiParam(value = "当前页数",required = true)
            @PathVariable Integer page,
            @ApiParam(value = "每页显示多少条数据",required = true)
            @PathVariable Integer limit,
            @ApiParam(value = "关键字查询")
            @RequestParam("keyword") String keyword){
        Page<Borrower> pageModel = new Page<>(page,limit);
        pageModel = borrowerService.borrowerList(pageModel , keyword);
        return R.ok().data("pageModel",pageModel);
    }

    @ApiOperation("获取借款人信息")
    @GetMapping("/show/{id}")
    public R show(
            @ApiParam(value = "借款人id",required = true)
            @PathVariable Long id){
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);
        return R.ok().data("borrowerDetailVO",borrowerDetailVO);
    }


    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO){
        borrowerService.approval(borrowerApprovalVO);
        return R.ok().message("审批完成");
    }



}

