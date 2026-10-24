package com.shop.dao;

/** Business-rule failure during checkout (e.g. insufficient stock). Its message is safe to show to the customer. */
public class OrderException extends Exception {
    private static final long serialVersionUID = 1L;
    public OrderException(String message) { super(message); }
}
