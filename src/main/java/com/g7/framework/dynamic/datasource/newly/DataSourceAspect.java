package com.g7.framework.dynamic.datasource.newly;

import com.g7.framework.dynamic.datasource.util.DataSourceUtils;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import org.apache.ibatis.session.SqlSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author dreamyao
 * @title 数据源自动选择的切面类
 * @date 2018/8/18 下午10:00
 * @since 1.0.0
 */
@Aspect
public class DataSourceAspect implements ApplicationContextAware, Ordered {

    private static final String READ_ONLY_METHOD_PREFFIX = "select";
    private static final String READ_ONLY_COUNT_PREFFIX = "count";
    private static final String READ_ONLY_FIND_PREFFIX = "find";
    private static final String READ_ONLY_GET_PREFFIX = "get";
    private static final String READ_ONLY_QUERY_PREFFIX = "query";
    private static final String READ_ONLY_LIST_PREFFIX = "list";
    private static final int DEFAULT_ORDER = -1;
    private final HikariDataSourceProperties hikariProperties;
    private final SqlSessionTemplate sqlSessionTemplate;
    private ApplicationContext applicationContext;

    public DataSourceAspect(HikariDataSourceProperties hikariProperties, SqlSessionTemplate sqlSessionTemplate) {
        this.hikariProperties = hikariProperties;
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Around("execution(* com..*.*Mapper.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        String beforeDataSource = DataSourceHolder.getDataSource();

        try {

            if (StringUtils.isEmpty(beforeDataSource)) {

                // 获取目标方法
                Signature signature = point.getSignature();
                MethodSignature methodSignature = (MethodSignature) signature;
                Method method = methodSignature.getMethod();

                Map<String, DataSource> dataSourceMap = applicationContext.getBeansOfType(DataSource.class);
                Map<String, DataSource> sourceDataSources = new HashMap<>(dataSourceMap);

                // 主库数据源集合中至少存在一个数据源实列
                Map<String, DataSource> masterTargetDataSources = new HashMap<>(6);
                // 从库数据源集合中可能不存在任何数据源实列
                Map<String, DataSource> slaveTargetDataSources = new HashMap<>(6);

                DataSourceUtils.classifyDataSources(sourceDataSources,
                        masterTargetDataSources, slaveTargetDataSources);

                // 判断方法是否处于事物中
                SqlSession session = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(),
                        sqlSessionTemplate.getExecutorType(),
                        sqlSessionTemplate.getPersistenceExceptionTranslator());

                boolean isTransactional = SqlSessionUtils.isSqlSessionTransactional(session,
                        sqlSessionTemplate.getSqlSessionFactory());

                String masterDataSourceName = DataSourceUtils.loadBalanceDataSourceName(masterTargetDataSources,
                        hikariProperties);
                String slaveDataSourceName = DataSourceUtils.loadBalanceDataSourceName(slaveTargetDataSources,
                        hikariProperties);

                String pointName = point.getSignature().getName();
                Master master = method.getAnnotation(Master.class);
                // 判断方法是否需要走从库
                boolean isChooseSlaveDataSource = pointName.startsWith(READ_ONLY_METHOD_PREFFIX)
                        || pointName.startsWith(READ_ONLY_COUNT_PREFFIX)
                        || pointName.startsWith(READ_ONLY_FIND_PREFFIX)
                        || pointName.startsWith(READ_ONLY_GET_PREFFIX)
                        || pointName.startsWith(READ_ONLY_QUERY_PREFFIX)
                        || pointName.startsWith(READ_ONLY_LIST_PREFFIX);

                if (isTransactional) {
                    // 如果在事物中那么同一事物的 CRUD操作都走主库
                    DataSourceHolder.setDataSource(masterDataSourceName);
                } else if (Objects.nonNull(master)) {
                    // 判断方法上如果有默认走主库的注解时，默认走主库
                    DataSourceHolder.setDataSource(masterDataSourceName);
                } else if (isChooseSlaveDataSource && Boolean.FALSE.equals(StringUtils.isEmpty(slaveDataSourceName))) {
                    DataSourceHolder.setDataSource(slaveDataSourceName);
                } else {
                    DataSourceHolder.setDataSource(masterDataSourceName);
                }
            }

            // 执行方法
            return point.proceed();

        } finally {
            // 当前线程中自己（当前Mapper接口或者Mapper接口中的方法）设置的数据源自己才能清理
            if (StringUtils.isEmpty(beforeDataSource)) {
                DataSourceHolder.removeDataSource();
            }
        }
    }

    @Override
    public int getOrder() {
        // Spring Aspect的顺序，如果一个方法上有多个切面，则值越小的先执行。
        return DEFAULT_ORDER;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
