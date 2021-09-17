package com.atguigu.controller;

import com.atguigu.entity.User;
import com.atguigu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/yx")
@CrossOrigin
public class ControllerDemo {

    @Autowired
    UserService userService;

    @GetMapping("/list")
    public List<User> userList(HttpServletRequest request){
        String tokens = request.getHeader("token");
        System.out.println(tokens);
        return userService.list();
    }

}
