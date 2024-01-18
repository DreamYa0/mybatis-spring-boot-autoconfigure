package com.g7.framework.hikari.customizer;

import com.g7.framwork.common.util.extension.SPI;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DruidDataSource2 的回调接口，可以在 DruidDataSource2 初始化之前对其进行定制
 * @author dreamyao
 */
@SPI
public interface HikariDataSourceCustomizer {

    /**
     * 定制化 HikariDataSource
     * @param hikariDataSource druid 数据源
     */
    void customize(HikariDataSource hikariDataSource);

}