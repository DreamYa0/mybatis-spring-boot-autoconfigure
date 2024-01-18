package com.g7.framework.hikari.autoconfigure.datasource.init;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceSchemaCreatedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Bean通过在DataSourceSchemaCreatedEvent上的InitializingBean.afterPropertiesSet()
 * 和data  -  * .sql SQL脚本上运行schema  -  * .sql来处理DataSource初始化。
 * @author dreamyao
 * @see org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 */
public class DataSourceInitializerInvoker implements ApplicationListener<DataSourceSchemaCreatedEvent>,
        InitializingBean {

    private static final Log logger = LogFactory.getLog(DataSourceInitializerInvoker.class);

    private final List<HikariDataSource> dataSources;

    private final DataSourceProperties properties;

    private final ApplicationContext applicationContext;

    private DataSourceInitializer dataSourceInitializer;

    private boolean initialized;

    public DataSourceInitializerInvoker(ObjectProvider<List<HikariDataSource>> dataSourcesProvider,
                                        DataSourceProperties properties, ApplicationContext applicationContext) {
        this.dataSources = dataSourcesProvider.getIfAvailable(ArrayList::new);
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        DataSourceInitializer initializer = getDataSourceInitializer();
        if (initializer != null) {
            boolean schemaCreated = this.dataSourceInitializer.createSchema();
            if (schemaCreated) {
                initialize(initializer);
            }
        }
    }

    private void initialize(DataSourceInitializer initializer) {
        try {
            initializer.getDataSources().stream()
                    .map(DataSourceSchemaCreatedEvent::new)
                    .forEach(this.applicationContext::publishEvent);
            // 侦听器可能尚未注册，因此不要依赖它。
            if (!this.initialized) {
                this.dataSourceInitializer.initSchema();
                this.initialized = true;
            }
        } catch (IllegalStateException ex) {
            logger.warn("Could not send event to complete DataSource initialization (" + ex.getMessage() + ")");
        }
    }

    @Override
    public void onApplicationEvent(DataSourceSchemaCreatedEvent event) {
        // 注意，事件可以发生不止一次
        // 这里不使用事件数据源
        DataSourceInitializer initializer = getDataSourceInitializer();
        if (!this.initialized && initializer != null) {
            initializer.initSchema();
            this.initialized = true;
        }
    }

    private DataSourceInitializer getDataSourceInitializer() {
        if (this.dataSourceInitializer == null) {
            List<HikariDataSource> dataSources = this.dataSources;
            if (!dataSources.isEmpty()) {
                this.dataSourceInitializer = new DataSourceInitializer(dataSources, this.properties,
                        this.applicationContext);
            }
        }
        return this.dataSourceInitializer;
    }
}
