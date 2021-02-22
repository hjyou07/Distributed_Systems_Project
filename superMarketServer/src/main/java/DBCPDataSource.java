import org.apache.commons.dbcp2.*;

public class DBCPDataSource {
  private static BasicDataSource dataSource;

  // NEVER store sensitive information below in plain text! TODO: figure out the fucking system properties
  private static final String HOST_NAME = "127.0.0.1"; /* System.getProperty("MySQL_IP_ADDRESS"); */
  private static final String PORT = "3306"; /* System.getProperty("MySQL_PORT"); */
  private static final String DATABASE = "SuperMarketDB";
  private static final String USERNAME = "heej"; /* System.getProperty("DB_USERNAME"); */
  private static final String PASSWORD = "letmein"; /* System.getProperty("DB_PASSWORD"); */

  static {
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    // connection url format: protocol//[hosts]/[database]?[properties]
    // protocol - jdbc:mysql: is for ordinary and basic JDBC failover connections
    // there's also jdbc:mysql:loadbalance: for load-balancing JDBC connections, would I need it?
    // properties are preceded by ? and written as key=value pairs separated by the symbol &
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    System.out.println(url);
    dataSource.setUrl(url);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    // The initial number of connections that are created when the pool is started
    dataSource.setInitialSize(10);
    // The maximum number of active connections that can be allocated from this pool at the same time, or negative for no limit.
    dataSource.setMaxTotal(60);
    // TODO: See if I should configure any other parameters, such as removeAbandonedOnBorrrow
    // A connection will be considered abandoned if it has been idle for 60 seconds
    //dataSource.setRemoveAbandonedTimeout(60);
    // Specifies the number of milliseconds to sleep between runs of the idle object evictor thread
    // needed for removeAbandonedOnMaintenance
    //dataSource.setTimeBetweenEvictionRunsMillis(30000);
    // Abandoned connections are removed each time a connection is borrowed from the pool
    //dataSource.setRemoveAbandonedOnBorrow(true);
    //dataSource.setRemoveAbandonedOnMaintenance(true);
  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }
}
