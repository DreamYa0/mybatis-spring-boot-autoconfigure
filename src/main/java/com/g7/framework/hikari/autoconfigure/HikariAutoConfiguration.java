package com.g7.framework.hikari.autoconfigure;

import com.g7.framework.cat.CatAutoConfiguration;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


/**
 * Hikari 连接池的自动配置
 * @author dreamyao
 */
@Configuration
@ConditionalOnClass(HikariDataSource.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties({DataSourceProperties.class, HikariDataSourceProperties.class})
@Import({HikariDataSourceConfiguration.class, CatAutoConfiguration.class})
public class HikariAutoConfiguration {

}