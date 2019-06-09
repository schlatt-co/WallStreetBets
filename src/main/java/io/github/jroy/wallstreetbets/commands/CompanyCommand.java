package io.github.jroy.wallstreetbets.commands;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import io.github.jroy.wallstreetbets.sql.SQLManager;
import io.github.jroy.wallstreetbets.sql.model.Company;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.sql.SQLException;

public class CompanyCommand implements CommandExecutor {

  private SQLManager sqlManager;

  public CompanyCommand(SQLManager sqlManager) {
    this.sqlManager = sqlManager;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof ConsoleCommandSender) {
      sender.sendMessage("This is a player only command");
      return true;
    }
    Player player = (Player) sender;
    if (args.length == 0) {
      String callsign = sqlManager.getCallsign(player.getUniqueId());
      sender.sendMessage(callsign == null ? "Warning: You don't have a company! Please ask an admin to create one!\n" : "Your current company callsign is: " + callsign + "\n");
      sender.sendMessage(help());
      return true;
    }

    String callsign = args[0];
    if (!sqlManager.isCompany(callsign)) {
      sender.sendMessage("Invalid company callsign!");
      return true;
    }

    try {
      Company company = sqlManager.getCompany(callsign);
      boolean isMember = company.getOwnerUuid().toString().equals(player.getUniqueId().toString()) || sqlManager.isMember(company.getCallsign(), player.getUniqueId());
      if (args.length == 1) {
        sender.sendMessage(
            "Your Company Info:" +
                "\nID: " + company.getId() +
                "\nCallsign: " + company.getCallsign() +
                "\nOwner UUID: " + company.getOwnerUuid() +
                "\nMember Count: " + company.getMembers().size() +
                "\nShareholder Count: " + company.getShareholders().size() +
                "\nTotal Shares: " + company.getTotalShares() +
                "\nWorth: " + company.getWorth());
        return true;
      }
      if (args.length != 3) {
        sender.sendMessage(help());
        return true;
      }
      switch (args[1]) {
        case "deposit": {
          if (!isMember) {
            sender.sendMessage("You are not a member of this company");
            return true;
          }
          if (!StringUtils.isNumeric(args[2]) && args[2].startsWith("-")) {
            sender.sendMessage("Invalid or negative amount!");
            return true;
          }

          BigDecimal amount = new BigDecimal(args[2]);
          if (!Economy.hasEnough(player.getName(), amount)) {
            sender.sendMessage("You do not have enough money to deposit into this company.");
            return true;
          }

          if (!company.addWorth(Integer.valueOf(args[2]))) {
            sender.sendMessage("Error while adding worth to company, your money hasn't been taken.");
            return true;
          }
          Economy.substract(player.getName(), amount);
          sender.sendMessage("Deposited money!");
          return true;
        }
        case "withdraw": {
          if (!isMember) {
            sender.sendMessage("You are not a member of this company");
            return true;
          }
          if (!StringUtils.isNumeric(args[2]) && args[2].startsWith("-")) {
            sender.sendMessage("Invalid or negative amount!");
            return true;
          }

          int amount = Integer.parseInt(args[2]);
          if (company.getWorth() < amount) {
            sender.sendMessage("Company doesn't have enough money!");
            return true;
          }
          if (!company.addWorth(Integer.valueOf("-" + amount))) {
            sender.sendMessage("Error while adding worth to company, your money hasn't been taken.");
            return true;
          }
          Economy.add(player.getName(), new BigDecimal(amount));
          sender.sendMessage("Withdrew money!");
          return true;
        }
        case "addmember": {
          if (!isMember) {
            sender.sendMessage("You are not a member of this company");
            return true;
          }
          Player newMember = Bukkit.getPlayer(args[2]);
          if (newMember == null) {
            sender.sendMessage("Player is not online!");
            return true;
          }

          if (!company.addMember(newMember.getUniqueId())) {
            sender.sendMessage("Error while adding member to company!");
            return true;
          }
          sender.sendMessage("Added user to company");
          newMember.sendMessage("You are now a member of the company: " + company.getCallsign() + ": " + company.getName());
          return true;
        }
        case "removemember": {
          if (!isMember) {
            sender.sendMessage("You are not a member of this company");
            return true;
          }
          Player member = Bukkit.getPlayer(args[2]);
          if (member == null) {
            sender.sendMessage("Player is not online!");
            return true;
          }

          sqlManager.removeMember(company.getCallsign(), member.getUniqueId());
          sender.sendMessage("Added user to company");
          member.sendMessage("You are no longer a member of the company: " + company.getCallsign() + ": " + company.getName());
          return true;
        }
        case "setshares": {
          if (!isMember) {
            sender.sendMessage("You are not a member of this company");
            return true;
          }
          if (!StringUtils.isNumeric(args[2]) && args[2].startsWith("-")) {
            sender.sendMessage("Invalid or negative amount!");
            return true;
          }

          if (!sqlManager.setTotalShares(company.getCallsign(), Integer.valueOf(args[2]))) {
            sender.sendMessage("Error while setting the total share count!");
            return true;
          }
          sender.sendMessage("Updated the total share count!");
          return true;
        }
        case "buyshares": {
        }
      }
      return true;
    } catch (SQLException | UserDoesNotExistException | NoLoanPermittedException e) {
      e.printStackTrace();
      sender.sendMessage("There was an error while preforming this command!");
      return true;
    }
  }

  private String help() {
    return "Correct Usage:\n" +
        "/company <callsign> - Info about a company\n" +
        "/company <callsign> deposit <amount> - Deposits money into an account\n" +
        "/company <callsign> withdraw <amount> - Withdraws money from an account\n" +
        "/company <callsign> addmember <username> - Adds a user to a company\n" +
        "/company <callsign> removemember <username> - Removes a user from a company\n" +
        "/company <callsign> setshares <share count> - Sets the share cap of a company\n" +
        "/company <callsign> buyshares <share count> - Buys an amount of shares from a company\n" +
        "/company <callsign> sellshares <share count> - Sells an amount of shares from a company";
  }
}
