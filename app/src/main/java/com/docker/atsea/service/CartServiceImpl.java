package com.docker.atsea.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.docker.atsea.model.CartItem;

@Service("cartService")
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private static final String CART_PREFIX = "cart:";
    private static final long CART_TTL_MILLIS = TimeUnit.HOURS.toMillis(24);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ConcurrentHashMap<String, CachedCart> inMemoryCarts = new ConcurrentHashMap<>();
    private volatile boolean redisUnavailableLogged = false;

    @Override
    public void addToCart(String sessionId, CartItem item) {
        String key = cartKey(sessionId);
        List<CartItem> cart = loadCart(key);
        
        boolean found = false;
        for (CartItem cartItem : cart) {
            if (cartItem.getProductId().equals(item.getProductId())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }
        
        if (!found) {
            cart.add(item);
        }
        
        saveCart(key, cart);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(String sessionId) {
        String key = cartKey(sessionId);
        return loadCart(key);
    }

    @Override
    public void removeFromCart(String sessionId, Integer productId) {
        String key = cartKey(sessionId);
        List<CartItem> cart = loadCart(key);
        cart.removeIf(item -> item.getProductId().equals(productId));
        saveCart(key, cart);
    }

    @Override
    public void clearCart(String sessionId) {
        String key = cartKey(sessionId);
        deleteCart(key);
    }

    private String cartKey(String sessionId) {
        return CART_PREFIX + sessionId;
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> loadCart(String key) {
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj instanceof List) {
                return (List<CartItem>) obj;
            }
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logRedisUnavailable(e);
            return getCartFromMemory(key);
        }
        return getCartFromMemory(key);
    }

    private void saveCart(String key, List<CartItem> cart) {
        try {
            redisTemplate.opsForValue().set(key, cart, 24, TimeUnit.HOURS);
            return;
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logRedisUnavailable(e);
        }
        saveCartToMemory(key, cart);
    }

    private void deleteCart(String key) {
        try {
            redisTemplate.delete(key);
            return;
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logRedisUnavailable(e);
        }
        inMemoryCarts.remove(key);
    }

    private List<CartItem> getCartFromMemory(String key) {
        CachedCart cached = inMemoryCarts.get(key);
        if (cached == null || cached.isExpired()) {
            inMemoryCarts.remove(key);
            return new ArrayList<>();
        }
        return new ArrayList<>(cached.items);
    }

    private void saveCartToMemory(String key, List<CartItem> cart) {
        inMemoryCarts.put(key, new CachedCart(cart, System.currentTimeMillis() + CART_TTL_MILLIS));
    }

    private void logRedisUnavailable(Exception e) {
        if (!redisUnavailableLogged) {
            redisUnavailableLogged = true;
            logger.warn("Redis unavailable, falling back to in-memory cart storage.", e);
        }
    }

    private static class CachedCart {
        private final List<CartItem> items;
        private final long expiresAt;

        private CachedCart(List<CartItem> items, long expiresAt) {
            this.items = new ArrayList<>(items);
            this.expiresAt = expiresAt;
        }

        private boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
