package com.redis.demo.config;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * RedisConfig — creates and manages a single JedisPool.
 *
 * JedisPool is thread-safe and reuses connections efficiently,
 * so it should be treated as a singleton throughout the app.
 */
public class RedisConfig {

    private static final String HOST    = "localhost";
    private static final int    PORT    = 6379;
    private static final int    TIMEOUT = 2000; // ms

    private static JedisPool pool;

    /** Returns the shared pool, creating it on first call. */
    public static JedisPool getPool() {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);           // max connections in pool
            config.setMaxIdle(5);             // max idle connections
            config.setMinIdle(1);             // min idle connections
            config.setTestOnBorrow(true);     // validate connection before use
            pool = new JedisPool(config, HOST, PORT, TIMEOUT);
            System.out.println("✅ Redis connection pool created → " + HOST + ":" + PORT);
        }
        return pool;
    }

    /** Gracefully shuts down the pool. Call once at app exit. */
    public static void shutdown() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
            System.out.println("🔒 Redis connection pool closed.");
        }
    }
}
