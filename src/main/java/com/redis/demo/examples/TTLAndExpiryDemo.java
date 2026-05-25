package com.redis.demo.examples;

import com.redis.demo.config.RedisConfig;
import redis.clients.jedis.Jedis;

/**
 * ════════════════════════════════════════════════════════════════════
 *  DEMO 4 — TTL (Time-To-Live) & Key Expiry
 * ════════════════════════════════════════════════════════════════════
 *
 *  TTL is one of Redis's most powerful features — data evicts itself
 *  without any cleanup code in your application.
 *
 *  Common real-world uses:
 *   • Session tokens        → expire after 30 minutes idle
 *   • OTP / verification codes → expire after 5 minutes
 *   • Rate-limit counters   → expire after 1 minute window
 *   • API response caches   → expire after a few seconds/minutes
 *
 *  Key commands:
 *   SETEX key secs value    → set with TTL (atomic)
 *   EXPIRE key secs         → add/update TTL on existing key
 *   PERSIST key             → remove TTL (make key permanent)
 *   TTL  key                → seconds remaining (-1 = no expiry, -2 = gone)
 *   PTTL key                → milliseconds remaining
 */
public class TTLAndExpiryDemo {

    public static void run() throws InterruptedException {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO 4 · TTL & Key Expiry");
        System.out.println("═".repeat(60));

        try (Jedis jedis = RedisConfig.getPool().getResource()) {

            // ── 1. SETEX — OTP with 5-second TTL ─────────────────────────
            System.out.println("\n📌 OTP token — expires in 5 seconds");
            jedis.setex("otp:user42", 5, "738291");
            System.out.println("  SETEX otp:user42 5 '738291'");
            System.out.println("  TTL   otp:user42 → " + jedis.ttl("otp:user42") + "s");
            System.out.println("  GET   otp:user42 → " + jedis.get("otp:user42"));

            System.out.println("  ⏳ Waiting 3 seconds …");
            Thread.sleep(3_000);
            System.out.println("  TTL   otp:user42 → " + jedis.ttl("otp:user42") + "s  (counting down)");

            System.out.println("  ⏳ Waiting 3 more seconds (total 6s) …");
            Thread.sleep(3_000);
            System.out.println("  TTL   otp:user42 → " + jedis.ttl("otp:user42") + "  (-2 = expired & gone)");
            System.out.println("  GET   otp:user42 → " + jedis.get("otp:user42") + "  (null = key evicted)");

            // ── 2. EXPIRE on an existing key ──────────────────────────────
            System.out.println("\n📌 Adding TTL to a permanent key with EXPIRE");
            jedis.set("config:feature_flag", "true");
            System.out.println("  SET   config:feature_flag 'true' (no TTL)");
            System.out.println("  TTL   config:feature_flag → " + jedis.ttl("config:feature_flag")
                    + "  (-1 = permanent)");
            jedis.expire("config:feature_flag", 10);
            System.out.println("  EXPIRE config:feature_flag 10");
            System.out.println("  TTL   config:feature_flag → " + jedis.ttl("config:feature_flag") + "s");

            // ── 3. PERSIST — remove TTL ───────────────────────────────────
            System.out.println("\n📌 PERSIST — make an expiring key permanent");
            jedis.persist("config:feature_flag");
            System.out.println("  PERSIST config:feature_flag");
            System.out.println("  TTL    config:feature_flag → " + jedis.ttl("config:feature_flag")
                    + "  (-1 = permanent again)");

            // ── 4. Rate-limit counter with sliding window ─────────────────
            System.out.println("\n📌 Rate-limit counter — 3 requests allowed per 10 seconds");
            String rateLimitKey = "rate:api:user42";
            jedis.del(rateLimitKey);

            for (int i = 1; i <= 5; i++) {
                long count = jedis.incr(rateLimitKey);
                if (count == 1) {
                    jedis.expire(rateLimitKey, 10); // start the window on first request
                }
                String status = count <= 3 ? "✅ ALLOWED" : "🚫 BLOCKED";
                System.out.printf("  Request %d → count=%d  %s  (TTL=%ds)%n",
                        i, count, status, jedis.ttl(rateLimitKey));
            }

            // ── cleanup ───────────────────────────────────────────────────
            jedis.del("otp:user42", "config:feature_flag", rateLimitKey);
            System.out.println("\n✅ TTL & expiry demo complete.");
        }
    }
}
