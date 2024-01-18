package com.g7.framework.mybatis.autoconfigure;

import com.g7.framework.cat.plugin.CatMybatisPlugin;
import com.g7.framework.dynamic.datasource.newly.DataSourceAspect;
import com.g7.framework.dynamic.datasource.newly.DynamicDataSource;
import com.g7.framework.dynamic.datasource.newly.SpecifyDataSourceAspect;
import com.g7.framework.hikari.autoconfigure.listener.DataSourceChangeListener;
import com.g7.framework.hikari.autoconfigure.listener.ShutdownHookListener;
import com.g7.framework.hikari.autoconfigure.properties.HikariDataSourceProperties;
import com.g7.framework.mybatis.enums.CodedEnumHandler;
import com.github.pagehelper.PageInterceptor;
import com.google.common.collect.Sets;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static com.g7.framework.hikari.autoconfigure.properties.HikariConstants.MASTER_DATASOURCE_NAME;

/**
 * @author dreamyao
 * @title mybatis 自动化配置类
 * @date 2018/9/7 上午9:43
 * @since 1.0.0
 */
@org.springframework.context.annotation.Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties({MybatisProperties.class, HikariDataSourceProperties.class})
@EnableTransactionManagement
public class MybatisAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MybatisAutoConfiguration.class);
    private final MybatisProperties properties;
    private final ResourceLoader resourceLoader;
    private final HikariDataSourceProperties hikariProperties;
    @Autowired(required = false)
    private CatMybatisPlugin catMybatisPlugin;


    public MybatisAutoConfiguration(MybatisProperties properties, ResourceLoader resourceLoader,
                                    HikariDataSourceProperties hikariProperties) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.hikariProperties = hikariProperties;
    }

    @PostConstruct
    public void checkConfigFileExists() {
        if (this.properties.isCheckConfigLocation() && StringUtils.hasText(this.properties.getConfigLocation())) {
            org.springframework.core.io.Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource
                    + " (please add config file or check your Mybatis configuration)");
        }
    }

    @Resource
    @Bean("dataSource")
    // @ConditionalOnBean(name = {"masterDataSource", "slaveDataSource"})
    @ConditionalOnMissingBean(value = {DynamicDataSource.class})
    public DynamicDataSource dataSource(Map<String, HikariDataSource> dataSources) {
        logger.debug("dynamic datasource init...");
        Assert.notEmpty(dataSources, "master data source is null.");

        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        DataSource defaultDataSource = null;
        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {

            if (dataSources.size() == 1) {
                defaultDataSource = entry.getValue();
                break;
            }

            if (entry.getKey().toLowerCase().contains(MASTER_DATASOURCE_NAME)) {
                defaultDataSource = entry.getValue();
                break;
            }
        }

        // 确保数据源至少存在主库数据源
        Assert.notNull(defaultDataSource, "default dataSource is null.");

        Map<Object, Object> map = new HashMap<>(dataSources);

        // 加入所有数据源
        dynamicDataSource.setTargetDataSources(map);
        // 加入默认数据源
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        return dynamicDataSource;
    }

    @Bean("sqlSessionFactory")
    @ConditionalOnMissingBean
    @ConditionalOnBean(name = "dataSource")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
                                               @Qualifier("pageInterceptor") PageInterceptor pageInterceptor)
            throws Exception {
        logger.debug("sql session factory init...");
        Assert.notNull(dataSource, "master data source is null.");

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();

        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        factory.setConfiguration(defaultConfiguration());

        Interceptor[] interceptors;
        if (Objects.nonNull(catMybatisPlugin)) {
            interceptors = new Interceptor[2];
            interceptors[0] = pageInterceptor;
            interceptors[1] = catMybatisPlugin;
        } else {
            interceptors = new Interceptor[1];
            interceptors[0] = pageInterceptor;
        }

        factory.setPlugins(interceptors);

        // mybatis mapper xml文件路径
        String mapperLocation = properties.getMapperLocation();
        if (Boolean.FALSE.equals(StringUtils.isEmpty(mapperLocation))) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            factory.setMapperLocations(resolver.getResources(mapperLocation));
        }

        // 类型别名包路径
        if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.properties.getTypeAliasesPackage());
        }

        // 枚举类型处理器包路径
        if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.properties.getTypeHandlersPackage());
        }

        return factory.getObject();
    }

    private Configuration defaultConfiguration() {

        Configuration conf = new Configuration();

        // 给予被嵌套的resultMap以字段-属性的映射支持
        conf.setAutoMappingBehavior(AutoMappingBehavior.FULL);
        // 数据库超过60秒仍未响应则超时
        conf.setDefaultStatementTimeout(60);
        // 设置启用数据库字段下划线映射到java对象的驼峰式命名属性，默认为false
        conf.setMapUnderscoreToCamelCase(true);
        // 全局性设置懒加载。如果设为‘false'，则所有相关联的都会被初始化加载
        conf.setLazyLoadingEnabled(true);
        // 如果不配置,使用println()会触发延迟加载
        conf.setLazyLoadTriggerMethods(Sets.newHashSet("equals","clone","hashCode","toString"));
        // 当设置为‘true'的时候，懒加载的对象可能被任何懒属性全部加载。否则，每个属性都按需加载
        conf.setAggressiveLazyLoading(false);

        String enumPackage = properties.getEnumPackage();
        if (Boolean.FALSE.equals(StringUtils.isEmpty(enumPackage))) {

            // handle all enum with CodedEnumHandler
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .setUrls(ClasspathHelper.forPackage(properties.getEnumPackage()))
                            .setScanners(new SubTypesScanner(false))
                            .filterInputsBy(new FilterBuilder().includePackage(properties.getEnumPackage())));
            Set<Class<? extends Enum>> types = reflections.getSubTypesOf(Enum.class);

            for (Class<? extends Enum> type : types) {
                try {
                    if (type.isEnum()) {
                        // 设置枚举处理器
                        conf.getTypeHandlerRegistry().register(type, CodedEnumHandler.class);
                    }
                } catch (Exception e) {
                    logger.error("Registering enum handler failed.", e);
                }
            }
        }

        return conf;
    }

    @Bean("pageInterceptor")
    @ConditionalOnMissingBean
    public PageInterceptor pageInterceptor() {

        Properties properties = new Properties();
        properties.setProperty("helperDialect", "mysql");
        properties.setProperty("pageSizeZero", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("reasonable", "false");

        PageInterceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setProperties(properties);

        return pageInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(value = SqlSessionFactory.class)
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        logger.debug("sql session template init...");
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    @ConditionalOnClass(value = DataSourceAspect.class)
    @ConditionalOnBean(value = SqlSessionTemplate.class)
    @ConditionalOnMissingBean(value = {DataSourceAspect.class})
    public DataSourceAspect dataSourceAspect(@Autowired SqlSessionTemplate sqlSessionTemplate) {
        logger.debug("dynamic datasource aspect init...");
        return new DataSourceAspect(hikariProperties, sqlSessionTemplate);
    }

    @Bean
    @ConditionalOnClass(value = SpecifyDataSourceAspect.class)
    @ConditionalOnBean(value = SqlSessionTemplate.class)
    @ConditionalOnMissingBean(value = {SpecifyDataSourceAspect.class})
    public SpecifyDataSourceAspect specifyDataSourceAspect() {
        logger.debug("specify datasource aspect init...");
        return new SpecifyDataSourceAspect(hikariProperties);
    }

    @Bean
    @ConditionalOnBean(value = DataSource.class)
    public PlatformTransactionManager annotationDrivenTransactionManager(DataSource dataSource) {
        logger.debug("platform transaction manager init...");
        Assert.notNull(dataSource, "master data source is null.");
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @ConditionalOnBean(name = "dataSource")
    public ShutdownHookListener shutdownHookListener(Map<String, HikariDataSource> dataSources) {
        logger.debug("druid shutdown hook listener init...");
        return new ShutdownHookListener(dataSources);
    }

    @Bean
    @ConditionalOnBean(name = "dataSource")
    public DataSourceChangeListener dataSourceChangeListener() {
        return new DataSourceChangeListener();
    }
}
