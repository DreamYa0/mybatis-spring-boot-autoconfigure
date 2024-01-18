package com.g7.framework.hikari.autoconfigure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author dreamyao
 * @title
 * @date 2020-01-01 13:48
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = HikariConstants.HIKARI_DATA_SOURCE_PREFIX)
public class HikariDataSourceProperties {

    /**
     * HikariCP 多数据源负载均衡策略
     */
    private String loadBalance;
    /**
     * 此属性控制从池返回的连接的默认自动提交行为。它是一个布尔值。 默认值：true
     */
    private Boolean autoCommit;
    /**
     * 此属性控制客户端（即您）将等待来自池的连接的最大毫秒数。如果在没有可用连接的情况下超过此时间，
     * 则会抛出SQLException。最低可接受的连接超时时间为250 ms。 默认值：30000（30秒）
     */
    private Long connectionTimeout;
    /**
     * 此属性控制测试连接是否活跃的最长时间。此值必须小于connectionTimeout。可接受的最低验证超时为250毫秒。 默认值：5000
     */
    private Long validationTimeout;
    /**
     * 此属性控制允许连接在池中闲置的最长时间。 此设置仅适用于minimumIdle定义为小于maximumPoolSize。
     * 一旦池达到连接，空闲连接将不会退出minimumIdle。连接是否因闲置而退出，最大变化量为+30秒，
     * 平均变化量为+15秒。在超时之前，连接永远不会退出。值为0意味着空闲连接永远不会从池中删除。允许的最小值是10000ms（10秒）。
     * 默认值：600000（10分钟）
     */
    private Long idleTimeout;
    /**
     * 此属性控制在记录表示可能的连接泄漏的消息之前，连接可以离开池的时间。值为0表示禁用泄漏检测。启用泄漏检测的最低可接受值为2000（2秒）
     * 默认值：0
     */
    private Long leakDetectionThreshold;
    /**
     * 此属性控制池中连接的最大生存期。正在使用的连接永远不会退休，只有在关闭后才会被删除。
     * 在逐个连接的基础上，应用较小的负面衰减来避免池中的大量消失。
     * 我们强烈建议设置此值，并且应该比任何数据库或基础设施规定的连接时间限制短几秒。
     * 值为0表示没有最大寿命（无限寿命），当然是idleTimeout设定的主题。 默认值：1800000（30分钟）
     */
    private Long maxLifetime;
    /**
     * 此属性控制池允许达到的最大大小，包括空闲和正在使用的连接。基本上这个值将决定到数据库后端的最大实际连接数。
     * 对此的合理价值最好由您的执行环境决定。当池达到此大小并且没有空闲连接可用时，
     * 对getConnection（）的调用将connectionTimeout在超时前阻塞达几毫秒。请阅读关于游泳池尺寸。 默认值：10
     */
    private Integer maxPoolSize = 10;
    /**
     * 该属性控制HikariCP尝试在池中维护的最小空闲连接数。
     * 如果空闲连接低于此值并且连接池中的总连接数少于此值maxPoolSize，则HikariCP将尽最大努力快速高效地添加其他连接。
     * 但是，为了获得最佳性能和响应尖峰需求，我们建议不要设置此值，而是允许HikariCP充当固定大小的连接池。
     * 默认值：与maxPoolSize相同
     */
    private Integer minIdle = 10;
    /**
     * 如果您的驱动程序支持JDBC4，我们强烈建议您不要设置此属性。
     * 这是针对不支持JDBC4的“传统”驱动程序Connection.isValid() API。
     * 这是在连接从池中获得连接以确认与数据库的连接仍然存在之前将要执行的查询。
     * 再一次，尝试运行没有此属性的池，如果您的驱动程序不符合JDBC4的要求，HikariCP将记录一个错误以告知您。 默认值：无
     */
    private String connectionTestQuery;

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Long getValidationTimeout() {
        return validationTimeout;
    }

    public void setValidationTimeout(Long validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    public Long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Long getLeakDetectionThreshold() {
        return leakDetectionThreshold;
    }

    public void setLeakDetectionThreshold(Long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
    }

    public Long getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(Integer maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }
}
