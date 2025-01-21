package com.taskmanagement.controller.user;

import com.taskmanagement.entity.User;
import com.taskmanagement.service.UserService;
import com.taskmanagement.vo.UserVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserLoginDTO user) {
        log.info("用户注册：{}", user);
        User registeredUser = userService.register(user);
        return ResponseEntity.ok(registeredUser);
    }
}
