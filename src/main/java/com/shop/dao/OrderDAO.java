package com.shop.dao;

import com.shop.model.CartItem;
import com.shop.model.Cart;
import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.util.DBUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    /**
     * Commits the session cart to the database as ONE atomic transaction:
     * lock product rows -> verify stock -> decrement stock -> insert order -> insert lines.
     * Either everything is saved or nothing is (rollback), so stock can never drift from orders.
     *
     * @return the new order id
     * @throws OrderException when a product vanished or stock is insufficient (nothing is saved)
     */
    public int placeOrder(int userId, Cart cart, String shippingAddress) throws SQLException, OrderException {
        List<CartItem> lines = new ArrayList<>(cart.getItems());
        if (lines.isEmpty()) throw new OrderException("Your cart is empty.");

        // Lock rows in ascending id order. Two concurrent checkouts that touch the same products
        // then always acquire locks in the same sequence, which rules out lock-order deadlocks.
        lines.sort(Comparator.comparingInt(CartItem::getProductId));

        Connection con = null;
        try {
            con = DBUtil.getConnection();
            con.setAutoCommit(false); // begin transaction

            List<OrderItem> snapshot = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (CartItem line : lines) {
                // FOR UPDATE: blocks other buyers of this product until we commit, preventing overselling.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT name, price, stock FROM products WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, line.getProductId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new OrderException("\"" + line.getName() + "\" is no longer available. Please remove it from your cart.");
                        }
                        String name = rs.getString("name");
                        // Price comes from the DB, not the session: the customer (or a stale cart) cannot dictate it.
                        BigDecimal price = rs.getBigDecimal("price");
                        int stock = rs.getInt("stock");
                        if (stock < line.getQuantity()) {
                            throw new OrderException("Only " + stock + " of \"" + name + "\" left in stock. Please update your cart.");
                        }
                        OrderItem oi = new OrderItem();
                        oi.setProductId(line.getProductId());
                        oi.setProductName(name);
                        oi.setUnitPrice(price);
                        oi.setQuantity(line.getQuantity());
                        snapshot.add(oi);
                        total = total.add(oi.getLineTotal());
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE id = ?")) {
                    ps.setInt(1, line.getQuantity());
                    ps.setInt(2, line.getProductId());
                    ps.executeUpdate();
                }
            }

            int orderId;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orders (user_id, total, status, shipping_address) VALUES (?, ?, 'PLACED', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setBigDecimal(2, total);
                ps.setString(3, shippingAddress);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Order id was not generated");
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity) VALUES (?,?,?,?,?)")) {
                for (OrderItem oi : snapshot) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, oi.getProductId());
                    ps.setString(3, oi.getProductName());
                    ps.setBigDecimal(4, oi.getUnitPrice());
                    ps.setInt(5, oi.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return orderId;

        } catch (SQLException | OrderException e) {
            rollbackQuietly(con);
            throw e;
        } finally {
            // Connection is managed manually (it needs rollback control), so the finally block is mandatory:
            // restore autocommit and release the connection no matter how we leave this method.
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ignored) { }
                try { con.close(); } catch (SQLException ignored) { }
            }
        }
    }

    /** A customer's orders (newest first) with their lines, fetched in a single join to avoid N+1 queries. */
    public List<Order> findByUser(int userId) throws SQLException {
        String sql = "SELECT o.id, o.total, o.status, o.shipping_address, o.created_at, "
                   + "       i.product_id, i.product_name, i.unit_price, i.quantity "
                   + "FROM orders o JOIN order_items i ON i.order_id = o.id "
                   + "WHERE o.user_id = ? ORDER BY o.created_at DESC, o.id DESC, i.id";
        Map<Integer, Order> orders = new LinkedHashMap<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Order o = orders.get(id);
                    if (o == null) {
                        o = new Order();
                        o.setId(id);
                        o.setUserId(userId);
                        o.setTotal(rs.getBigDecimal("total"));
                        o.setStatus(rs.getString("status"));
                        o.setShippingAddress(rs.getString("shipping_address"));
                        o.setCreatedAt(rs.getTimestamp("created_at"));
                        orders.put(id, o);
                    }
                    OrderItem oi = new OrderItem();
                    oi.setProductId(rs.getInt("product_id")); // 0 if the product was later deleted
                    oi.setProductName(rs.getString("product_name"));
                    oi.setUnitPrice(rs.getBigDecimal("unit_price"));
                    oi.setQuantity(rs.getInt("quantity"));
                    o.getItems().add(oi);
                }
            }
        }
        return new ArrayList<>(orders.values());
    }

    private void rollbackQuietly(Connection con) {
        if (con == null) return;
        try { con.rollback(); } catch (SQLException ignored) { }
    }
}
