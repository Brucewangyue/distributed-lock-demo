package com.w.serverorder.service;
public interface OrderLockService {
    boolean grabOrder(int orderId,Integer driverId);
}
