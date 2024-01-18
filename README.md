# 一、maven坐标

```xml
<!--mybatis 自动配置-->
<dependency>
    <groupId>com.g7.framework</groupId>
    <artifactId>mybatis-spring-boot-autoconfigure</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

# 二、改进

相对mybatis-spring-boot-autoconfigure组件1.0.0版本 增加如下改进（包含但不仅限）

1、可以任意定制化配置druid连接池配置

2、可以配置任意数量的数据源

3、实现多主多从、一主一从、一主多从、多主一从 类型数据源配置

4、实现多数据源之间的多种负载均衡策略，并开放负载均衡策略定制，可以实现任意方式的定制

5、提供Java代码方式的数据源定制扩展，可以对数据源进行任何特殊的定制

6、去掉 1.0.0版本一些冗余配置

7、实现数据源监控，监控页面http://localhost:应用端口/druid

# 三、Apollo简单配置示例

```properties
spring.datasource.driver-class-name = com.mysql.jdbc.Driver
spring.datasource.username = exchange_test_whitebox
spring.datasource.password = jNzHmF5evo5U


# 是否开启加密
spring.datasource.druid.config.enabled = true
# 加密密钥
spring.datasource.druid.config.key =

spring.datasource.druid.stat-view-servlet.enabled = true
spring.datasource.druid.slf4j.enabled = true
spring.datasource.druid.slf4j.data-source-log-enabled = false
spring.datasource.druid.slf4j.connection-log-enabled = false
spring.datasource.druid.slf4j.statement-log-enabled = false
spring.datasource.druid.slf4j.result-set-log-enabled = false
spring.datasource.druid.web-stat.enabled = true
# 多主多从、一主多从、多主一从 数据源类型时，同主或同从数据源之间的负载均衡策略
spring.datasource.druid.loadbalance = random
spring.datasource.druid.initial-size = 1
spring.datasource.druid.max-active = 20
spring.datasource.druid.time-between-eviction-runs-millis = 60000
spring.datasource.druid.min-evictable-idle-time-millis = 1800000
spring.datasource.druid.test-on-borrow = true

spring.datasource.druid.data-sources.master-one.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
spring.datasource.druid.data-sources.master-two.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
spring.datasource.druid.data-sources.master-three.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
spring.datasource.druid.data-sources.slave-one.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
spring.datasource.druid.data-sources.slave-two.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
spring.datasource.druid.data-sources.slave-three.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
spring.datasource.druid.data-sources.slave-four.url = jdbc:mysql://rm-8vbwk2yqgi44m3wj8.mysql.zhangbei.rds.aliyuncs.com/exchange_test_whitebox?useUnicode=true&characterEncoding=UTF8&useLegacyDatetimeCode=false&sessionVariables=time_zone='%2B0:00'
```

# 四、Druid 多数据源支持

## 使用方式

编辑 application 配置文件，druid-starter 会将 `spring.datasource.druid.data-sources` 开头的的属性注入到 Map<String, DruidDataSource> 中，并根据该 Map 构建数据源。

```yml
spring:
  autoconfigure:
    ## 多数据源环境下必须排除掉 DataSourceAutoConfiguration，否则会导致循环依赖报错
    exclude:
      org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
  datasource:
    ## 以 'spring.datasource' 和 'spring.datasource.druid' 开头的属性会作为公共配置，注入到每一个数据源
    driver-class-name: org.h2.Driver
    username: root
    password: 123456
    druid:
      ## 多数据源的标识，若该属性存在则为多数据源环境，不存在则为单数据源环境
      data-sources:
        ### master 数据源的配置，以下为 master 数据源独有的配置
        master:
          url: jdbc:h2:file:./master
        ### slave 数据源的配置，以下为 slave 数据源独有的配置
        slave:
          url: jdbc:h2:file:./slave
```

注： 排除 `org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration` 有两种方式，以下方式任选其一即可：

1. 在

   ```
   @SpringBootApplication
   ```

   注解中排除

   ```
   @SpringBootApplication(exclude =DataSourceAutoConfiguration.class)
   ```

2. 在

   ```
   apollo配置中心
   ```

   中排除

   ```properties
   spring.autoconfigure.exclude = org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
   ```

## 原理

### 公共配置

多数据源其实很好实现，编写 N 个数据源的配置，并且声明 N 个 Bean 即可，但带来的一个问题是各数据源之间存在大量相同的配置，每个数据源全部声明一遍既繁琐又无用，为了解决这个问题，mybatis-spring-boot-autoconfigure 1.0.1版本 中新增了两个类：DruidDataSource2 和 AbstractDruidDataSource2

其中 AbstractDruidDataSource2 是个抽象类，继承自 com.alibaba.druid.pool.DruidDataSource，主要作用是自动注入 `spring.datasource` 和 `spring.datasource.druid` 开头的属性，并作为 parent bean 为各个数据源实例提供公共配置。

DruidDataSource2 继承自 AbstractDruidDataSource2，内部没有任何代码，目的是继承 AbstractDruidDataSource2 中的已注入的配置，同时可以注入各数据源单独的配置，如遇到相同的属性会覆盖父类的同名属性。

也就是说，以下两种方式是等价的：

```yml
方式一：
spring:
 datasource:
   druid:
     driver-class-name: org.h2.Driver
     username: root
     password: 123456
     data-sources:
       master:
         url: jdbc:h2:file:./master
       slave:
         url: jdbc:h2:file:./slave
方式二：
spring:
 datasource:
   druid:
     master:
       driver-class-name: org.h2.Driver
       username: root
       password: 123456
       url: jdbc:h2:file:./master
     slave:
       driver-class-name: org.h2.Driver
       username: root
       password: 123456
       url: jdbc:h2:file:./slave
```

**自动配置**

mybatis-spring-boot-autoconfigure 1.0.1版本 判断是否多数据源的条件是 apollo配置中心是否存在 `spring.datasource.druid.data-sources` 属性，若存在即为多数据源环境，不存在则为单数据源环境。

多数据源环境下，druid-starter 会将 `spring.datasource.druid.data-sources` 开头的属性注入到 Map<String, DruidDataSource> 中，然后遍历该 Map，并通过构造 BeanDefinition 的方式来构造各个数据源，最后通过 PropertySourcesBinder 将各数据源单独的属性注入到各数据源。

# 五、Druid全配置说明

```yml
# 单数据源，多数据源的配置请看下方
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:example
    username: root
    password: Biyu5YzU+6sxDRbmWEa3B2uUcImzDo0BuXjTlL505+/pTb+/0Oqd3ou1R6J8+9Fy3CYrM18nBDqf6wAaPgUGOg==
    schema: classpath:import.sql
    druid:
      initial-size: 0
      max-active: 8
      min-idle: 0
      max-wait: -1
      not-full-timeout-retry-count: 0
      validation-query: SELECT 1
      validation-query-timeout: -1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      time-between-eviction-runs-millis: 60000
      time-between-connect-error-millis: 500
      min-evictable-idle-time-millis: 1800000
      max-evictable-idle-time-millis: 25200000
      phy-timeout-millis: -1
      pool-prepared-statements: false
      share-prepared-statements: false
      max-pool-prepared-statement-per-connection-size: 10
      use-global-data-source-stat: false
      stat:
        ## 是否开启 StatFilter，默认 true，可以不写（https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_StatFilter）
        enabled: true
        ## 数据库类型
        db-type:
        ## 是否开启慢查询统计
        log-slow-sql: false
        ## 慢查询的定义
        slow-sql-millis: 3000
        ## 是否开启合并参数化 SQL
        merge-sql: false
        ## 是否开启连接的堆栈信息
        connection-stack-trace-enable: false
      slf4j:
        ## 是否开启 StatFilter，默认 false（https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_LogFilter）
        enabled: true
        ## datasource log 的总开关，前置条件：druid.sql.DataSource 的日志级别为 debug
        ## 无调用者
        data-source-log-enabled: true
        ## connection log 的总开关，前置条件：druid.sql.Connection 的日志级别为 debug
        connection-log-enabled: true
        ## connection error log 的总开关，前置条件：druid.sql.Connection 的日志级别为 debug
        ## 无调用者
        connection-log-error-enabled: true
        ### 无调用者
        connection-connect-before-log-enabled: true
        ### {conn-410001} connected
        ### {conn-410001} pool-connect
        connection-connect-after-log-enabled: true
        ### {conn-410001} pool-recycle
        ### {conn-410001} closed
        connection-close-after-log-enabled: true
        ### 无调用者
        connection-commit-after-log-enabled: true
        ### 无调用者
        connection-rollback-after-log-enabled: true
        ## statement log 的总开关，前置条件：druid.sql.Statement 的日志级别为 debug
        statement-log-enabled: true
        ## statement sql 输出选项，可以开启多个
        statement-sql-format-option:
          ### 关键字大写
          upp-case: false
          ### 表名不敏感
          desensitize: false
          ### 格式化
          pretty-format: true
          ### 参数化，column=?
          parameterized: false
        ## statement sql 格式化选项
        statement-sql-pretty-format: true
        ### {conn-410001, pstmt-420002} created. $sql
        statement-create-after-log-enabled: true
        ### {conn-410001, pstmt-420002} Parameters :[]
        ### {conn-410001, pstmt-420002} Types :[]
        statement-parameter-set-log-enabled: true
        ### {conn-410001, pstmt-420002} executed. $executable-sql
        statement-executable-sql-log-enable: false
        ### {conn-410001, pstmt-420002} executed. 2.073044 millis. $sql
        statement-execute-after-log-enabled: true
        ### {conn-410001, pstmt-420002} closed
        statement-close-after-log-enabled: true
        ### 无调用者，LogFilter#isStatementPrepareAfterLogEnabled
        statement-prepare-after-log-enabled: true
        ### 无调用者，LogFilter#isStatementPrepareCallAfterLogEnabled
        statement-prepare-call-after-log-enabled: true
        ### 无调用者，LogFilter#isStatementExecuteQueryAfterLogEnabled
        statement-execute-query-after-log-enabled: true
        ### 无调用者，LogFilter#isStatementExecuteUpdateAfterLogEnabled
        statement-execute-update-after-log-enabled: true
        ### 无调用者，LogFilter#isStatementExecuteBatchAfterLogEnabled
        statement-execute-batch-after-log-enabled: true
        ### {conn-410001, pstmt-420002} clearParameters.
        statement-parameter-clear-log-enable: true
        ## result-set log 的总开关，前置条件：druid.sql.ResultSet 的日志级别为 debug
        result-set-log-enabled: true
        ### {conn-410001, pstmt-420000, rs-450000} open
        ### {conn-410001, pstmt-420000, rs-450000} Header: []
        result-set-open-after-log-enabled: true
        ### {conn-410001, pstmt-420000, rs-450000} Result: []
        result-set-next-after-log-enabled: true
        ### {conn-410001, pstmt-420000, rs-450000} closed
        result-set-close-after-log-enabled: true
        ## statement error log 的总开关
        ## {conn-410001, pstmt-420002} executed error.
        statement-log-error-enabled: true
        ## result-set error log 的总开关
        ## 无调用者，LogFilter#isResultSetLogErrorEnabled
        result-set-log-error-enabled: true
      wall:
        ## 是否开启 WallFilter，默认 false（https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE-wallfilter）
        enabled: true
        ## 数据库类型
        db-type: h2
        ## 检测到攻击 SQL 时输出错误日志，默认 false
        log-violation: true
        ## 检测到攻击 SQL 时抛出异常，默认 true
        throw-exception: false
        ## WallFilter 自定义配置
        config:
          ### 检查 delete 语句是否不包含 where 条件
          delete-where-none-check: true
      config:
        ## 是否开启 ConfigFilter，默认 false（https://github.com/alibaba/druid/wiki/%E4%BD%BF%E7%94%A8ConfigFilter）
        enabled: true
      aop-stat:
        ## 是否开启 DruidStatInterceptor，默认 false
        enabled: false
      web-stat:
        ## 是否开启 WebStatFilter，默认 false（https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_%E9%85%8D%E7%BD%AEWebStatFilter）
        enabled: false
      stat-view-servlet:
        ## 是否开启 StatViewServlet，默认 false（https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE_StatViewServlet%E9%85%8D%E7%BD%AE）
        enabled: true
        ## 登陆用户名
        login-username: druid
        ## 登陆密码
        login-password: druid

---

# 多数据源配置方式
## 各个数据源中相同的配置可以放到 `spring.datasource` 中，`spring.datasource.druid` 会
## 继承 `spring.datasource` 的配置，如果遇到相同的属性会覆盖掉 `spring.datasource`
spring:
  datasource:
    ### 所有数据源都会继承这些属性
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:example
    username: root
    password: Biyu5YzU+6sxDRbmWEa3B2uUcImzDo0BuXjTlL505+/pTb+/0Oqd3ou1R6J8+9Fy3CYrM18nBDqf6wAaPgUGOg==
    schema: classpath:import.sql
    druid:
      ### 子数据源也可以自定义配置，与 `spring.datasource` 相同的属性会覆盖 `spring.datasource` 的配置
      ### 不同的属性只会注入到当前数据源，不会对其它数据源造成影响
      ### 最终该数据源的配置为 `spring.datasource.*` + `spring.datasource.druid.*` + `spring.datasource.druid.${name}.*`
      data-sources:
        order-master:
          ### 会覆盖 `spring.datasource.url`
          url: jdbc:h2:file:./order_master
        order-slave:
          url: jdbc:h2:file:./order_slave
```

# 六、数据源负载均衡策略扩展

如果自己想要扩展负载均衡策略，只需要进行如下操作

实现 LoadBalance 接口

![image-20190319103059827](../../Library/Application Support/typora-user-images/image-20190319103059827.png)

# 七、数据源定制器扩展

如果想要扩展数据源定制器，需要实现 DruidDataSourceCustomizer 接口后进行如下操作

![image-20190319103127779](../../Library/Application Support/typora-user-images/image-20190319103127779.png)



