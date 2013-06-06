package com.norcode.bukkit.metalchat;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetalChat extends JavaPlugin {

    ConfigAccessor messages;
    public Chat vaultChat;
    private HashMap<String, Object> consoleMetadata = new HashMap<String, Object>();

    public void onEnable() {
        setupVault();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        messages = new ConfigAccessor(this, "messages.yml");
        messages.getConfig().options().copyDefaults(true);
        messages.saveConfig();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginCommand("tell").setExecutor(new PMCommand(this));
        getServer().getPluginCommand("reply").setExecutor(new ReplyCommand(this));
    }

    private void setupVault() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            vaultChat = chatProvider.getProvider();
        }
    }

    public String getMsg(String key, Object... args) {
        String tpl = messages.getConfig().getString(key);
        if (tpl == null) {
            tpl = "[" + key + "] ";
            for (int i=0;i< args.length;i++) {
                tpl += "{"+i+"}, ";
            }
        }
        return new MessageFormat(ChatColor.translateAlternateColorCodes('&', tpl)).format(args);
    }

    /**
     * When a player prefixes a message with a single @ followed by a space, it will be sent as a reply
     * to the last PM they sent or received.
     * @param player
     * @return the recipient of a single @ message.
     */
    public Player getPMReplyTo(Player player) {
        if (!player.hasMetadata(MetaKeys.PM_REPLY_TO)) {
            return null;
        }
        String recip = player.getMetadata(MetaKeys.PM_REPLY_TO).get(0).asString();
        Player p = getServer().getPlayer(recip);
        return p;
    }

    private static final Pattern formatPattern = Pattern.compile("(\\{\\{[^}]+}})");
    public String formatMessage(String format, HashMap<String, String> data) {
        String args = "";
        for (Map.Entry<String, String> e: data.entrySet()) {
            args+=e.getKey() + "=" + e.getValue() + ",";
        }
        getLogger().info("FormatMessage:args:" + args);
        StringBuffer sb = new StringBuffer();
        format = ChatColor.translateAlternateColorCodes('&', format);
        Matcher m = formatPattern.matcher(format);
        String repString;
        while (m.find())
        {
            String varName = m.group(1).substring(2,m.group(1).length()-2).toLowerCase().trim();
            if (data != null) {
                repString = data.get(varName);
                if (repString == null) {
                    repString = "";
                } else {
                    repString = ChatColor.translateAlternateColorCodes('&', repString);
                }
            } else {
                repString = "";
            }
            m.appendReplacement(sb, repString);


        }
        m.appendTail(sb);
        return sb.toString();
    }

    public void assignChatMeta(Player player) {
        HashMap<String, String> meta = new HashMap<String, String>();
        String group = vaultChat.getPrimaryGroup(player);
        getLogger().info("assigning chat meta for player " + player + " in group " + group);
        if (group != null) {
            meta.put("groupprefix", vaultChat.getGroupPrefix((String) null, group));
            meta.put("groupsuffix", vaultChat.getGroupSuffix((String) null, group));
        }
        meta.put("playerprefix", vaultChat.getPlayerPrefix((String) null, player.getName()));
        meta.put("playersuffix", vaultChat.getPlayerSuffix((String) null, player.getName()));
        getLogger().info("assigned meta " + meta);
        player.setMetadata(MetaKeys.CHAT_FORMAT_DATA, new FixedMetadataValue(this, meta));
    }

    public String formatIncomingPrivateMessage(CommandSender commandSender, CommandSender target, String message) {
        String fmt = getConfig().getString("incoming-pm-format");
        String formatted = formatMessage(fmt, (HashMap<String, String>) getMetaObject(commandSender, MetaKeys.CHAT_FORMAT_DATA));
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (!commandSender.hasPermission("metalchat.colors")) {
            message = ChatColor.stripColor(message);
        }
        return MessageFormat.format(formatted, getDisplayName(commandSender), target.getName(), message);
    }

    public String formatOutgoingPrivateMessage(CommandSender commandSender, CommandSender target, String message) {
        String fmt = getConfig().getString("outgoing-pm-format");
        String formatted = formatMessage(fmt, (HashMap<String, String>) getMetaObject(target, MetaKeys.CHAT_FORMAT_DATA));
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (!commandSender.hasPermission("metalchat.colors")) {
            message = ChatColor.stripColor(message);
        }
        return MessageFormat.format(formatted, getDisplayName(commandSender), target.getName(), message);
    }

    public String formatChatMessage(CommandSender sender, String message) {
        String fmt = getConfig().getString("global-chat-format");
        getLogger().info("Formatting: " + fmt);
        String formatted = formatMessage(fmt, (HashMap<String, String>) getMetaObject(sender, MetaKeys.CHAT_FORMAT_DATA));
        getLogger().info("formatted: " + formatted);
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (!sender.hasPermission("metalchat.colors")) {
            message = ChatColor.stripColor(message);
        }
        return MessageFormat.format(formatted, getDisplayName(sender), message);
    }

    private String getDisplayName(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getDisplayName();

        }
        return sender.getName();
    }

    public void setMeta(CommandSender player, String key, Object value) {
        if (player instanceof Player) {
            ((Player) player).setMetadata(key, new FixedMetadataValue(this, value));
        } else if (player instanceof ConsoleCommandSender) {
            consoleMetadata.put(key, value);
        } else {
            getLogger().warning("Unknown command sender: " + player);
        }
    }

    public Object getMetaObject(CommandSender player, String key) {
        if (player instanceof Player) {
            if (((Player) player).hasMetadata(key)) {
                return ((Player) player).getMetadata(key).get(0).value();
            }
        } else if (player instanceof ConsoleCommandSender) {
            return consoleMetadata.get(key);
        }
        return null;
    }

    public String getMetaString(CommandSender player, String key) {
        return (String) getMetaObject(player, key);
    }
}
