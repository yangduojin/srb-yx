package com.atguigu;


import com.atguigu.entity.Product;
import com.atguigu.entity.User;
import com.atguigu.mapper.ProductMapper;
import com.atguigu.mapper.UserMapper;
import com.atguigu.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Autowired
    private ProductMapper productMapper;

    //userservice 自带单表service方法
    @Autowired
    private UserService userService;

    @Test
    public void test1(){
        System.out.println("111");
    }

    @Test
    public void testMapper(){

//        List<User> y = userMapper.selectAllByName("y");
//        for (User user : y) {
//            System.out.println(user);
//        }

        //page
//        IPage<User> iPage = new Page<>(3,3);
//        userMapper.selectPage(iPage,null);
//        System.out.println(iPage);

        // myPage
        IPage<User> iPage = new Page<>(3,3);
        IPage<User> userIPage = userMapper.selectPageByPage(iPage, 10);
        System.out.println(userIPage);

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
    
    @Test
    public void testLogic(){
        userMapper.deleteById(8l);
    }
    
    @Test
    public void testProductMapperOptimisticLocker(){
        Product p1 = productMapper.selectById(1l);
        Product p2 = productMapper.selectById(1l);

        p1.setPrice(p1.getPrice() + 50);
        int result1 = productMapper.updateById(p1);
        System.out.println("p1 change price is  " + result1);

        p2.setPrice(p2.getPrice() + 50);
        int result2 = productMapper.updateById(p2);
        System.out.println("p2 change price is   " + result2);

        if (result2==0) {
            System.out.println("retry p2 change price ");
            Product product = productMapper.selectById(1l);
            product.setPrice(product.getPrice() + 50);
            productMapper.updateById(product);

        }

        Product p3 = productMapper.selectById(1l);
        System.out.println("finally result is " + p3);
    }
    
    @Test
    public void testWrapper(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.notLike("name","y")
                .notBetween("age",10,20)
                .isNotNull("email");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(user -> System.out.println(user));
    }

    @Test
    public void testWrapper2(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.notLike("name","i")
                    .and(i -> i.lt("age",25).or().isNotNull("email")).orderByDesc("id");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(user -> System.out.println(user));
    }
    
    @Test
    public void testWrapper3(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("name","age");
        List<User> users = userMapper.selectList(queryWrapper);
        users.forEach(System.out::println);
    }
    
    @Test
    public void testQueryWrapper(){
        String name = "y";
        Integer ageBegin = 10;
        Integer ageEnd  = 30;
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.like(name!=null&&name!="","name",name)
                .ge(ageBegin!=null,"age",ageBegin)
                .le(ageEnd!=null,"age",ageEnd);
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(System.out::println);

    }

    @Test
    public void testUpdateWrapper(){
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.set("email","yx@gmail.com")
                .like("name","yoohooo")
                .gt("age",5);
        User user = new User();
        int update = userMapper.update(user, wrapper);
        System.out.println(update);
    }



}
