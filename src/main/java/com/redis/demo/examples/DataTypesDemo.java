package com.redis.demo.examples;

import com.redis.demo.config.RedisConfig;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;

/**
 * ════════════════════════════════════════════════════════════════════
 *  DEMO 2 — Redis Data Types
 * ════════════════════════════════════════════════════════════════════
 *
 *  Redis supports five core data structures beyond plain strings:
 *
 *   LIST         → ordered collection with push/pop (queue / stack)
 *   HASH         → field-value map (ideal for storing objects)
 *   SET          → unordered collection of unique members
 *   SORTED SET   → set with a numeric score for ordering
 *
 *  Choosing the right type drastically reduces the amount of logic
 *  you need to write in application code.
 */
public class DataTypesDemo {

    public static void run() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO 2 · Redis Data Types");
        System.out.println("═".repeat(60));

        try (Jedis jedis = RedisConfig.getPool().getResource()) {

            // ── 1. LIST ───────────────────────────────────────────────────
            //  Use-cases: task queues, message logs, recent-activity feeds.
            System.out.println("\n📌 LIST — task queue");
            jedis.del("tasks");
            jedis.rpush("tasks", "send_email", "resize_image", "sync_db"); // enqueue at right
            System.out.println("  RPUSH tasks → send_email, resize_image, sync_db");
            System.out.println("  LRANGE tasks 0 -1 → " + jedis.lrange("tasks", 0, -1));
            System.out.println("  LPOP  tasks       → " + jedis.lpop("tasks"));  // dequeue from left
            System.out.println("  LLEN  tasks       → " + jedis.llen("tasks") + " remaining");

            // ── 2. HASH ───────────────────────────────────────────────────
            //  Use-cases: storing objects/entities without JSON overhead.
            System.out.println("\n📌 HASH — user profile");
            jedis.del("user:1");
            jedis.hset("user:1", Map.of(
                "name",  "Alice",
                "email", "alice@example.com",
                "age",   "30",
                "role",  "admin"
            ));
            System.out.println("  HSET user:1 → " + jedis.hgetAll("user:1"));
            System.out.println("  HGET user:1 name → " + jedis.hget("user:1", "name"));
            jedis.hincrBy("user:1", "age", 1);           // atomic field increment
            System.out.println("  HINCRBY user:1 age +1 → age is now " + jedis.hget("user:1", "age"));

            // ── 3. SET ────────────────────────────────────────────────────
            //  Use-cases: tags, unique visitors, friend lists, permissions.
            System.out.println("\n📌 SET — unique page visitors");
            jedis.del("visitors:homepage");
            jedis.sadd("visitors:homepage", "alice", "bob", "charlie", "alice"); // alice added twice
            System.out.println("  SADD visitors → alice, bob, charlie, alice (duplicate ignored)");
            System.out.println("  SMEMBERS     → " + jedis.smembers("visitors:homepage"));
            System.out.println("  SCARD        → " + jedis.scard("visitors:homepage") + " unique visitors");
            System.out.println("  SISMEMBER alice → " + jedis.sismember("visitors:homepage", "alice"));
            System.out.println("  SISMEMBER dave  → " + jedis.sismember("visitors:homepage", "dave"));

            // SET intersection — users on both pages
            jedis.sadd("visitors:about", "bob", "diana", "alice");
            Set<String> both = jedis.sinter("visitors:homepage", "visitors:about");
            System.out.println("  SINTER homepage ∩ about → " + both);

            // ── 4. SORTED SET ─────────────────────────────────────────────
            //  Use-cases: leaderboards, rate limiting, priority queues.
            System.out.println("\n📌 SORTED SET — leaderboard");
            jedis.del("leaderboard");
            jedis.zadd("leaderboard", 4500, "alice");
            jedis.zadd("leaderboard", 7200, "bob");
            jedis.zadd("leaderboard", 3100, "charlie");
            jedis.zadd("leaderboard", 8900, "diana");
            System.out.println("  ZADD leaderboard (alice=4500, bob=7200, charlie=3100, diana=8900)");

            // Highest scorers first (ZREVRANGE)
            System.out.println("  ZREVRANGE leaderboard 0 -1 WITHSCORES →");
            jedis.zrevrangeWithScores("leaderboard", 0, -1)
                 .forEach(e -> System.out.printf("    %-10s %,.0f pts%n", e.getElement(), e.getScore()));

            System.out.println("  ZRANK alice (0-based from lowest) → " + jedis.zrank("leaderboard", "alice"));
            System.out.println("  ZSCORE diana → " + jedis.zscore("leaderboard", "diana").intValue() + " pts");

            // ── cleanup ───────────────────────────────────────────────────
            jedis.del("tasks", "user:1", "visitors:homepage", "visitors:about", "leaderboard");
            System.out.println("\n✅ Data types demo complete.");
        }
    }
}
