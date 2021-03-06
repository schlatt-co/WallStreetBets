package io.github.jroy.wallstreetbets.sql;

import io.github.jroy.wallstreetbets.WallStreetBets;
import io.github.jroy.wallstreetbets.sql.model.Company;
import io.github.jroy.wallstreetbets.sql.model.Member;
import io.github.jroy.wallstreetbets.sql.model.Shareholder;
import io.github.jroy.wallstreetbets.utils.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLManager {

  private Connection connection;

  private static final String SELECT_COMPANY = "SELECT * FROM `companies` WHERE callsign = ?;";
  private static final String SELECT_SHARES = "SELECT * FROM `shares` WHERE callsign = ?;";
  private static final String SELECT_SHARE = "SELECT * FROM `shares` WHERE callsign = ? AND shareholder = ?;";
  private static final String SELECT_MEMBERS = "SELECT * FROM `members` WHERE callsign = ?;";
  private static final String SELECT_MEMBER = "SELECT * FROM `members` WHERE callsign = ? AND member = ?;";
  private static final String SELECT_COMPANY_FROM_UUID = "SELECT * FROM `companies` WHERE owner = ?;";
  private static final String INSERT_COMPANY = "INSERT INTO `companies` (callsign, name, owner) VALUES (?, ?, ?);";
  private static final String INSERT_COMPANY_MEMBER = "INSERT INTO `members` (member, callsign) VALUES (?, ?);";
  private static final String INSERT_COMPANY_SHAREHOLDER = "INSERT INTO `shares` (shareholder, callsign) VALUES (?, ?);";
  private static final String REMOVE_COMPANY = "DELETE FROM `companies` WHERE callsign = ?;";
  private static final String REMOVE_COMPANY_MEMBER = "DELETE FROM `members` WHERE callsign = ? AND member = ?;";
  private static final String REMOVE_COMPANY_SHAREHOLDER = "DELETE FROM `shares` WHERE callsign = ? AND member = ?;";
  private static final String REMOVE_ALL_COMPANY_MEMBERS = "DELETE FROM `members` WHERE callsign = ?;";
  private static final String REMOVE_ALL_COMPANY_SHAREHOLDERS = "DELETE FROM `shares` WHERE callsign = ?;";
  private static final String INCREMENT_COMPANY_WORTH = "UPDATE `companies` SET worth = worth + ? WHERE callsign = ?";
  private static final String UPDATE_COMPANY_SHARES = "UPDATE `companies` SET total_shares = ? WHERE callsign = ?";

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
   * Deletes a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @return True if success.
   */
  public boolean deleteCompany(String callsign) {
    try {
      PreparedStatement statement = connection.prepareStatement(REMOVE_COMPANY);
      statement.setString(1, callsign);
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
   * Purges all members from a company.
   * @param callsign The ticker for company, usually 3 english characters.
   */
  public void deleteCompanyMembers(String callsign) {
    try {
      PreparedStatement statement = connection.prepareStatement(REMOVE_ALL_COMPANY_MEMBERS);
      statement.setString(1, callsign);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
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
   * Purges all shareholders from a company.
   * @param callsign The ticker for company, usually 3 english characters.
   */
  public void deleteCompanyShareholders(String callsign) {
    try {
      PreparedStatement statement = connection.prepareStatement(REMOVE_ALL_COMPANY_SHAREHOLDERS);
      statement.setString(1, callsign);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Removes a member from a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param uuid The UUID of the player to remove.
   * @throws SQLException If the deletion was unsuccessful.
   */
  public void removeMember(String callsign, UUID uuid) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(REMOVE_COMPANY_MEMBER);
    statement.setString(1, callsign);
    statement.setString(2, uuid.toString());
    statement.executeUpdate();
  }

  /**
   * Removes a shareholder from a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param uuid The UUID of the player to remove.
   * @throws SQLException If the deletion was unsuccessful.
   */
  public void removeShareholder(String callsign, UUID uuid) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(REMOVE_COMPANY_SHAREHOLDER);
    statement.setString(1, callsign);
    statement.setString(2, uuid.toString());
    statement.executeUpdate();
  }

  /**
   * Validates if a player is a shareholder of a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param uuid The UUID of the player to validate.
   * @return True if valid shareholder.
   */
  public boolean isShareholder(String callsign, UUID uuid) {
    try {
      PreparedStatement statement = connection.prepareStatement(SELECT_SHARE);
      statement.setString(1, callsign);
      statement.setString(2, uuid.toString());
      return statement.executeQuery().next();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Validates if a player is a member of a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param uuid The UUID of the player to validate.
   * @return True if valid member.
   */
  public boolean isMember(String callsign, UUID uuid) {
    try {
      PreparedStatement statement = connection.prepareStatement(SELECT_MEMBER);
      statement.setString(1, callsign);
      statement.setString(2, uuid.toString());
      return statement.executeQuery().next();
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Validates if a callsign is with a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @return True if valid ticker.
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
   * Gets the callsign of a company from a UUID
   * @param uuid The UUID of the owner of the company.
   * @return The ticker for company, usually 3 english characters OR null if company doesn't exist
   */
  public String getCallsign(UUID uuid) {
    try {
      PreparedStatement statement = connection.prepareStatement(SELECT_COMPANY_FROM_UUID);
      statement.setString(1, uuid.toString());
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        return rs.getString("callsign");
      }
    } catch (SQLException ignored) {}
    return null;
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
    return new Company(this, set.getInt("id"), set.getString("callsign"), set.getString("name"), UUID.fromString(set.getString("name")), set.getInt("total_shares"), set.getInt("worth"), getShareholders(callsign), getMembers(callsign));
  }

  /**
   * Gets a list of all shareholders in a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @return The list of shareholders in a company
   * @throws SQLException If the company wasn't found.
   */
  public List<Shareholder> getShareholders(String callsign) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT_SHARES);
    statement.setString(1, callsign);
    ResultSet set = statement.executeQuery();
    List<Shareholder> shareholders = new ArrayList<>();
    while (set.next()) {
      shareholders.add(new Shareholder(this, set.getInt("id"), UUID.fromString(set.getString("shareholder")), set.getString("callsign")));
    }
    return shareholders;
  }

  /**
   * Gets a list of members in a company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @return The list of members in a company.
   * @throws SQLException If the company wasn't found.
   */
  public List<Member> getMembers(String callsign) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(SELECT_MEMBERS);
    statement.setString(1, callsign);
    ResultSet set = statement.executeQuery();
    List<Member> members = new ArrayList<>();
    while (set.next()) {
      members.add(new Member(this, set.getInt("id"), UUID.fromString(set.getString("member")), set.getString("callsign")));
    }
    return members;
  }

  /**
   * Adds a worth to a company
   * @param callsign The ticker for company, usually 3 english characters.
   * @param amount The amount of money to add to a company
   * @return True if success.
   */
  public boolean addWorth(String callsign, int amount) {
    try {
      PreparedStatement statement = connection.prepareStatement(INCREMENT_COMPANY_WORTH);
      statement.setInt(1, amount);
      statement.setString(2, callsign);
      statement.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Updates the total share count of the company.
   * @param callsign The ticker for company, usually 3 english characters.
   * @param count The amount of shares to cap the company at.
   * @return True if success.
   */
  public boolean setTotalShares(String callsign, int count) {
    try {
      PreparedStatement statement = connection.prepareStatement(UPDATE_COMPANY_SHARES);
      statement.setInt(1, count);
      statement.setString(2, callsign);
      statement.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
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
    connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `companies` ( `id` INT(50) NOT NULL AUTO_INCREMENT , `callsign` VARCHAR(255) NOT NULL , `name` VARCHAR(255) NOT NULL , `owner` VARCHAR(255) NOT NULL , `total_shares` INT(255) NOT NULL DEFAULT '0' , `worth` INT(255) NOT NULL DEFAULT '0' , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
    connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `shares` ( `id` INT(50) NOT NULL AUTO_INCREMENT , `shareholder` VARCHAR(255) NOT NULL , `callsign` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
    connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS `members` ( `id` INT(50) NOT NULL AUTO_INCREMENT , `member` VARCHAR(255) NOT NULL , `callsign` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
  }
}
