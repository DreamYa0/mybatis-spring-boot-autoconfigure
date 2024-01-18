package com.g7.framework.mybatis.transaction;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author dreamyao
 * @title
 * @date 2018/11/1 8:17 PM
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(value = TransactionTemplate.class)
public class TransactionAutoConfiguration {

    @Bean
    public TransactionProxy transactionProxy() {
        return new TransactionProxy();
    }
}
