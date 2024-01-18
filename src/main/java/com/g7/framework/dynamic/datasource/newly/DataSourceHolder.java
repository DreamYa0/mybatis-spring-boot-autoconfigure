package com.g7.framework.dynamic.datasource.newly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/18 下午9:59
 * @since 1.0.0
 */
public class DataSourceHolder {

    private static final ThreadLocal<String> holder = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(DataSourceHolder.class);

    public static String getDataSource() {
        // Thread t = Thread.currentThread();
        // System.out.println("----------------------- 读取数据线程ID：" + t.getId() + ", 读取数据线程名称：" + t.getName() + ", 读取数据：" + holder.get() + " -----------------------");
        return holder.get();
    }

    public static void setDataSource(String name) {
        // Thread t = Thread.currentThread();
        // System.out.println("----------------------- 写入数据线程ID：" + t.getId() + ", 写入数据线程名称：" + t.getName() + ", 写入数据：" + name + " -----------------------");
        holder.set(name);
    }

    public static void removeDataSource() {
        // Thread t = Thread.currentThread();
        // logger.info("----------------------- 清除数据线程ID：{} , 清除数据源：{} -----------------------", t.getId(), holder.get());
        holder.remove();
    }

}