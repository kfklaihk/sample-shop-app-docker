package com.docker.atsea.controller;

import java.security.Principal;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.docker.atsea.model.CartItem;
import com.docker.atsea.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    public static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    CartService cartService;

    private String getCartOwnerId(HttpSession session, Principal principal) {
        if (principal != null) {
            return "user:" + principal.getName();
        }
        return "session:" + session.getId();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ResponseEntity<List<CartItem>> getCart(HttpSession session, Principal principal) {
        List<CartItem> cart = cartService.getCart(getCartOwnerId(session, principal));
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Void> addToCart(@RequestBody CartItem item, HttpSession session, Principal principal) {
        cartService.addToCart(getCartOwnerId(session, principal), item);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{productId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> removeFromCart(@PathVariable("productId") Integer productId, HttpSession session, Principal principal) {
        cartService.removeFromCart(getCartOwnerId(session, principal), productId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE)
    public ResponseEntity<Void> clearCart(HttpSession session, Principal principal) {
        cartService.clearCart(getCartOwnerId(session, principal));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
