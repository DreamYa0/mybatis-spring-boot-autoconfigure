package com.g7.framework.dynamic.datasource.util;

import com.g7.framework.dynamic.loadbalance.LoadBalance;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import com.g7.framwork.common.util.extension.ExtensionLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.g7.framework.hikari.autoconfigure.properties.HikariConstants.MASTER_DATASOURCE_NAME;
import static com.g7.framework.hikari.autoconfigure.properties.HikariConstants.SLAVE_DATASOURCE_NAME;

/**
 * @author dreamyao
 * @title
 * @date 2018/12/20 6:34 PM
 * @since 1.0.0
 */
public abstract class DataSourceUtils {

    private static final String DEFAULT_LOAD_BALANCE_NAME = "random";
    private static final ConcurrentMap<String, LoadBalance> loadBalanceConcurrentMap = new ConcurrentHashMap<>();

    /**
     * 根据负载均衡策略获取数据源集合中的某个数据源
     * @param dataSources 数据源集合
     * @return 某个数据源
     */
    public static String loadBalanceDataSourceName(Map<String, DataSource> dataSources,
                                                   HikariDataSourceProperties hikariProperties) {

        // 负载均衡策略
        String loadBalanceName = hikariProperties.getLoadBalance();

        LoadBalance loadBalance;
        if (StringUtils.isEmpty(loadBalanceName)) {
            if (loadBalanceConcurrentMap.containsKey(DEFAULT_LOAD_BALANCE_NAME)) {
                loadBalance = loadBalanceConcurrentMap.get(DEFAULT_LOAD_BALANCE_NAME);
            } else {
                // 如果无负载均衡策略配置，则采用默认负载均衡策略
                loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getDefaultExtension();
                loadBalanceConcurrentMap.putIfAbsent(DEFAULT_LOAD_BALANCE_NAME, loadBalance);
            }
        } else {
            if (loadBalanceConcurrentMap.containsKey(loadBalanceName)) {
                loadBalance = loadBalanceConcurrentMap.get(loadBalanceName);
            } else {
                // 根据负载均衡扩展点名称获取对应负载均衡策略
                loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceName);
                loadBalanceConcurrentMap.putIfAbsent(loadBalanceName, loadBalance);
            }
        }

        return loadBalance.select(dataSources);
    }

    public static void classifyDataSources(Map<String, DataSource> sourceDataSources,
                                           Map<String, DataSource> masterTargetDataSources,
                                           Map<String, DataSource> slaveTargetDataSources) {

        sourceDataSources.forEach((dataSourceName,dataSource) -> {
            if (dataSourceName.toLowerCase().contains(MASTER_DATASOURCE_NAME)) {
                masterTargetDataSources.put(dataSourceName, dataSource);
            } else if (dataSourceName.toLowerCase().contains(SLAVE_DATASOURCE_NAME)) {
                slaveTargetDataSources.put(dataSourceName, dataSource);
            }
        });
    }

    public static String loadBalanceDataSourceName(ApplicationContext applicationContext,
                                                                   HikariDataSourceProperties hikariProperties) {
        // 所有数据源
        Map<String, DataSource> sourceDataSources = new HashMap<>(
                applicationContext.getBeansOfType(DataSource.class));
        // 主库数据源集合中至少存在一个数据源实列
        Map<String, DataSource> masterTargetDataSources = new HashMap<>(6);
        sourceDataSources.forEach((dataSourceName, dataSource) -> {
            if (dataSourceName.toLowerCase().contains(MASTER_DATASOURCE_NAME)) {
                masterTargetDataSources.put(dataSourceName, dataSource);
            }
        });
        return loadBalanceDataSourceName(masterTargetDataSources, hikariProperties);
    }
}
