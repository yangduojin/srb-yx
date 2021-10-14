package com.atguigu.srb.core.service.impl;

import com.atguigu.srb.core.mapper.LendItemMapper;
import com.atguigu.srb.core.mapper.LendItemReturnMapper;
import com.atguigu.srb.core.mapper.LendMapper;
import com.atguigu.srb.core.mapper.LendReturnMapper;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.LendItemReturn;
import com.atguigu.srb.core.pojo.entity.LendReturn;
import com.atguigu.srb.core.service.LendItemReturnService;
import com.atguigu.srb.core.service.UserBindService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借回款记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
public class LendItemReturnServiceImpl extends ServiceImpl<LendItemReturnMapper, LendItemReturn> implements LendItemReturnService {

    @Resource
    private LendMapper lendMapper;

    @Resource
    private LendReturnMapper lendReturnMapper;

    @Resource
    private LendItemMapper lendItemMapper;

    @Resource
    private UserBindService userBindService;


    @Override
    public List<LendItemReturn> selectByLendId(Long lendId, Long userId) {
        QueryWrapper<LendItemReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_id",lendId)
        .eq("invest_user_id", userId)
        .orderByAsc("current_period");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<Map<String, Object>> addReturnDetail(Long lendReturnId) {
        LendReturn lendReturn = lendReturnMapper.selectById(lendReturnId);
        Lend lend = lendMapper.selectById(lendReturn.getLendId());



        List<LendItemReturn> lendItemReturnList = this.selectLendItemReturnList(lendReturnId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (LendItemReturn lendItemReturn : lendItemReturnList) {
            HashMap<String, Object> hashMap = new HashMap<>();
            LendItem lendItem = lendItemMapper.selectById(lendItemReturn.getLendItemId());
            String bindCode = userBindService.getBindCodeByUserId(lendItem.getInvestUserId());

            hashMap.put("agentProjectCode",lend.getLendNo());
            hashMap.put("voteBillNo",lendItem.getLendItemNo());
            hashMap.put("toBindCode",bindCode);
            hashMap.put("transitAmt",lendItemReturn.getTotal());
            hashMap.put("baseAmt",lendItemReturn.getPrincipal());
            hashMap.put("benifitAmt",lendItemReturn.getInterest());
            hashMap.put("feeAmt",lendItemReturn.getFee());
            list.add(hashMap);
        }
        return list;
    }

    @Override
    public List<LendItemReturn> selectLendItemReturnList(Long lendReturnId) {
        QueryWrapper<LendItemReturn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("lend_return_id",lendReturnId);
        return baseMapper.selectList(queryWrapper);
    }
}
