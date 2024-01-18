package com.g7.framework.mybatis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author dreamyao
 * @title mybatis 配置文件
 * @date 2018/9/7 上午9:43
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "mybatis")
public class MybatisProperties {

    /**
     * mybatis xml 配置文件存放路径.
     */
    private String configLocation;

    /**
     * mybatis mapper xml 文件存放路径
     */
    private String mapperLocation;

    /**
     * Packages to search type aliases. (Package delimiters are ",; \t\n")
     */
    private String typeAliasesPackage;

    /**
     * Packages to search for type handlers. (Package delimiters are ",; \t\n")
     */
    private String typeHandlersPackage;

    /**
     * 指示是否执行 MyBatis xml配置文件的状态检查。
     */
    private boolean checkConfigLocation = false;

    /**
     * 枚举对象包路径
     */
    private String enumPackage;

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public String getMapperLocation() {
        return mapperLocation;
    }

    public void setMapperLocation(String mapperLocation) {
        this.mapperLocation = mapperLocation;
    }

    public String getTypeAliasesPackage() {
        return typeAliasesPackage;
    }

    public void setTypeAliasesPackage(String typeAliasesPackage) {
        this.typeAliasesPackage = typeAliasesPackage;
    }

    public String getTypeHandlersPackage() {
        return typeHandlersPackage;
    }

    public void setTypeHandlersPackage(String typeHandlersPackage) {
        this.typeHandlersPackage = typeHandlersPackage;
    }

    public boolean isCheckConfigLocation() {
        return checkConfigLocation;
    }

    public void setCheckConfigLocation(boolean checkConfigLocation) {
        this.checkConfigLocation = checkConfigLocation;
    }

    public String getEnumPackage() {
        return enumPackage;
    }

    public void setEnumPackage(String enumPackage) {
        this.enumPackage = enumPackage;
    }
}
