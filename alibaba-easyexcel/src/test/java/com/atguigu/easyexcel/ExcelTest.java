package com.atguigu.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.atguigu.easyexcel.dto.ExcelStudentDTO;
import com.atguigu.easyexcel.listener.ExcelStudentDTOListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelTest {
    @Test
    public void simpleWriteXlsx() {
        String fileName = "d:/excel/simpleWrite.xlsx"; //需要提前新建目录
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        EasyExcel.write(fileName, ExcelStudentDTO.class).sheet("first").doWrite(data());
    }

    @Test
    public void simpleWriteXls() {
        String fileName = "d:/excel/simpleWrite.xls"; //需要提前新建目录
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        EasyExcel.write(fileName, ExcelStudentDTO.class).excelType(ExcelTypeEnum.XLS).sheet("first").doWrite(data());
    }

    private List<ExcelStudentDTO> data(){
        List<ExcelStudentDTO> list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ExcelStudentDTO data = new ExcelStudentDTO();
            data.setName("yxxx22"+i);
            data.setBirthday(new Date());
            data.setSalary(111.00);
            list.add(data);
        }
        return list;
    }

    @Test
    public void simpleReadXlsx(){
        String fileName = "d:/excel/simpleWrite.xlsx";
        EasyExcel.read(fileName,ExcelStudentDTO.class,new ExcelStudentDTOListener()).sheet().doRead();
    }
    
    @Test
    public void simpleReadXls2(){
        String fileName = "d:/excel/simpleWrite.xls";
        EasyExcel.read(fileName,ExcelStudentDTO.class,new ExcelStudentDTOListener()).excelType(ExcelTypeEnum.XLS).sheet().doRead();
    }

}
