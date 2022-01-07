package com.w.serverorder.controller;

import com.w.serverorder.lock.EtcdLock;
import com.w.serverorder.service.impl.EtcdLockTestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("lockTest")
public class LockTestController {

//    @Resource
//    EtcdLockTestService etcdLockTestService;
//
//    @GetMapping("incr")
//    public String incr() {
//        etcdLockTestService.incr();
//        return "success";
//    }
//
//    @GetMapping("getCount")
//    public int getCount() {
//        return etcdLockTestService.getCount();
//    }
}
