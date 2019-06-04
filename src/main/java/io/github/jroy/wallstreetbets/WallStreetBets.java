package io.github.jroy.wallstreetbets;

import io.github.jroy.wallstreetbets.sql.SQLManager;
import io.github.jroy.wallstreetbets.utils.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class WallStreetBets extends JavaPlugin {

  @Getter
  private static WallStreetBets instance;

  private SQLManager sqlManager;

  @Override
  public void onEnable() {
    Logger.log("Loading WallStreetBets...");
    instance = this;
    loadConfig();
    Logger.log("Loading SQLManager...");
    try {
      sqlManager = new SQLManager();
    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
  }

  private void loadConfig() {
    getConfig().addDefault("mysql.username", "");
    getConfig().addDefault("mysql.password", "");
    getConfig().options().copyDefaults(true);
    saveConfig();
    reloadConfig();
  }
}
