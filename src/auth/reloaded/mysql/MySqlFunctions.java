package auth.reloaded.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.mysql.jdbc.Statement;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import auth.reloaded.AuthReloaded;
import auth.reloaded.Hash;

public class MySqlFunctions {
  private static MySql mysql = AuthReloaded.mysql;
  private static String table = mysql.table;

  public static boolean playerHasEntry(Player p) {
    UUID uuid = p.getUniqueId();

    try {
      PreparedStatement hasEntry = mysql.getConnection().prepareStatement("SELECT * FROM `" + table + "` WHERE uuid=?");
      hasEntry.setString(1, uuid.toString());

      ResultSet results = hasEntry.executeQuery();
      if (results.next())
        return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return false;
  }

  public static boolean loginPlayer(Player p, String password) {
    UUID uuid = p.getUniqueId();

    PreparedStatement hasEntry;
    try {
      hasEntry = mysql.getConnection().prepareStatement("SELECT * FROM `" + table + "` WHERE uuid=?");
    
      hasEntry.setString(1, uuid.toString());

      ResultSet results = hasEntry.executeQuery();
      results.next();

      if (!playerHasEntry(p)) {
        p.sendMessage(ChatColor.RED + "You are not registered.\nUse " + ChatColor.BOLD + "/register <password> <confirm-password> " + ChatColor.RED + "instead.");
        return false;
      }

      p.sendMessage(results.getString("password"));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    p.sendMessage(ChatColor.RED + "Something went wrong. Please contact the server administrator.");
    return false;
  }

  public static boolean registerPlayer(Player p, String password_hash, String password_salt, String ip_hash, CommandSender player_to_send_message_to) {
    UUID uuid = p.getUniqueId();

    PreparedStatement hasEntry;
    try {
      hasEntry = mysql.getConnection().prepareStatement("SELECT * FROM `" + table + "` WHERE uuid=?");
    
      hasEntry.setString(1, uuid.toString());

      ResultSet results = hasEntry.executeQuery();
      results.next();

      if (playerHasEntry(p)) {
        player_to_send_message_to.sendMessage(ChatColor.RED + (p == player_to_send_message_to ? "You are already registered.\nUse " + ChatColor.BOLD + "/login <password>" + ChatColor.RED + " instead." : "Player is already registered."));
        return false;
      }

      PreparedStatement create = mysql.getConnection().prepareStatement("INSERT INTO `" + table + "` VALUES (default,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
      
      create.setString(1, uuid.toString());
      create.setString(2, p.getDisplayName());
      create.setString(3, p.getName());
      create.setString(4, password_hash + "." + password_salt);
      create.setString(5, ip_hash);
      create.setLong(6, System.currentTimeMillis() / 1000L);
      create.setString(7, null);

      create.executeUpdate();

      p.sendMessage(ChatColor.GREEN + "You have been successfully registered.");
      if (player_to_send_message_to != p) player_to_send_message_to.sendMessage(ChatColor.GREEN + "Player has been successfully registered.");
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    player_to_send_message_to.sendMessage(ChatColor.RED + "Something went wrong. Please contact the server administrator.");
    return false;
  }

  public static Integer getIps(String ip) {
    PreparedStatement ips;
    try {
      ips = mysql.getConnection().prepareStatement("SELECT count(*) FROM `" + table + "` WHERE ip=?");
      ips.setString(1, Hash.hash(ip));

      ResultSet results = ips.executeQuery();

      if (results.next())
        return results.getInt(1);
      else return 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return -1;
  }
}
