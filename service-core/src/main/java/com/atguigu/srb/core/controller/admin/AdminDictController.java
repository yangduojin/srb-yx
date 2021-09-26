package com.atguigu.srb.core.controller.admin;


import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.pojo.entity.ExcelDictDTO;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@CrossOrigin
@RestController
@RequestMapping("/admin/core/dict")
@Api(tags = "数据字典管理")
public class AdminDictController {

    @Autowired
    private DictService dictService;


    @ApiOperation("上传excel")
    @PostMapping("import")
    public R batchImport(
            @ApiParam(value = "上传的excel",required = true)
            @RequestParam("file")MultipartFile multipartFile){
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
            dictService.importData(inputStream);
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }
        return R.ok().message("upload success");
    }

    @SneakyThrows
    @ApiOperation("下载数据字典excel")
    @GetMapping("/export")
    public void export(HttpServletResponse response){
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.listDictData());
    }

    @ApiOperation("根据id查询数据字典集合")
    @GetMapping("/list/{parentId}")
    public R listByParentId(@PathVariable("parentId") Long parentId){
        List<Dict> list = dictService.listByParentId(parentId);
        return R.ok().data("list",list);
    }

}

