package com.w.serverorder.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("lua")
public class LuaController {

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    @Qualifier("luaLockOrderDel")
    DefaultRedisScript<Boolean> luaLockOrderDel;

    @Autowired
    @Qualifier("luaLockOrderSet")
    DefaultRedisScript<Boolean> luaLockOrderSet;

    @RequestMapping("set")
    public String set() {
        List<String> keys = Arrays.asList("testLua", "hello lua");
        redis.execute(luaLockOrderSet, keys, "100");
        return "success";
    }

    @RequestMapping("del")
    public String del() {
        redis.execute(luaLockOrderDel, Collections.singletonList("testLua"));
        return "success";
    }
}
