package com.g7.framework.hikari.autoconfigure;

import com.g7.framework.hikari.autoconfigure.datasource.init.DataSourceInitializerInvoker;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 支持数据源初始化时执行 SQL
 * <p>
 * 在多数据源场景下，#{@link DataSourceAutoConfiguration} 会报 #{@code DataSource} 和
 * #{@code HikariDataSourceInitializer} 的循环依赖，目前的解决办法是排除 #{@link DataSourceAutoConfiguration}，
 * 但是这样一来，Spring Boot 在数据源初始化时执行 SQL 的特性会被移除，此类是为了保留该特性
 * <p>
 * 同时增加了可以在多个数据源执行 SQL 的功能
 * @author dreamyao
 */
@Configuration
@Import(DataSourceInitializerInvoker.class)
@ConditionalOnBean(HikariDataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class HikariDataSourceInitializerAutoConfiguration {

}