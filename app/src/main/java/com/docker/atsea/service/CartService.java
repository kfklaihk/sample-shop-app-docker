package com.docker.atsea.service;

import java.util.List;
import com.docker.atsea.model.CartItem;

public interface CartService {
    void addToCart(String sessionId, CartItem item);
    List<CartItem> getCart(String sessionId);
    void removeFromCart(String sessionId, Integer productId);
    void clearCart(String sessionId);
}
