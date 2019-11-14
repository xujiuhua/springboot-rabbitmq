package com.example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.BrokerMessageLog;

import java.util.List;

/**
 * <p></p>
 *
 * @author jiuhua.xu
 * @version 1.0
 * @since JDK 1.8
 */
public interface BrokerMessageLogDao extends BaseMapper<BrokerMessageLog> {

    List<BrokerMessageLog> findTimeout();
}
