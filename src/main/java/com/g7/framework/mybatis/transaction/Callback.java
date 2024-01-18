package com.g7.framework.mybatis.transaction;

/**
 * 泛型加持。通用的事务业务逻辑。
 * @author zhangbin
 * @date 2018/5/23.
 */
@FunctionalInterface
public interface Callback<T> {

    /**
     * 事务范围内的业务逻辑，带有返回值。
     * @return T
     * @throws Exception 未知异常
     */
    T execute() throws Exception;
}
