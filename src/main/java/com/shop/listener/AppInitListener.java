package com.shop.listener;

import com.shop.dao.UserDAO;
import com.shop.model.User;
import com.shop.util.DBUtil;
import com.shop.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeds the administrator account on application start-up if it does not exist yet.
 * Done in code (rather than a pasted hash in schema.sql) so the password is hashed with the exact
 * algorithm used at login and the credentials stay configurable in db.properties.
 */
@WebListener
public class AppInitListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String email = DBUtil.getProperty("admin.email", "admin@shop.com").toLowerCase();
        try {
            UserDAO dao = new UserDAO();
            if (dao.findByEmail(email) == null) {
                User admin = new User();
                admin.setFullName(DBUtil.getProperty("admin.name", "Store Administrator"));
                admin.setEmail(email);
                admin.setPasswordHash(PasswordUtil.hash(DBUtil.getProperty("admin.password", "Admin@123")));
                admin.setRole(User.ROLE_ADMIN);
                dao.insert(admin);
                LOG.info("Seeded administrator account: " + email);
            }
        } catch (SQLException e) {
            // Do not abort deployment: the site can still serve; the log tells the operator what to fix.
            LOG.log(Level.SEVERE, "Could not seed admin account. Is MySQL running and db.properties correct?", e);
        }
    }
}
