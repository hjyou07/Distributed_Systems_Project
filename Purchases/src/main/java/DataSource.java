import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSource {
  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource dataSource;

  private static final String HOST_NAME = "localhost";
  private static final String PORT = "3306";
  private static final String DATABASE = "SuperMarketDB";
  private static final String USERNAME = "heej";
  private static final String PASSWORD = "letmein";

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
