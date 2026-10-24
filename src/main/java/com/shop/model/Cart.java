package com.shop.model;

import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Session-scoped shopping cart. Nothing is persisted until checkout.
 * Methods are synchronized because a session can be touched by concurrent requests (e.g. two browser tabs).
 */
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String SESSION_KEY = "cart";

    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    /** Fetches the cart from the session, lazily creating it. */
    public static Cart from(HttpSession session) {
        synchronized (session) { // one cart per session, even under racing first requests
            Cart cart = (Cart) session.getAttribute(SESSION_KEY);
            if (cart == null) {
                cart = new Cart();
                session.setAttribute(SESSION_KEY, cart);
            }
            return cart;
        }
    }

    /** Adds quantity, capping the line at available stock. */
    public synchronized void add(Product p, int qty) {
        CartItem line = items.get(p.getId());
        if (line == null) {
            line = new CartItem(p, 0);
            items.put(p.getId(), line);
        } else {
            line.refresh(p);
        }
        line.setQuantity(Math.min(line.getQuantity() + Math.max(qty, 1), p.getStock()));
    }

    /** Sets an absolute quantity (capped at stock); a value below 1 removes the line. */
    public synchronized void setQuantity(Product p, int qty) {
        if (qty < 1) { items.remove(p.getId()); return; }
        CartItem line = items.get(p.getId());
        if (line != null) {
            line.refresh(p);
            line.setQuantity(Math.min(qty, p.getStock()));
        }
    }

    public synchronized void remove(int productId) { items.remove(productId); }
    public synchronized void clear() { items.clear(); }

    public synchronized Collection<CartItem> getItems() { return new ArrayList<>(items.values()); }
    public synchronized boolean isEmpty() { return items.isEmpty(); }

    public synchronized int getItemCount() {
        int n = 0;
        for (CartItem i : items.values()) n += i.getQuantity();
        return n;
    }

    public synchronized BigDecimal getTotal() {
        BigDecimal t = BigDecimal.ZERO;
        for (CartItem i : items.values()) t = t.add(i.getLineTotal());
        return t;
    }
}
