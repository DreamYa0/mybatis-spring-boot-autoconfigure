package com.g7.framework.hikari.autoconfigure.properties;

/**
 * Druid 常量池
 * @author dreamyao
 * @date 2018/7/30
 */
public class HikariConstants {

    public static final String MASTER_DATASOURCE_NAME = "master";
    public static final String SLAVE_DATASOURCE_NAME = "slave";
    public static final String HIKARI_DATA_SOURCE_PREFIX = "spring.datasource.hikari";
    private static final String DELIMITER = ".";
    private static final String DATA_SOURCES = "data-sources";
    public static final String DATA_SOURCE_JDBC_URL_PREFIX = HIKARI_DATA_SOURCE_PREFIX + DELIMITER + DATA_SOURCES;
    public static final String BEAN_NAME = "dataSource";
    public static final String BEAN_SUFFIX = "DataSource";
    public static final String DATA_SOURCE_HIKARI_URL_NAME = "jdbc-url";
}