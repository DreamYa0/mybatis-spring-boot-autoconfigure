package com.g7.framework.hikari.autoconfigure.datasource;

/**
 * @author dreamyao
 * @title HikariCP 数据源，会继承 AbstractHikariDataSource2 的配置，
 * 并注入 'spring.datasource.hikaricp.data-source.${name}' 的配置
 * @date 2020-01-01 14:02
 * @since 1.0.0
 */
public class HikariDataSource2 extends AbstractHikariDataSource2 {
}
