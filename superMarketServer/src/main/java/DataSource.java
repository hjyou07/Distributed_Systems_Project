import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource dataSource;

  private static final String HOST_NAME = System.getProperty("SQL_IP");
  private static final String PORT = System.getProperty("SQL_PORT");
  private static final String DATABASE = "SuperMarketDB";
  private static final String USERNAME = System.getProperty("DB_USERNAME");
  private static final String PASSWORD = System.getProperty("DB_PASSWORD");

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    // System.out.println(url);
    // System.out.println(USERNAME);
    // System.out.println(PASSWORD);
    config.setJdbcUrl(url);
    config.setUsername(USERNAME);
    config.setPassword(PASSWORD);
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.setMaximumPoolSize(15);
    config.setMaxLifetime(120000);
    dataSource = new HikariDataSource(config);
  }

  public static HikariDataSource getDataSource() {
    return dataSource;
  }

  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
}
