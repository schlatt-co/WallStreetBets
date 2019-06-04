package io.github.jroy.wallstreetbets.sql;

import io.github.jroy.wallstreetbets.WallStreetBets;
import io.github.jroy.wallstreetbets.sql.model.Company;
import io.github.jroy.wallstreetbets.sql.model.Shareholder;
import io.github.jroy.wallstreetbets.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLManager {

  private Connection connection;

  private static final String SELECT_COMPANY = "SELECT * FROM `companies` WHERE callsign = ?;";
  private static final String INSERT_COMPANY = "INSERT INTO `companies` (callsign, name, owner) VALUES (?, ?, ?);";
  private static final String INSERT_COMPANY_MEMBER = "INSERT INTO `members` (member, callsign) VALUES (?, ?);";
  private static final String INSERT_COMPANY_SHAREHOLDER = "INSERT INTO `shares` (shareholder, callsign) VALUES (?, ?);";

  private static final String SELECT_SHARES = "SELECT * FROM `shares` WHERE callsign = ?;";

  public SQLManager() throws SQLException, ClassNotFoundException {
    Logger.log("SQLManager: Logging in...");
    connect();
    Logger.log("SQLManager: Logged in!");
    Logger.log("SQLManager: Loading tables...");
    loadTables();
    Logger.log("SQLManager: Loaded tables!");
  }

  /**
   * Creates a company and adds it to the database.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param name The name of the company.
   * @param owner The UUID of the owner of the company.
   * @return True if success.
   */
  public boolean createCompany(String callsign, String name, UUID owner) {
    try {
      PreparedStatement statement = connection.prepareStatement(INSERT_COMPANY);
      statement.setString(1, callsign);
      statement.setString(2, name);
      statement.setString(3, owner.toString());
      statement.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Adds a member to a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param member The UUID of the user being added to the company.
   * @return True if success.
   */
  public boolean addCompanyMember(String callsign, UUID member) {
    try {
      PreparedStatement statement = connection.prepareStatement(INSERT_COMPANY_MEMBER);
      statement.setString(1, member.toString());
      statement.setString(2, callsign);
      statement.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Rewards a share to a shareholder.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param shareholder The UUID of the target shareholder.
   * @return True is success.
   */
  public boolean addShareholder(String callsign, UUID shareholder) {
    try {
      PreparedStatement statement = connection.prepareStatement(INSERT_COMPANY_SHAREHOLDER);
      statement.setString(1, shareholder.toString());
      statement.setString(2, callsign);
      statement.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Validates if a callsign is with a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @return True is valid ticker.
   */
  public boolean isCompany(String callsign) {
    try {
      PreparedStatement statement = connection.prepareStatement(SELECT_COMPANY);
      statement.setString(1, callsign);
      return statement.executeQuery().next();
    } catch (SQLException e) {
      return false;
    }
  }

  /**
   * Fetches details of a company and provides helper methods to set data.
   * @param callsign The ticker for company, usually 3 english characters.
   * @return The company object.
   * @throws SQLException If the company wasn't found.
   */
  public Company getCompany(String callsign) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT_COMPANY);
    statement.setString(1, callsign);
    ResultSet set = statement.executeQuery();
    if (!set.next()) {
      throw new SQLException("Invalid Callsign");
    }
    return new Company(this, set.getInt("id"), set.getString("callsign"), UUID.fromString(set.getString("name")), set.getInt("active_shares"), set.getInt("total_shares"), getShareholders(callsign));
  }

  public Shareholder[] getShareholders(String callsign) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT_SHARES);
    statement.setString(1, callsign);
    ResultSet set = statement.executeQuery();
    List<Shareholder> shareholders = new ArrayList<>();
    while (set.next()) {
      shareholders.add(new Shareholder(this, set.getInt("id"), UUID.fromString(set.getString("shareholder")), set.getString("callsign")));
    }
    return shareholders.toArray(Shareholder[]::new);
  }

  private void connect() throws SQLException, ClassNotFoundException {
    if (connection != null && !connection.isClosed()) {
      return;
    }
    synchronized (this) {
      if (connection != null && !connection.isClosed()) {
        return;
      }
      Class.forName("com.mysql.cj.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/stocks?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=GMT",
          WallStreetBets.getInstance().getConfig().getString("mysql.username"),
          WallStreetBets.getInstance().getConfig().getString("mysql.password"));
    }
  }

  private void loadTables() throws SQLException {
    connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `companies` ( `id` INT(50) NOT NULL AUTO_INCREMENT , `callsign` VARCHAR(255) NOT NULL , `name` VARCHAR(255) NOT NULL , `owner` VARCHAR(255) NOT NULL , `active_shares` INT(255) NOT NULL DEFAULT '0' , `total_shares` INT(255) NOT NULL DEFAULT '0' , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
    connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `shares` ( `id` INT(50) NOT NULL AUTO_INCREMENT , `shareholder` VARCHAR(255) NOT NULL , `callsign` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
    connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `members` ( `id` INT(50) NOT NULL AUTO_INCREMENT , `member` VARCHAR(255) NOT NULL , `callsign` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
  }
}
