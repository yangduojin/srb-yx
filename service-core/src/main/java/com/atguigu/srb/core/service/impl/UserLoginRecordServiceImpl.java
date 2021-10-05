package com.atguigu.srb.core.service.impl;

        import com.atguigu.srb.core.mapper.UserLoginRecordMapper;
        import com.atguigu.srb.core.pojo.entity.UserLoginRecord;
        import com.atguigu.srb.core.service.UserLoginRecordService;
        import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
        import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.stereotype.Service;

        import java.util.List;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author Helen
 * @since 2021-09-22
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

    @Autowired
    UserLoginRecordMapper userLoginRecordMapper;
    @Override
    public List<UserLoginRecord> getuserLoginRecordTop50(Long id) {
        QueryWrapper<UserLoginRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",id).orderByDesc("id").last("limit 50");
        List<UserLoginRecord> userLoginRecords = baseMapper.selectList(queryWrapper);
        return userLoginRecords;
    }
}
