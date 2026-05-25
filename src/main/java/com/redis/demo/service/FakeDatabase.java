package com.redis.demo.service;

import com.redis.demo.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * FakeDatabase simulates a slow, persistent data store (e.g. PostgreSQL).
 *
 * In a real application this would issue SQL queries.
 * Here it just sleeps 300 ms so you can feel the latency difference
 * between a cache hit and a database round-trip.
 */
public class FakeDatabase {

    private static final Map<Integer, User> DATA = new HashMap<>();

    static {
        DATA.put(1, new User(1, "Alice",   "alice@example.com",   30));
        DATA.put(2, new User(2, "Bob",     "bob@example.com",     25));
        DATA.put(3, new User(3, "Charlie", "charlie@example.com", 35));
        DATA.put(4, new User(4, "Diana",   "diana@example.com",   28));
    }

    /**
     * Simulates a database query with 300 ms latency.
     *
     * @param id user ID to look up
     * @return the User, or null if not found
     */
    public User findById(int id) {
        System.out.println("  🗄️  [DB]    Querying database for user " + id + " …");
        try {
            Thread.sleep(300); // artificial latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        User user = DATA.get(id);
        if (user != null) {
            System.out.println("  🗄️  [DB]    Found: " + user);
        } else {
            System.out.println("  🗄️  [DB]    User " + id + " not found.");
        }
        return user;
    }

    /** Simulates saving/updating a user in the database. */
    public void save(User user) {
        System.out.println("  🗄️  [DB]    Saving user " + user.getId() + " to database …");
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        DATA.put(user.getId(), user);
        System.out.println("  🗄️  [DB]    Saved: " + user);
    }
}
