package com.g7.framework.dynamic.loadbalance;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author dreamyao
 * @title
 * @date 2019-12-12 16:26
 * @since 1.0.0
 */
public class HashLoadBalance extends AbstractLoadBalance {

    private static final int VIRTUAL_NODE_NUM = 5;

    @Override
    protected String doSelect(Map<String, DataSource> dataSources) {
        final TreeMap<Long, String> treeMap = new TreeMap<>();
        Set<String> dataSourceNames = dataSources.keySet();
        for (String dataSourceName : dataSourceNames) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                long addressHash = hash("MYBATIS-" + dataSourceName + "-HASH-" + i);
                treeMap.put(addressHash, dataSourceName);
            }
        }

        return null;
    }

    private static long hash(final String key) {
        // md5 byte
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes;
        keyBytes = key.getBytes(StandardCharsets.UTF_8);

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = (long) (digest[3] & 0xFF) << 24
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);
        return hashCode & 0xffffffffL;
    }
}
