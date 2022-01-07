package com.w.serverorder.dao;

import com.w.serverorder.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderDao {
    int insert(Order order);

    Order selectByOrderId(int orderId);

    int update(Order entity);
}
