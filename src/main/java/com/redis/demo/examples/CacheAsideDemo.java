package com.redis.demo.examples;

import com.redis.demo.model.User;
import com.redis.demo.service.UserCacheService;

/**
 * ════════════════════════════════════════════════════════════════════
 *  DEMO 3 — Cache-Aside Pattern
 * ════════════════════════════════════════════════════════════════════
 *
 *  This is the most widely-used caching strategy.
 *  The application manages the cache itself:
 *
 *   ┌─────────────────────────────────────────────────────────────┐
 *   │  Client → App → Redis (hit?) ─yes─→ return data            │
 *   │                      │                                      │
 *   │                     no                                      │
 *   │                      ↓                                      │
 *   │                  Database → store in Redis → return data   │
 *   └─────────────────────────────────────────────────────────────┘
 *
 *  Key observations in this demo:
 *  • First call  → CACHE MISS  (slow, DB query)
 *  • Second call → CACHE HIT   (fast, Redis only)
 *  • After update → CACHE MISS again (invalidation)
 */
public class CacheAsideDemo {

    public static void run() throws InterruptedException {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO 3 · Cache-Aside Pattern");
        System.out.println("═".repeat(60));

        UserCacheService cacheService = new UserCacheService();

        // ── Round 1: first access — cache is empty ─────────────────────
        System.out.println("\n▶ Step 1: First fetch of user 1 (CACHE MISS expected)");
        long start = System.currentTimeMillis();
        User user = cacheService.getUser(1);
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("  ⏱️  Response time: %d ms  ←  slow (DB round-trip)%n", elapsed);
        System.out.println("  Result: " + user);

        // ── Round 2: second access — Redis should serve it ─────────────
        System.out.println("\n▶ Step 2: Fetch user 1 again (CACHE HIT expected)");
        start = System.currentTimeMillis();
        user = cacheService.getUser(1);
        elapsed = System.currentTimeMillis() - start;
        System.out.printf("  ⏱️  Response time: %d ms  ←  fast (Redis)%n", elapsed);
        System.out.println("  Result: " + user);
        System.out.println("  TTL remaining: " + cacheService.getRemainingTTL(1) + "s");

        // ── Round 3: update user → invalidate cache ────────────────────
        System.out.println("\n▶ Step 3: Update user 1's email (cache invalidation)");
        user.setEmail("alice.new@example.com");
        cacheService.updateUser(user);

        // ── Round 4: next read must go to DB again ─────────────────────
        System.out.println("\n▶ Step 4: Fetch user 1 after update (CACHE MISS expected)");
        start = System.currentTimeMillis();
        user = cacheService.getUser(1);
        elapsed = System.currentTimeMillis() - start;
        System.out.printf("  ⏱️  Response time: %d ms  ←  slow again (DB re-fetch)%n", elapsed);
        System.out.println("  Result: " + user + "  ← shows updated email ✓");

        // ── Round 5: non-existent user ─────────────────────────────────
        System.out.println("\n▶ Step 5: Fetch non-existent user 99");
        user = cacheService.getUser(99);
        System.out.println("  Result: " + user + "  (not cached — no point storing null)");

        System.out.println("\n✅ Cache-Aside demo complete.");
    }
}
