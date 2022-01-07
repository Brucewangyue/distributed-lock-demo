package com.w.serverorder.dao;

import com.w.serverorder.entity.OrderLock;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderLockDao {
    boolean insert(OrderLock entity);

    /**
     * 需要解锁
     * @param orderId
     * @return
     */
    int delete(int orderId);
}
