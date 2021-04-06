import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DataSource {
  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource dataSource;

  private static final String HOST_NAME = "DB_HOST";
  private static final String PORT = "3306";
  private static final String DATABASE = "SuperMarketDB";
  private static final String USERNAME = "DB_USERNAME";
  private static final String PASSWORD = "DB_PASSWORD";
  private static final String FILE_PATH = "/home/ec2-user/purchase/config.properties";
  private static final boolean isLocal = false;

  static {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    try (InputStream input = new FileInputStream(FILE_PATH)) {
      Properties prop = new Properties();
      prop.load(input);
      String url = String
          .format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", prop.getProperty(HOST_NAME), PORT,
              DATABASE);
      config.setJdbcUrl(url);
      config.setUsername(prop.getProperty(USERNAME));
      config.setPassword(prop.getProperty(PASSWORD));
      config.addDataSourceProperty("cachePrepStmts", "true");
      config.addDataSourceProperty("prepStmtCacheSize", "250");
      config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      config.setMaximumPoolSize(64);
      config.setMaxLifetime(120000);
      dataSource = new HikariDataSource(config);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
    public static HikariDataSource getDataSource() {
    return dataSource;
  }

  public static Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
}
