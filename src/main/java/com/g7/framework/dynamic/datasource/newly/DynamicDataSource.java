package com.g7.framework.dynamic.datasource.newly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author dreamyao
 * @title 获取当前ThreadLocal设置的dataSource （动态设置的dataSource）
 * @date 2018/8/18 下午9:57
 * @since 1.0.0
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private static Logger logger = LoggerFactory.getLogger(DynamicDataSource.class);

    private static boolean printLog = false;


    public static void setPrintLog(boolean printLog) {
        DynamicDataSource.printLog = printLog;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        // 供调试，勿删
        /*System.out.println("=================DataSourceHolder.getDataSource()===========================");
        String ret = DataSourceHolder.getDataSource();
        System.out.println("DS: "+ret);
        System.out.println("=================DataSourceHolder.getDataSource()===========================");
        return ret;*/
        String ret = DataSourceHolder.getDataSource();

        if (printLog) {
            Thread thread = Thread.currentThread();
            logger.info("---------------------------- 读取数据线程ID：{} , 使用数据源：{} --------------------------",
                    thread.getId(), ret);
        }
        return ret;
    }
}