package com.redis.demo.examples;

import com.redis.demo.config.RedisConfig;
import redis.clients.jedis.Jedis;

/**
 * ════════════════════════════════════════════════════════════════════
 *  DEMO 1 — Basic Redis Operations
 * ════════════════════════════════════════════════════════════════════
 *
 *  Covers the fundamental Redis string commands every developer
 *  should know:
 *
 *   SET  key value          → store a value
 *   GET  key                → retrieve a value (null if missing)
 *   DEL  key                → delete a key
 *   EXISTS key              → 1 if key exists, 0 otherwise
 *   EXPIRE key seconds      → set a TTL on an existing key
 *   SETEX key seconds value → set a value with TTL in one command
 *   TTL  key                → remaining time-to-live in seconds
 *   INCR key                → atomically increment an integer value
 *   APPEND key value        → append text to an existing string
 *   MSET / MGET             → set / get multiple keys at once
 */
public class BasicOperationsDemo {

    public static void run() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO 1 · Basic Redis String Operations");
        System.out.println("═".repeat(60));

        // Borrow a connection from the pool (auto-returned via try-with-resources)
        try (Jedis jedis = RedisConfig.getPool().getResource()) {

            // ── 1. SET & GET ──────────────────────────────────────────────
            System.out.println("\n📌 SET & GET");
            jedis.set("greeting", "Hello, Redis!");
            String value = jedis.get("greeting");
            System.out.println("  SET greeting = 'Hello, Redis!'");
            System.out.println("  GET greeting → " + value);

            // ── 2. EXISTS & DEL ───────────────────────────────────────────
            System.out.println("\n📌 EXISTS & DEL");
            System.out.println("  EXISTS greeting → " + jedis.exists("greeting"));
            jedis.del("greeting");
            System.out.println("  DEL greeting");
            System.out.println("  EXISTS greeting → " + jedis.exists("greeting"));
            System.out.println("  GET greeting    → " + jedis.get("greeting")
                    + "  (null = key gone)");

            // ── 3. TTL — SETEX & TTL command ─────────────────────────────
            System.out.println("\n📌 TTL — SETEX (value expires in 5 seconds)");
            jedis.setex("session:abc123", 5, "user:42");
            System.out.println("  SETEX session:abc123 5 'user:42'");
            System.out.println("  TTL   session:abc123 → " + jedis.ttl("session:abc123") + "s remaining");
            System.out.println("  GET   session:abc123 → " + jedis.get("session:abc123"));

            // ── 4. INCR — atomic counter ──────────────────────────────────
            System.out.println("\n📌 INCR — atomic counter");
            jedis.del("page_views");
            jedis.incr("page_views");
            jedis.incr("page_views");
            jedis.incr("page_views");
            System.out.println("  INCR page_views x3 → " + jedis.get("page_views"));

            // ── 5. APPEND ─────────────────────────────────────────────────
            System.out.println("\n📌 APPEND");
            jedis.set("log", "2026-05-25 START");
            jedis.append("log", " | event=login | user=42");
            System.out.println("  APPEND log → " + jedis.get("log"));

            // ── 6. MSET & MGET — batch operations ─────────────────────────
            System.out.println("\n📌 MSET & MGET (batch)");
            jedis.mset(
                "color",  "blue",
                "lang",   "Java",
                "cache",  "Redis"
            );
            java.util.List<String> values = jedis.mget("color", "lang", "cache", "missing_key");
            System.out.println("  MGET color, lang, cache, missing_key");
            System.out.println("  → " + values);   // "missing_key" returns null

            // ── cleanup ───────────────────────────────────────────────────
            jedis.del("page_views", "log", "color", "lang", "cache", "session:abc123");
            System.out.println("\n✅ Basic operations demo complete.");
        }
    }
}
