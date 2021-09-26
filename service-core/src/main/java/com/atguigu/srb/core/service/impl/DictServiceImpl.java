package com.atguigu.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.core.listener.ExcelDictDTOListener;
import com.atguigu.srb.core.mapper.DictMapper;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.pojo.entity.ExcelDictDTO;
import com.atguigu.srb.core.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


//    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void importData(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(baseMapper)).sheet().doRead();
        log.info("importData finished");
    }

    @Override
    public List<ExcelDictDTO> listDictData() {
        List<Dict> dicts = baseMapper.selectList(null);
        List<ExcelDictDTO> list = dicts.stream().map(dict -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(dict,excelDictDTO);
            return excelDictDTO;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<Dict> listByParentId(Long parentId) {
        QueryWrapper<Dict> queryByParentId = new QueryWrapper<Dict>().eq("parent_id", parentId);
        List<Dict> dicts = baseMapper.selectList(queryByParentId);
        dicts.forEach(dict -> {
            Boolean hasChildren = hasChildren(dict.getId());
            dict.setHasChildren(hasChildren);
        });
        return dicts;
    }

    private Boolean hasChildren(Long parentId){
        QueryWrapper<Dict> queryParentId = new QueryWrapper<Dict>().eq("parent_id", parentId);
        Integer hasChildrenCount = baseMapper.selectCount(queryParentId);
        return hasChildrenCount > 0 ? true : false ;
    }
}
