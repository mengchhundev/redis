package com.redis.demo.examples;

import com.redis.demo.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ════════════════════════════════════════════════════════════════════
 *  DEMO 5 — Pub/Sub Messaging
 * ════════════════════════════════════════════════════════════════════
 *
 *  Redis Pub/Sub lets components communicate without knowing about
 *  each other — a classic event-driven / message-bus pattern.
 *
 *   SUBSCRIBE channel        → listen for messages on a channel
 *   PUBLISH   channel msg    → broadcast a message
 *   PSUBSCRIBE pattern       → subscribe via glob pattern
 *
 *  Real-world uses:
 *   • Invalidating cache entries across multiple app servers
 *   • Sending real-time notifications / live-feed updates
 *   • Microservice event bus (lightweight alternative to Kafka)
 *
 *  Note: messages are NOT persisted. A subscriber that is offline
 *  when a message is published will miss it.
 *  Use Redis Streams if you need durability / replay.
 */
public class PubSubDemo {

    public static void run() throws InterruptedException {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  DEMO 5 · Pub/Sub Messaging");
        System.out.println("═".repeat(60));

        // How many messages the subscriber should receive before quitting
        int expectedMessages       = 3;
        CountDownLatch latch        = new CountDownLatch(expectedMessages);
        String         channel      = "notifications";

        // ── Subscriber thread ─────────────────────────────────────────────
        //  subscribe() BLOCKS the thread, so it must run in its own thread.
        Thread subscriberThread = new Thread(() -> {
            try (Jedis subscriberJedis = RedisConfig.getPool().getResource()) {
                System.out.println("\n  👂 [SUB]  Listening on channel '" + channel + "' …");

                subscriberJedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String ch, String message) {
                        System.out.println("  📨 [SUB]  Received on '" + ch + "': " + message);
                        latch.countDown();
                        if (latch.getCount() == 0) {
                            unsubscribe(); // stop listening after all messages received
                        }
                    }

                    @Override
                    public void onSubscribe(String ch, int subscribedChannels) {
                        System.out.println("  ✅ [SUB]  Subscribed to '" + ch + "'");
                    }

                    @Override
                    public void onUnsubscribe(String ch, int subscribedChannels) {
                        System.out.println("  🔕 [SUB]  Unsubscribed from '" + ch + "'");
                    }
                }, channel);
            }
        }, "redis-subscriber");

        subscriberThread.setDaemon(true);
        subscriberThread.start();

        // Give the subscriber a moment to set up
        Thread.sleep(400);

        // ── Publisher ─────────────────────────────────────────────────────
        try (Jedis publisherJedis = RedisConfig.getPool().getResource()) {
            String[] messages = {
                "user.login:alice",
                "order.placed:order-5001",
                "cache.invalidate:product:42"
            };

            for (String msg : messages) {
                Thread.sleep(300); // small gap between publishes
                long receivers = publisherJedis.publish(channel, msg);
                System.out.println("  📢 [PUB]  Published '" + msg + "'  (receivers=" + receivers + ")");
            }
        }

        // Wait until all messages are received (max 5 seconds)
        boolean done = latch.await(5, TimeUnit.SECONDS);
        System.out.println(done
            ? "\n✅ Pub/Sub demo complete — all messages delivered."
            : "\n⚠️  Pub/Sub demo timed out waiting for messages.");
    }
}
