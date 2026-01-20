package com.docker.atsea.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.docker.atsea.model.CartItem;

@Service("cartService")
public class CartServiceImpl implements CartService {

    private static final String CART_PREFIX = "cart:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addToCart(String sessionId, CartItem item) {
        String key = CART_PREFIX + sessionId;
        List<CartItem> cart = getCart(sessionId);
        
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
        
        redisTemplate.opsForValue().set(key, cart, 24, TimeUnit.HOURS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CartItem> getCart(String sessionId) {
        String key = CART_PREFIX + sessionId;
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) {
            return new ArrayList<>();
        }
        return (List<CartItem>) obj;
    }

    @Override
    public void removeFromCart(String sessionId, Integer productId) {
        String key = CART_PREFIX + sessionId;
        List<CartItem> cart = getCart(sessionId);
        cart.removeIf(item -> item.getProductId().equals(productId));
        redisTemplate.opsForValue().set(key, cart, 24, TimeUnit.HOURS);
    }

    @Override
    public void clearCart(String sessionId) {
        String key = CART_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
