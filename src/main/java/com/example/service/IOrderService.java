package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Order;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
public interface IOrderService extends IService<Order> {

    /**
     * 创建订单
     *
     * @param order
     */
    void createOrder(Order order);

}
