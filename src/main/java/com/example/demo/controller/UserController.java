package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final String URL_PRE = "";

    @RequestMapping("/login")
    public Object login(String username,String password) throws Exception {

        HashMap<Object, Object> map = new HashMap<>();
        map.put("token","123");
        return map;
    }

}
