package com.g7.framework.dynamic.loadbalance;


import com.g7.framwork.common.util.extension.SPI;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @date 2018/12/17 8:29 PM
 * @since 1.0.0
 */
@SPI(value = RandomLoadBalance.NAME)
public interface LoadBalance {

    /**
     * 数据源选择 K 数据源名称 V 数据源
     * @param dataSources 数据源集合
     * @return 目标数据源名称
     */
    String select(Map<String, DataSource> dataSources);
}
