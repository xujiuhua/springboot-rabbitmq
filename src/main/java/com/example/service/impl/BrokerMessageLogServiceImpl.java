package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.BrokerMessageLogDao;
import com.example.dao.OrderDao;
import com.example.entity.BrokerMessageLog;
import com.example.entity.Order;
import com.example.service.IBrokerMessageLogService;
import com.example.service.IOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
@Service
public class BrokerMessageLogServiceImpl extends ServiceImpl<BrokerMessageLogDao, BrokerMessageLog> implements IBrokerMessageLogService {
    @Override
    public List<BrokerMessageLog> findTimeout() {
        return baseMapper.findTimeout();
    }
}
