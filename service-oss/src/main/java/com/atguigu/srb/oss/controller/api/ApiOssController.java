package com.atguigu.srb.oss.controller.api;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.oss.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
//@CrossOrigin
@RequestMapping("/api/oss/file")
@Api(tags = "文件上传总接口")
public class ApiOssController {

    @Autowired
    OssService ossService;

    @SneakyThrows
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public R upload(
            @ApiParam(value = "上传文件",required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "模块名称",required = true)
            @RequestParam("moduleName") String moduleName){
        InputStream inputStream = file.getInputStream();
        String OriginalFilename = file.getOriginalFilename();
        String uploadPath = ossService.upload(OriginalFilename, moduleName, inputStream);
        return R.ok().message("文件上传成功").data("uploadPath",uploadPath);
    }

    @ApiOperation("删除文件")
    @DeleteMapping("/del")
    public R deleteFile(
            @ApiParam(value = "文件名",required = true)
            @RequestParam(value = "fileName",required = true) String fileName){
        try {
            ossService.delFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error().message("文件删除失败");
        }
        return R.ok().message("文件删除成功");
    }
}
