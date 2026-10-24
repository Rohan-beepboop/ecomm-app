package com.shop.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One line of the session cart. Name/price/image are display snapshots only; the authoritative
 * price is re-read from the database inside the checkout transaction (never trust session data for money).
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int productId;
    private String name;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int maxStock;
    private int quantity;

    public CartItem(Product p, int quantity) {
        this.productId = p.getId();
        refresh(p);
        this.quantity = quantity;
    }

    /** Re-syncs display data and stock ceiling from a freshly loaded product. */
    public void refresh(Product p) {
        this.name = p.getName();
        this.imageUrl = p.getImageUrl();
        this.unitPrice = p.getPrice();
        this.maxStock = p.getStock();
    }

    public BigDecimal getLineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getMaxStock() { return maxStock; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
