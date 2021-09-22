package com.atguigu.srb.core.controller;


import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.service.IntegralGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api/integralGrade")
public class IntegralGradeController {

    @Autowired
    IntegralGradeService integralGradeService;

    @GetMapping("/list")
    public List<IntegralGrade> list(){
        return integralGradeService.list();
    }
}

