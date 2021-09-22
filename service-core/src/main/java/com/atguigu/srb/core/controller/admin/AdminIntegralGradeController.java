package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@RestController
@RequestMapping("/admin/integralGrade")
@Api(tags = "积分等级管理")
public class AdminIntegralGradeController {

    @Autowired
    IntegralGradeService integralGradeService;

    @GetMapping("/list")
    @ApiOperation("积分等级列表")
    public R list(){
        List<IntegralGrade> list = integralGradeService.list();
        return R.ok().data("list",list);
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation(value = "根据id删除积分等级", notes = "逻辑删除")
    public R removeById(
            @PathVariable("id")
            @ApiParam(value = "数据id", required = true, example = "1")
                              Long id){
        boolean result = integralGradeService.removeById(id);
        if(result){
            return R.ok().message("删除成功");
        }else {
            return R.error().message("删除失败");
        }
    }

    @PostMapping("/save")
    @ApiOperation("新增积分等级")
    public R save(
            @ApiParam(value = "积分等级对象",required = true)
            @RequestBody IntegralGrade integralGrade){
        boolean result = integralGradeService.save(integralGrade);
        if(result){
            return R.ok().message("保存成功");
        }else {
            return R.error().message("保存失败");
        }
    }

    @GetMapping("/get/{id}")
    @ApiOperation("根据id获取积分等级")
    public R getById(
            @PathVariable
            @ApiParam(value = "数据id",required = true,example = "1") Long id){
        IntegralGrade integralGrade = integralGradeService.getById(id);
        if(integralGrade !=null){
            return R.ok().message("查询成功").data("record",integralGrade);
        }else {
            return R.error().message("数据不存在");
        }
    }

    @PutMapping("/update")
    @ApiOperation("更新积分等级")
    public R updateById(
            @ApiParam(value = "积分等级对象",required = true)
            @RequestBody IntegralGrade integralGrade ){
        boolean result = integralGradeService.updateById(integralGrade);
        if(result){
            return R.ok().message("更新成功");
        }else {
            return R.error().message("更新失败");
        }

    }
}

