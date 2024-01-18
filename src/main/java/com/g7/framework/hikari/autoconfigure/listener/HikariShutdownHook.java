package com.g7.framework.hikari.autoconfigure.listener;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dreamyao
 * @title
 * @date 2018/12/23 2:00 PM
 * @since 1.0.0
 */
class HikariShutdownHook extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(HikariShutdownHook.class);
    /**
     * Has it already been destroyed or not?
     */
    private final AtomicBoolean destroyed;
    /**
     * 数据源集合为事实不可变的
     */
    private final Map<String, HikariDataSource> dataSources;

    protected static HikariShutdownHook getDruidShutdownHook(Map<String, HikariDataSource> dataSources) {
        return new HikariShutdownHook("HikariShutdownHook", dataSources);
    }

    private HikariShutdownHook(String name, Map<String, HikariDataSource> dataSources) {
        super(name);
        this.destroyed = new AtomicBoolean(false);
        this.dataSources = dataSources;
    }

    @Override
    public void run() {
        if (logger.isInfoEnabled()) {
            logger.info("Run hikari shutdown hook now.");
        }
        destroyAll();
    }

    protected void destroyAll() {
        if (Boolean.FALSE.equals(destroyed.compareAndSet(false, true))) {
            return;
        }
        destroyDruid();
    }

    private void destroyDruid() {
        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(dataSources))) {
            Set<Map.Entry<String, HikariDataSource>> entries = dataSources.entrySet();
            for (Map.Entry<String, HikariDataSource> entry : entries) {
                HikariDataSource dataSource = entry.getValue();
                if (Boolean.FALSE.equals(dataSource.isClosed())) {
                    dataSource.close();
                    logger.info("Data source is closed, data source name is {}", entry.getKey());
                }
            }
        }
    }
}
