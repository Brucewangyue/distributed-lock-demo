package com.w.serverorder.entity;

import lombok.Data;

@Data
public class OrderLock {
    private int orderId;
    private int driverId;
}
