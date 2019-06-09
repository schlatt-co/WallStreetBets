package io.github.jroy.wallstreetbets.commands;

import io.github.jroy.wallstreetbets.sql.SQLManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ManagerCommand implements CommandExecutor {

  private final SQLManager sqlManager;

  public ManagerCommand(SQLManager sqlManager) {
    this.sqlManager = sqlManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(help());
      return true;
    }

    switch (args[0]) {
      case "create": {
        if (args.length < 4) {
          sender.sendMessage(help());
          return true;
        }
        String callsign = args[1];
        if (sqlManager.isCompany(callsign)) {
          sender.sendMessage("Company already exists!");
          return true;
        }
        Player owner = Bukkit.getPlayer(args[2]);
        if (owner == null) {
          sender.sendMessage("Player is not online!");
          return true;
        }
        String name = StringUtils.join(Arrays.copyOfRange(args, 3, args.length), " ");

        if (sqlManager.createCompany(callsign, name, owner.getUniqueId())) {
          sender.sendMessage("Company created");
        } else {
          sender.sendMessage("Error while creating company!");
        }
      }
      case "delete": {
        if (args.length < 2) {
          sender.sendMessage(help());
          return true;
        }

        String callsign = args[1];
        if (!sqlManager.isCompany(callsign)) {
          sender.sendMessage("Company does not exist!");
          return true;
        }

        if (!sqlManager.deleteCompany(callsign)) {
          sender.sendMessage("Error while deleting company!");
          return false;
        }
        sqlManager.deleteCompanyMembers(callsign);
        sqlManager.deleteCompanyShareholders(callsign);
        sender.sendMessage("Company deleted!");
      }
    }
    return true;
  }

  private String help() {
    return "Correct Usages:\n\n/wallstreetbets create <callsign> <owner name> <company name>\n\n/wallstreetbets delete <callsign>";
  }
}
