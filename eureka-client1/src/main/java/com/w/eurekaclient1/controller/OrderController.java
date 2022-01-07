package com.w.eurekaclient1.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    RestTemplate restTemplate;

    @PostMapping("grabOrder")
    public String grabOrder(@RequestParam("orderId") int orderId,@RequestParam("driverId") int driverId){

//        Map<String,Object> body = new HashMap<>();
//        body.put("orderId",orderId);
//        body.put("driverId",driverId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> map= new LinkedMultiValueMap<>();
        map.add("orderId",orderId);
        map.add("driverId", driverId);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);


        ResponseEntity<String> forEntity = restTemplate.postForEntity("http://server-order/order/grabOrder",request,String.class);

        return forEntity.getBody();
    }
}
