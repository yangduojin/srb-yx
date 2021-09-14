package com.atguigu;


import com.atguigu.entity.User;
import com.atguigu.mapper.UserMapper;
import com.atguigu.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class myTest {
    //usermapper 自带单表方法
    @Autowired
    private UserMapper userMapper;
    //userservice 自带单表service方法
    @Autowired
    private UserService userService;

    @Test
    public void test1(){
        System.out.println("111");
    }

    @Test
    public void testMapper(){
        List<User> y = userMapper.selectAllByName("y");
        for (User user : y) {
            System.out.println(user);
        }
    }

    @Test
    public void testService(){
//        List<User> list = userService.list();
//        for (User user : list) {
//            System.out.println(user);
//        }
        User user = new User();
        user.setName("yoohooo");
        userService.save(user);
    }
}
