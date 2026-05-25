package com.redis.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.demo.config.RedisConfig;
import com.redis.demo.model.User;
import redis.clients.jedis.Jedis;

/**
 * UserCacheService — Cache-Aside pattern implementation.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 *  Cache-Aside (Lazy Loading) pattern:
 *
 *  READ:
 *    1. Check Redis first.
 *    2. Cache HIT  → return cached value (fast, no DB call).
 *    3. Cache MISS → query database, store result in Redis, return.
 *
 *  WRITE:
 *    1. Update the database.
 *    2. Invalidate (delete) the cache entry so the next read
 *       fetches fresh data from the database.
 * └─────────────────────────────────────────────────────────────────┘
 *
 * This is the most common caching pattern — the application code
 * owns cache population and invalidation.
 */
public class UserCacheService {

    private static final int    TTL_SECONDS = 10;        // cache entry lifetime
    private static final String KEY_PREFIX  = "user:";   // namespace for user keys

    private final FakeDatabase db;
    private final ObjectMapper jackson;

    public UserCacheService() {
        this.db      = new FakeDatabase();
        this.jackson = new ObjectMapper();
    }

    // ── READ — Cache-Aside ────────────────────────────────────────────────────

    /**
     * Fetches a user by ID, using Redis as a look-aside cache.
     *
     * @param id user ID
     * @return User or null
     */
    public User getUser(int id) {
        String cacheKey = KEY_PREFIX + id;

        try (Jedis jedis = RedisConfig.getPool().getResource()) {

            // 1. Check cache
            String cached = jedis.get(cacheKey);

            if (cached != null) {
                // ── CACHE HIT ──────────────────────────────────────────────
                System.out.println("  ⚡ [CACHE] HIT  for key '" + cacheKey + "'");
                return jackson.readValue(cached, User.class);
            }

            // ── CACHE MISS ────────────────────────────────────────────────
            System.out.println("  ❌ [CACHE] MISS for key '" + cacheKey + "'");

            // 2. Query database
            User user = db.findById(id);

            // 3. Populate cache (only if the record exists)
            if (user != null) {
                String json = jackson.writeValueAsString(user);
                jedis.setex(cacheKey, TTL_SECONDS, json);
                System.out.println("  💾 [CACHE] Stored '" + cacheKey
                        + "' with TTL=" + TTL_SECONDS + "s");
            }

            return user;

        } catch (Exception e) {
            throw new RuntimeException("Cache lookup failed for user " + id, e);
        }
    }

    // ── WRITE — Update & Invalidate ───────────────────────────────────────────

    /**
     * Updates a user in the database and invalidates its cache entry.
     *
     * @param user updated user object
     */
    public void updateUser(User user) {
        String cacheKey = KEY_PREFIX + user.getId();

        // 1. Persist to database
        db.save(user);

        // 2. Invalidate cache — next read will re-fetch fresh data
        try (Jedis jedis = RedisConfig.getPool().getResource()) {
            long deleted = jedis.del(cacheKey);
            System.out.println("  🗑️  [CACHE] Invalidated key '" + cacheKey
                    + "' (deleted=" + deleted + ")");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns the remaining TTL for a cached user, or -2 if not cached. */
    public long getRemainingTTL(int id) {
        try (Jedis jedis = RedisConfig.getPool().getResource()) {
            return jedis.ttl(KEY_PREFIX + id);
        }
    }
}
