package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.TransFlow;
import com.atguigu.srb.core.pojo.entity.bo.TransFlowBO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface TransFlowService extends IService<TransFlow> {

    void saveTransFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);

    List<TransFlow> transFlowList(Long userId);
}
