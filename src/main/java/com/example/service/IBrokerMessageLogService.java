package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.BrokerMessageLog;

import java.util.List;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
public interface IBrokerMessageLogService extends IService<BrokerMessageLog> {
    /**
     * 投递超时记录
     *
     * @return
     */
    List<BrokerMessageLog> findTimeout();
}
