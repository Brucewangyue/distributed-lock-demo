package com.w.serverorder.service;

import com.w.serverorder.dao.OrderDao;
import com.w.serverorder.entity.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OrderServer {

    @Resource
    OrderDao dao;

    public boolean grapOrder(int orderId, int driverId) {
        Order order = dao.selectByOrderId(orderId);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (order.getBelongDriverId() == null) {
            order.setBelongDriverId(driverId);

            return dao.update(order) > 0;
        }

        return false;
    }
}
