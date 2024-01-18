package com.g7.framework.hikari.autoconfigure.datasource;

import com.g7.framework.hikari.autoconfigure.properties.HikariConstants;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Properties;

/**
 * @author dreamyao
 * @title
 * @date 2020-01-01 13:43
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = HikariConstants.HIKARI_DATA_SOURCE_PREFIX)
public abstract class AbstractHikariDataSource2 extends HikariDataSource {

    @Autowired
    private DataSourceProperties dataSourceProperties;
    @Autowired
    private HikariDataSourceProperties hikariDataSourceProperties;

    @PostConstruct
    public void initDruidParentProperties() {
        initDataSourceProperties();
        initHiKariDataSourceProperties();
        initEnhanceDataSourceProperties();
    }

    private void initDataSourceProperties() {

        if (!StringUtils.isEmpty(dataSourceProperties.getDriverClassName())
                && StringUtils.isEmpty(super.getDriverClassName())) {
            super.setDriverClassName(dataSourceProperties.getDriverClassName());
        }

        if (!StringUtils.isEmpty(dataSourceProperties.getUrl()) && StringUtils.isEmpty(super.getJdbcUrl())) {
            super.setJdbcUrl(dataSourceProperties.getUrl());
        }

        if (!StringUtils.isEmpty(dataSourceProperties.getUsername()) && StringUtils.isEmpty(super.getUsername())) {
            super.setUsername(dataSourceProperties.getUsername());
        }

        if (!StringUtils.isEmpty(dataSourceProperties.getPassword()) && StringUtils.isEmpty(super.getPassword())) {
            super.setPassword(dataSourceProperties.getPassword());
        }
    }

    private void initHiKariDataSourceProperties() {

        if (Objects.nonNull(hikariDataSourceProperties.getAutoCommit())) {
            super.setAutoCommit(hikariDataSourceProperties.getAutoCommit());
        }

        if (!StringUtils.isEmpty(hikariDataSourceProperties.getConnectionTestQuery())) {
            super.setConnectionTestQuery(hikariDataSourceProperties.getConnectionTestQuery());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getConnectionTimeout())) {
            super.setConnectionTimeout(hikariDataSourceProperties.getConnectionTimeout());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getIdleTimeout())) {
            super.setIdleTimeout(hikariDataSourceProperties.getIdleTimeout());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getLeakDetectionThreshold())) {
            super.setLeakDetectionThreshold(hikariDataSourceProperties.getLeakDetectionThreshold());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getMaxLifetime())) {
            super.setMaxLifetime(hikariDataSourceProperties.getMaxLifetime());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getMaxPoolSize())) {
            super.setMaximumPoolSize(hikariDataSourceProperties.getMaxPoolSize());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getMinIdle())) {
            super.setMinimumIdle(hikariDataSourceProperties.getMinIdle());
        }

        if (Objects.nonNull(hikariDataSourceProperties.getValidationTimeout())) {
            super.setValidationTimeout(hikariDataSourceProperties.getValidationTimeout());
        }
    }

    private void initEnhanceDataSourceProperties() {

        // MySQL高性能配置
        Properties properties = new Properties();
        properties.put("dataSource.cachePrepStmts", true);
        properties.put("dataSource.prepStmtCacheSize", 350);
        properties.put("dataSource.prepStmtCacheSqlLimit", 2048);
        properties.put("dataSource.useServerPrepStmts", true);
        properties.put("dataSource.useLocalSessionState", true);
        properties.put("dataSource.rewriteBatchedStatements", true);
        properties.put("dataSource.cacheResultSetMetadate", true);
        properties.put("dataSource.cacheServerConfiguration", true);
        properties.put("dataSource.elideSetAutoCommits", true);
        properties.put("dataSource.maintainTimeStats", false);

        super.setDataSourceProperties(properties);
    }

    public void setDataSourceProperties(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    public void setHikariDataSourceProperties(HikariDataSourceProperties hikariDataSourceProperties) {
        this.hikariDataSourceProperties = hikariDataSourceProperties;
    }
}
