package com.g7.framework.dynamic.datasource.newly;

import com.g7.framework.dynamic.datasource.util.DataSourceUtils;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author dreamyao
 * @title
 * @date 2019-04-01 15:23
 * @since 1.0.0
 */
@Aspect
public class SpecifyDataSourceAspect implements ApplicationContextAware, Ordered {

    private static final int DEFAULT_ORDER = -2;
    private ApplicationContext applicationContext;
    private final HikariDataSourceProperties hikariProperties;

    public SpecifyDataSourceAspect(HikariDataSourceProperties hikariProperties) {
        this.hikariProperties = hikariProperties;
    }

    @Override
    public int getOrder() {
        // Spring Aspect的顺序，如果一个方法上有多个切面，则值越小的先执行。
        return DEFAULT_ORDER;
    }

    @Around("@annotation(com.g7.framework.dynamic.datasource.newly.SpecifyDataSource)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        String beforeDataSource = DataSourceHolder.getDataSource();

        try {

            Signature signature = point.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            // 获取目标类
            Class<?> target = point.getTarget().getClass();

            // 根据目标类方法中的注解，选择数据源
            SpecifyDataSource classAnnotation = target.getAnnotation(SpecifyDataSource.class);
            SpecifyDataSource methodAnnotation = method.getAnnotation(SpecifyDataSource.class);
            if (Objects.nonNull(classAnnotation)) {
                String currentDataSource = classAnnotation.value();
                setCurrentDateSource(beforeDataSource, currentDataSource);
            } else if (Objects.nonNull(methodAnnotation)) {
                String currentDataSource = methodAnnotation.value();
                setCurrentDateSource(beforeDataSource, currentDataSource);
            }

            return point.proceed();

        } finally {

            if (StringUtils.isEmpty(beforeDataSource)) {
                DataSourceHolder.removeDataSource();
            } else {
                // 如果方法1指定了d1数据源，方法2指定了d2数据源，方法2嵌套在方法1中，
                // 则需要方法2执行完后清除d2数据源且需要重新设置回d1数据源，以便方法1中的其他操作会走到正确的d1数据源
                DataSourceHolder.setDataSource(beforeDataSource);
            }
        }
    }

    private void setCurrentDateSource(String beforeDataSource, String currentDataSource) {

        if (StringUtils.isEmpty(currentDataSource)) {
            // 如果指定数据源名称为空，则走主库，同一注解实现指定数据源和指定主库数据源的选择需求
            currentDataSource = DataSourceUtils.loadBalanceDataSourceName(applicationContext,
                    hikariProperties);
        }

        // 如果新老数据源相同，则跳过
        if (Objects.equals(beforeDataSource, currentDataSource)) {
            return;
        }

        if (Boolean.FALSE.equals(StringUtils.isEmpty(currentDataSource))) {
            DataSourceHolder.setDataSource(currentDataSource);
        } else {
            // 兼容 没有配置主库的情况
            DataSourceHolder.setDataSource(beforeDataSource);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
