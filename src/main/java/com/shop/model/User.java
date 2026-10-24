package com.shop.model;

import java.io.Serializable;

/** Authenticated principal stored in the HttpSession. The password hash is never placed in the session. */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private int id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAdmin() { return ROLE_ADMIN.equals(role); }

    /** Copy safe for session storage: identical, minus the credential hash. */
    public User toSessionUser() {
        User u = new User();
        u.id = id; u.fullName = fullName; u.email = email; u.role = role;
        return u;
    }
}
