# Meridian Goods: Java Servlet E-Commerce Application

A course-end project: MVC + DAO architecture on Java Servlets/JSP, JDBC and MySQL, deployed on Apache Tomcat.

| Layer | Package / folder | Responsibility |
|---|---|---|
| Model | `com.shop.model` | Plain data objects (`User`, `Product`, `Cart`, `Order`...) |
| DAO | `com.shop.dao` | **All** JDBC and SQL; `PreparedStatement` everywhere, try-with-resources |
| Controller | `com.shop.controller` | Servlets: read request, call DAO, forward/redirect. No HTML, no SQL |
| Filters | `com.shop.filter` | CSRF + flash (`CoreRequestFilter`), login guard (`AuthFilter`), admin guard (`AdminFilter`) |
| View | `WEB-INF/views/*.jsp` | JSP + JSTL only (inside `WEB-INF`, so unreachable except via a controller) |

## 1. Prerequisites
* JDK 17+
* Apache Tomcat **10.1.x** (Jakarta EE 10, `jakarta.*` packages)
* MySQL 8.x
* Maven 3.8+ (bundled in Eclipse "for Java EE" and IntelliJ) for dependency download only

## 2. Create the database
```bash
mysql -u root -p < database/schema.sql
```
This creates `ecommerce_db`, all tables and 12 sample products in 4 categories.
The administrator account is created automatically the first time the app starts.

## 3. Configure credentials
Edit `src/main/resources/db.properties`: set `db.user`, `db.password` (and `db.url` if MySQL is not on `localhost:3306`).
Default admin login (change the password before any real use):

| Role | E-mail | Password |
|---|---|---|
| Admin | `admin@shop.com` | `Admin@123` |

Customers register themselves at `/register`.

## 4. Import the project
**Eclipse (Enterprise Java edition):** File > Import > Maven > *Existing Maven Projects* > select this folder.
Right-click the project > Run As > *Run on Server* > choose Tomcat v10.1.

**IntelliJ IDEA (Ultimate):** File > Open > select `pom.xml` > *Open as Project*.
Run > Edit Configurations > + > *Tomcat Server > Local* > Deployment tab > + > `ecommerce:war exploded`; set Application context to `/ecommerce`.

## 5. Deploy without an IDE
```bash
mvn clean package                         # produces target/ecommerce.war
cp target/ecommerce.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/startup.sh             # Windows: bin\startup.bat
```
Open **http://localhost:8080/ecommerce/**. Check `logs/catalina.out` for the line
`Seeded administrator account: admin@shop.com`.

## 6. Feature tour / test script
1. Browse `/catalog`, search ("coffee", "50%"), filter by category.
2. Add items as a guest; use +/- and Remove in `/cart`. Items out of stock cannot be added.
3. Click *Proceed to checkout*: you are redirected to log in/register, then returned to checkout.
4. Place the order: stock is decremented and the order appears in *My orders* (single DB transaction).
5. Log in as admin: you land on `/admin/products` (add, edit, delete). A customer visiting `/admin/products` receives **403**.

## 7. Security measures built in
* `PreparedStatement` for every query; LIKE wildcards escaped in search.
* PBKDF2-HMAC-SHA256 salted password hashes; constant-time compare; generic login error.
* Session-fixation defence (`changeSessionId`), HttpOnly cookie, cookie-only tracking.
* Role stored server-side in the session; role never accepted from a form.
* CSRF token on every POST; `c:out` escaping on all user data (XSS); open-redirect guard on `returnUrl`.
* Checkout re-reads prices and stock from the DB with `SELECT ... FOR UPDATE` inside a transaction (no overselling, no price tampering).
* Enable `<secure>true</secure>` in `web.xml` once served over HTTPS.

## 8. Using Tomcat 8 / 9 (`javax.*`)
1. In `pom.xml` use `javax.servlet:javax.servlet-api:4.0.1` (provided), `javax.servlet.jsp:javax.servlet.jsp-api:2.3.3` (provided) and `javax.servlet:jstl:1.2`; drop the two Jakarta JSTL artifacts and set `maven.compiler.release` to 11.
2. Replace imports: `sed -i 's/jakarta\./javax./g' $(grep -rl "jakarta\." src/main/java)`
3. In `taglibs.jspf` use URIs `http://java.sun.com/jsp/jstl/core`, `.../fmt`, `.../functions`; `admin/products.jsp` also needs the `fn` URI changed; `index.jsp` the `core` URI.
4. In `web.xml` switch to the `javaee` 4.0 schema header, and in `error.jsp` rename the `jakarta.servlet.error.*` attributes to `javax.servlet.error.*`.

## 9. Troubleshooting
| Symptom | Cause / fix |
|---|---|
| `Communications link failure` | MySQL not running or wrong `db.url` |
| `Access denied for user` | Wrong `db.user` / `db.password` |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Maven dependencies not resolved; run *Maven > Update Project* |
| JSP shows raw `${...}` or taglib error | JSTL jars missing from `WEB-INF/lib` (Maven provides them) or Tomcat 9 with jakarta URIs |
| 404 on `/ecommerce/...` | Context path differs; check the deployed WAR name |
| 403 "Invalid or missing security token" | Session expired; reload the page |
