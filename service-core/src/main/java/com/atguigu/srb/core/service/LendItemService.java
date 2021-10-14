package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.LendItem;
import com.atguigu.srb.core.pojo.entity.vo.InvestVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface LendItemService extends IService<LendItem> {

    String commitInvest(InvestVO investVO);

    void hfbNotify(Map<String, Object> paramMap);

    List<LendItem> selectByLendId(Long id, Integer status);

    List<LendItem> listByLendId(Long lendId);
}
