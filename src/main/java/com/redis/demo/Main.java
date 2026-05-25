package com.redis.demo;

import com.redis.demo.config.RedisConfig;
import com.redis.demo.examples.*;
import redis.clients.jedis.Jedis;

/**
 * ════════════════════════════════════════════════════════════════════
 *  Redis Cache — Java Demo Application
 * ════════════════════════════════════════════════════════════════════
 *
 *  Prerequisites:
 *    • Redis server running on localhost:6379
 *      → Docker:  docker run -d -p 6379:6379 redis:7
 *      → macOS:   brew install redis && brew services start redis
 *      → Linux:   sudo apt install redis-server && sudo service redis start
 *
 *  Build & Run:
 *    mvn clean package -q
 *    java -jar target/redis-cache-demo-1.0-SNAPSHOT.jar
 *
 *  Demos included:
 *    1. Basic String Operations    — SET, GET, DEL, INCR, MSET …
 *    2. Redis Data Types           — List, Hash, Set, Sorted Set
 *    3. Cache-Aside Pattern        — lazy loading + invalidation
 *    4. TTL & Key Expiry           — OTP, rate-limiting
 *    5. Pub/Sub Messaging          — real-time events
 * ════════════════════════════════════════════════════════════════════
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          Redis Cache Demo — Java + Jedis                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        // ── Verify Redis connection ───────────────────────────────────────
        try (Jedis jedis = RedisConfig.getPool().getResource()) {
            String pong = jedis.ping();
            System.out.println("🔗 Redis ping → " + pong);
        } catch (Exception e) {
            System.err.println("❌ Cannot connect to Redis on localhost:6379");
            System.err.println("   Start Redis first:");
            System.err.println("   → docker run -d -p 6379:6379 redis:7");
            System.exit(1);
        }

        try {
            // ── Run each demo ─────────────────────────────────────────────
            BasicOperationsDemo.run();

            Thread.sleep(500);
            DataTypesDemo.run();

            Thread.sleep(500);
            CacheAsideDemo.run();

            Thread.sleep(500);
            TTLAndExpiryDemo.run();

            Thread.sleep(500);
            PubSubDemo.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Demo interrupted: " + e.getMessage());
        } finally {
            RedisConfig.shutdown();
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║  All demos finished! Check the output above to see how  ║");
        System.out.println("║  Redis cache works in each scenario.                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
    }
}
