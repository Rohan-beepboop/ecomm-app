package com.shop.dao;

import com.shop.model.Category;
import com.shop.model.Product;
import com.shop.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Catalog queries (search/filter) plus admin CRUD. */
public class ProductDAO {

    private static final String BASE =
        "SELECT p.id, p.category_id, c.name AS category_name, p.name, p.description, p.price, p.stock, p.image_url "
      + "FROM products p JOIN categories c ON c.id = p.category_id ";

    /**
     * Keyword + category search.
     * The SQL string is assembled ONLY from fixed literals chosen by program logic; every user-supplied
     * value is bound through "?" placeholders, so the dynamic WHERE clause stays injection-proof.
     */
    public List<Product> search(String keyword, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (p.name LIKE ? OR p.description LIKE ?) ");
            String like = "%" + escapeLike(keyword.trim()) + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        sql.append("ORDER BY p.id DESC");

        List<Product> result = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Product findById(int id) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(BASE + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Category> findAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name FROM categories ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Category(rs.getInt("id"), rs.getString("name")));
        }
        return list;
    }

    public int insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (category_id, name, description, price, stock, image_url) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id=?, name=?, description=?, price=?, stock=?, image_url=? WHERE id=?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /** Past orders keep their snapshot rows because order_items.product_id is ON DELETE SET NULL. */
    public boolean delete(int id) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, Product p) throws SQLException {
        ps.setInt(1, p.getCategoryId());
        ps.setString(2, p.getName());
        ps.setString(3, p.getDescription());
        ps.setBigDecimal(4, p.getPrice());
        ps.setInt(5, p.getStock());
        ps.setString(6, p.getImageUrl());
    }

    /** Neutralises the LIKE wildcards so a user typing "50%" searches for the literal text. */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setImageUrl(rs.getString("image_url"));
        return p;
    }
}
