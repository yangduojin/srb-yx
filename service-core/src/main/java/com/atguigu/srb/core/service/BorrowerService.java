package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
public interface BorrowerService extends IService<Borrower> {

    void saveBorrowerInfo(Long userId, BorrowerVO borrowerVO);

    Integer getBorrowerStatus(Long userId);

    Page<Borrower> borrowerList(Page<Borrower> pageModel, String keyword);

    BorrowerDetailVO getBorrowerDetailInfo(Integer id);

    void approval(BorrowerApprovalVO borrowerApprovalVO);
}
