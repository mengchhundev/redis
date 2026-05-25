# 🔴 Redis Cache — Java Demo

A hands-on Java project that walks you through the core concepts of **Redis caching** using [Jedis](https://github.com/redis/jedis) (the official Java Redis client).

---

## 📂 Project Structure

```
Redis/
├── pom.xml                              # Maven build file
├── README.md
└── src/main/java/com/redis/demo/
    ├── Main.java                        # Entry point — runs all demos
    ├── config/
    │   └── RedisConfig.java             # JedisPool singleton
    ├── model/
    │   └── User.java                    # Simple POJO (serialised to JSON for caching)
    ├── service/
    │   ├── FakeDatabase.java            # Simulated DB with 300 ms latency
    │   └── UserCacheService.java        # Cache-Aside implementation
    └── examples/
        ├── BasicOperationsDemo.java     # Demo 1 — SET, GET, DEL, INCR …
        ├── DataTypesDemo.java           # Demo 2 — List, Hash, Set, Sorted Set
        ├── CacheAsideDemo.java          # Demo 3 — Cache-Aside pattern
        ├── TTLAndExpiryDemo.java        # Demo 4 — TTL, rate-limiting
        └── PubSubDemo.java              # Demo 5 — Pub/Sub messaging
```

---

## 🧠 Concepts Covered

### Demo 1 — Basic String Operations
| Command | Description |
|---------|-------------|
| `SET key value` | Store a string value |
| `GET key` | Retrieve a value (null if missing) |
| `DEL key` | Delete a key |
| `EXISTS key` | Check if a key exists (1/0) |
| `SETEX key secs val` | Store with TTL in one atomic command |
| `TTL key` | Remaining lifetime in seconds |
| `INCR key` | Atomically increment an integer |
| `APPEND key val` | Append text to a string |
| `MSET / MGET` | Set / get multiple keys at once |

---

### Demo 2 — Redis Data Types

| Type | Real-world use |
|------|---------------|
| **String** | Sessions, flags, counters, simple caches |
| **List** | Task queues, recent activity feeds |
| **Hash** | Storing objects (user profiles, settings) |
| **Set** | Unique visitors, tags, friend lists |
| **Sorted Set** | Leaderboards, priority queues |

---

### Demo 3 — Cache-Aside Pattern (Lazy Loading)

The most popular caching strategy:

```
READ  →  Redis? ──hit──▶ return cached value (fast)
                 │
               miss
                 │
                 ▼
             Database ──▶ store in Redis ──▶ return value
```

```
WRITE →  Update Database ──▶ DELETE cache entry
         (next read will re-populate from fresh DB data)
```

The demo measures response time so you can see the speed difference between a **cache miss** (~300 ms DB call) and a **cache hit** (<5 ms Redis lookup).

---

### Demo 4 — TTL & Key Expiry

Keys expire automatically — no cron jobs needed:

```java
jedis.setex("otp:user42", 300, "738291");   // expires in 5 minutes
jedis.expire("session:abc", 1800);           // add TTL to existing key
jedis.persist("config:flag");               // remove TTL (make permanent)
jedis.ttl("otp:user42");                    // check remaining time
```

Also includes a **sliding-window rate limiter** built purely in Redis.

---

### Demo 5 — Pub/Sub Messaging

```
Publisher ──PUBLISH channel msg──▶ Redis ──▶ Subscriber(s)
```

Real-world uses:
- Broadcasting **cache invalidation** events across multiple servers
- Sending **real-time notifications**
- Lightweight **microservice event bus**

> ⚠️ Pub/Sub messages are **not persisted**. Use **Redis Streams** if you need message replay/durability.

---

## 🚀 Getting Started

### 1. Start Redis

```bash
# Docker (easiest)
docker run -d -p 6379:6379 --name redis-demo redis:7

# macOS
brew install redis && brew services start redis

# Ubuntu/Debian
sudo apt install redis-server && sudo service redis start
```

### 2. Build

```bash
mvn clean package -q
```

### 3. Run

```bash
java -jar target/redis-cache-demo-1.0-SNAPSHOT.jar
```

---

## 📦 Dependencies

| Library | Purpose |
|---------|---------|
| `jedis 5.1` | Java Redis client (connection pool + commands) |
| `jackson-databind` | Serialize Java objects → JSON for cache storage |
| `logback-classic` | Logging (Jedis internal logs suppressed) |

---

## 💡 Key Takeaways

1. **Redis is not just a cache** — it's a versatile data structure store.
2. **Choose the right data type** — Hashes for objects, Sorted Sets for leaderboards, Sets for unique memberships.
3. **Always set a TTL** — prevents cache from growing unboundedly.
4. **Cache-Aside is the default pattern** — the app owns cache population and invalidation.
5. **JedisPool is essential** — always use a connection pool in production; never share a single `Jedis` instance across threads.
