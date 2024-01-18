package com.g7.framework.dynamic.datasource.newly;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify DataSource (指定数据源)<br>
 * 注意：<br>
 * {@code @SpecifyDataSource}放在类级别上等同于该类的每个公有方法都放上了{@code @SpecifyDataSource}<br>
 * {@code @SpecifyDataSource}只对公有法有效，因为都是Spring AOP代理）
 * @author dreamyao
 * @title
 * @date 2018/8/18 下午9:58
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SpecifyDataSource {

    /**
     * 数据源的名称
     */
    String value() default "";
}
