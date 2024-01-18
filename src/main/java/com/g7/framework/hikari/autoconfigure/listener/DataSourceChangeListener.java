package com.g7.framework.hikari.autoconfigure.listener;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.g7.framework.hikari.autoconfigure.datasource.HikariDataSource2;
import com.g7.framework.hikari.autoconfigure.properties.HikariConstants;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import com.g7.framework.hikari.autoconfigure.util.CharMatcher;
import com.g7.framework.hikari.customizer.HikariDataSourceCustomizer;
import com.g7.framwork.common.util.extension.ExtensionLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author dreamyao
 * @title
 * @date 2019/1/2 10:08 AM
 * @since 1.0.0
 */
public class DataSourceChangeListener implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private static ConcurrentMap<String, String> modifyDataSourceMap = new ConcurrentHashMap<>();
    @Autowired
    private DataSourceProperties dataSourceProperties;
    @Autowired
    private HikariDataSourceProperties hikariProperties;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        refreshApplicationContext();
    }

    private void refreshApplicationContext() {

        Config apolloConfig = ConfigService.getAppConfig();

        DefaultListableBeanFactory beanFactory =
                (DefaultListableBeanFactory)applicationContext.getAutowireCapableBeanFactory();

        apolloConfig.addChangeListener(changeEvent -> {

            Set<String> changedKeys = changeEvent.changedKeys();
            for (String changeKey : changedKeys) {
                if (changeKey.contains(HikariConstants.DATA_SOURCE_JDBC_URL_PREFIX) &&
                        changeKey.contains(HikariConstants.DATA_SOURCE_HIKARI_URL_NAME)) {

                    ConfigChange change = changeEvent.getChange(changeKey);
                    if (Objects.equals(change.getChangeType(), PropertyChangeType.ADDED)
                            || Objects.equals(change.getChangeType(), PropertyChangeType.MODIFIED)) {
                        // 缓存
                        modifyDataSourceMap.putIfAbsent(changeKey, change.getNewValue());

                    } else if (Objects.equals(change.getChangeType(), PropertyChangeType.DELETED)) {
                        // 销毁对应的数据源Bean，暂时不支持此种方式
                        // beanFactory.removeBeanDefinition(getBeanName(changeKey));
                    }
                }
            }
            registerDruidDataSources(beanFactory);
        });
    }

    private void registerDruidDataSources(DefaultListableBeanFactory beanFactory) {

        modifyDataSourceMap.forEach((changeKey,changeValue) -> {

            // 注册 BeanDefinition
            // 把驼峰命名转为 - 划线命名
            String camelName = CharMatcher.camelToHyphen().apply(getBeanName(changeKey));

            // 注册以 DataSource 为后缀的别名
            if (!StringUtils.endsWithIgnoreCase(camelName, HikariConstants.BEAN_SUFFIX)) {
                beanFactory.registerAlias(camelName, camelName + HikariConstants.BEAN_SUFFIX);
            }
            HikariDataSource2 dataSource2 = new HikariDataSource2();
            dataSource2.setJdbcUrl(changeValue);
            dataSource2.setDataSourceProperties(dataSourceProperties);
            dataSource2.setHikariDataSourceProperties(hikariProperties);

            // 获取所以扩展点名集合
            ExtensionLoader<HikariDataSourceCustomizer> extensionLoader =
                    ExtensionLoader.getExtensionLoader(HikariDataSourceCustomizer.class);
            Set<String> loadedExtensions = extensionLoader.getLoadedExtensionNames();
            // 加载扩展点
            List<HikariDataSourceCustomizer> sourceCustomizers = loadedExtensions.stream()
                    .map(extensionLoader::getExtension).collect(Collectors.toList());

            // 定制化配置，拥有最高优先级，会覆盖之前已有的配置
            sourceCustomizers.forEach(customizer -> customizer.customize(dataSource2));

            beanFactory.applyBeanPostProcessorsBeforeInitialization(dataSource2, camelName);
            beanFactory.registerSingleton(camelName, dataSource2);

        });

        if (Boolean.FALSE.equals(CollectionUtils.isEmpty(modifyDataSourceMap))) {
            modifyDataSourceMap.clear();
        }
    }

    private String getBeanName(String changeKey) {
        return changeKey.substring(HikariConstants.DATA_SOURCE_JDBC_URL_PREFIX.length() + 1, changeKey.length() - 4);
    }
}
