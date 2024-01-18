package com.g7.framework.hikari.autoconfigure;

import com.g7.framework.hikari.autoconfigure.datasource.HikariDataSource2;
import com.g7.framework.hikari.autoconfigure.properties.HikariConstants;
import com.g7.framework.hikari.autoconfigure.util.CharMatcher;
import com.g7.framework.hikari.customizer.HikariDataSourceCustomizer;
import com.g7.framwork.common.util.extension.ExtensionLoader;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static java.util.Collections.emptyMap;

/**
 * Druid 数据源配置
 * @author dreamyao
 */
@Import(HikariDataSourceConfiguration.DataSourceImportSelector.class)
public class HikariDataSourceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(HikariDataSourceConfiguration.class);

    /**
     * 构造 BeanDefinition，通过 DruidDataSource2 实现继承 'spring.datasource.druid' 的配置
     * @return BeanDefinition druidBeanDefinition
     */
    private static BeanDefinition genericDruidBeanDefinition() {
        return BeanDefinitionBuilder
                .genericBeanDefinition(HikariDataSource2.class)
                .setDestroyMethodName("close")
                .getBeanDefinition();
    }

    /**
     * 单数据源注册
     * @author dreamyao
     */
    static class SingleDataSourceRegistrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(HikariConstants.BEAN_NAME)) {
                registry.registerBeanDefinition(HikariConstants.BEAN_NAME, genericDruidBeanDefinition());
            }
        }
    }

    /**
     * 多数据源注册
     * @author dreamyao
     */
    static class DynamicDataSourceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

        private Map<String, Object> dataSources;

        @Override
        public void setEnvironment(Environment environment) {
            this.dataSources = Binder.get(environment)
                    .bind(HikariConstants.DATA_SOURCE_JDBC_URL_PREFIX, Bindable.mapOf(String.class, Object.class))
                    .orElse(emptyMap());
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
            this.dataSources.keySet().forEach(dataSourceName -> {
                // 注册 BeanDefinition
                // 把驼峰命名转为 - 划线命名
                String camelName = CharMatcher.camelToHyphen().apply(dataSourceName);
                registry.registerBeanDefinition(camelName, genericDruidBeanDefinition());
                // 注册以 DataSource 为后缀的别名
                if (!StringUtils.endsWithIgnoreCase(camelName, HikariConstants.BEAN_SUFFIX)) {
                    registry.registerAlias(camelName, camelName + HikariConstants.BEAN_SUFFIX);
                }
            });
        }
    }

    /**
     * HikariDataSource2 的 Bean 处理器，将各数据源的自定义配置绑定到 Bean
     * @author dreamyao
     */
    static class DataSourceBeanPostProcessor implements EnvironmentAware, BeanPostProcessor {

        private final List<HikariDataSourceCustomizer> customizers;
        private Environment environment;
        private Map<String, Object> dataSources;

        public DataSourceBeanPostProcessor(ObjectProvider<List<HikariDataSourceCustomizer>> customizers) {

            // 获取所以扩展点名集合
            ExtensionLoader<HikariDataSourceCustomizer> extensionLoader =
                    ExtensionLoader.getExtensionLoader(HikariDataSourceCustomizer.class);
            Set<String> loadedExtensions = extensionLoader.getLoadedExtensionNames();
            // 加载扩展点
            List<HikariDataSourceCustomizer> sourceCustomizers = loadedExtensions.stream()
                    .map(extensionLoader::getExtension).collect(Collectors.toList());
            // 如果扩展点不存在，则采用spring bean注入的方式
            if (CollectionUtils.isEmpty(sourceCustomizers)) {
                sourceCustomizers = customizers.getIfAvailable(ArrayList::new);
            }
            this.customizers = sourceCustomizers;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
            this.dataSources = Binder.get(environment)
                    .bind(HikariConstants.DATA_SOURCE_JDBC_URL_PREFIX, Bindable.mapOf(String.class, Object.class))
                    .orElse(emptyMap());
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof HikariDataSource) {
                if (dataSources.isEmpty()) {
                    logger.info("hikari single data-source({}) init...", beanName);
                } else {
                    logger.info("hikari dynamic data-source({}) init...", beanName);
                }
                // 设置 Druid 名称
                HikariDataSource hikariDataSource = (HikariDataSource) bean;
                // 将 'spring.datasource.hikari.data-sources.${name}' 的配置绑定到 Bean
                if (!dataSources.isEmpty()) {
                    Binder.get(environment).bind(HikariConstants.DATA_SOURCE_JDBC_URL_PREFIX +
                            "." + beanName, Bindable.ofInstance(hikariDataSource));
                }
                // 定制化配置，拥有最高优先级，会覆盖之前已有的配置
                customizers.forEach(customizer -> customizer.customize(hikariDataSource));
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }

    /**
     * 数据源选择器
     * 当配置文件中存在 spring.datasource.hikari.data-sources 属性时为多数据源
     * 不存在则为单数据源
     * @author dreamyao
     */
    static class DataSourceImportSelector implements ImportSelector, EnvironmentAware {

        private Map<String, Object> dataSources;

        @Override
        public void setEnvironment(Environment environment) {
            this.dataSources = Binder.get(environment)
                    .bind(HikariConstants.DATA_SOURCE_JDBC_URL_PREFIX, Bindable.mapOf(String.class, Object.class))
                    .orElse(emptyMap());
        }

        @Override
        public String[] selectImports(AnnotationMetadata metadata) {
            Builder<Class<?>> imposts = Stream.<Class<?>>builder().add(DataSourceBeanPostProcessor.class);
            imposts.add(dataSources.isEmpty() ? SingleDataSourceRegistrar.class : DynamicDataSourceRegistrar.class);
            return imposts.build().map(Class::getName).toArray(String[]::new);
        }
    }
}