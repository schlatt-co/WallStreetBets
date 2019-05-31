package io.github.jroy.wallstreetbets.sql;

import io.github.jroy.wallstreetbets.WallStreetBets;
import io.github.jroy.wallstreetbets.utils.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLManager {

  private Connection connection;

  public SQLManager() throws SQLException, ClassNotFoundException {
    Logger.log("SQLManager: Logging in...");
    connect();
    Logger.log("SQLManager: Logged in!");
    Logger.log("SQLManager: Loading tables...");
    loadTables();
    Logger.log("SQLManager: Loaded tables!");
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
