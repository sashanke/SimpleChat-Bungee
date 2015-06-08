package com.github.calenria.scbungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class SimpleChatListener implements Listener {

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (SimpleChat.hideStream) {
            return;
        }
        BungeeCord.getInstance().getLogger().info("Player Login: " + event.getConnection().getName());
        sendAll(ChatColor.translateAlternateColorCodes('&', String.format(SimpleChat.messages.getString("login"), event.getConnection().getName())));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (SimpleChat.hideStream) {
            return;
        }
        BungeeCord.getInstance().getLogger().info("Player Disconnect: " + event.getPlayer().getName());
        sendAll(ChatColor.translateAlternateColorCodes('&', String.format(SimpleChat.messages.getString("logout"), event.getPlayer().getName())));
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        String rawMessage = new String(event.getData());
        if (SimpleChat.debug) {
            BungeeCord.getInstance().getLogger().info("Tag: " + event.getTag() + " Recived plugin message: " + rawMessage);
        }

        if (!event.getTag().equals("BungeeCord")) {
            if (SimpleChat.debug) {
                BungeeCord.getInstance().getLogger().info("Kein BungeeCord, Verwerfe Tag: " + event.getTag());
            }
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subchannel = in.readUTF();

        if (subchannel.equals("SimpleChat")) {
            String pluginMessage = in.readUTF();
            StringTokenizer st = new StringTokenizer(pluginMessage, "@#@");
            String type = st.nextToken();
            if (type.equals("login") && st.hasMoreTokens()) {
                String playerName = st.nextToken();
                if (st.hasMoreTokens()) {
                    String tabListName = st.nextToken();
                    ProxiedPlayer pPlayer = BungeeCord.getInstance().getPlayer(playerName);
                    if (pPlayer != null) {
                        String server = pPlayer.getServer().getInfo().getName().substring(0, 1);
                        tabListName = ChatColor.translateAlternateColorCodes('&', server + " " + tabListName);
                        if (tabListName.length() >= 16) {
                            tabListName = tabListName.substring(0, 16);
                        }
                        pPlayer.setDisplayName(tabListName);
                    }

                    Map<String, ServerInfo> servers = BungeeCord.getInstance().getServers();
                    for (Entry<String, ServerInfo> server : servers.entrySet()) {
                        sendPluginMessage("@#@ping@#@ping", server);
                    }

                }
            } else if (type.equals("pm") && st.hasMoreTokens()) {
                @SuppressWarnings("unused")
                String serverName = st.nextToken();
                @SuppressWarnings("unused")
                String channel = st.nextToken();
                String from = st.nextToken();
                String to = st.nextToken();
                String message = st.nextToken();
                while (st.hasMoreTokens()) {
                    message += "@" + st.nextToken();
                }
                ProxiedPlayer fromPlayer = BungeeCord.getInstance().getPlayer(from);
                ProxiedPlayer toPlayer = BungeeCord.getInstance().getPlayer(to);
                if (toPlayer != null) {
                    toPlayer.sendMessage(message);
                    if (fromPlayer != null) {
                        fromPlayer.sendMessage(message);
                        sendPluginMessage("@#@wp@#@" + fromPlayer.getName() + "@#@" + toPlayer.getName(), toPlayer.getServer());
                    }
                } else {
                    if (fromPlayer != null) {
                        fromPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                "&4Fehler beim Ermitteln des Chat Partners. Offline oder Verschrieben? (" + to + ")"));
                    }
                }

            } else {
                String serverName = st.nextToken();
                Map<String, ServerInfo> servers = BungeeCord.getInstance().getServers();
                for (Entry<String, ServerInfo> server : servers.entrySet()) {
                    if (!server.getKey().equals(serverName) && !type.equals("spy")) {
                        sendPluginMessage(pluginMessage, server);
                    }
                    if (type.equals("spy")) {
                        sendPluginMessage(pluginMessage, server);
                    }
                }
            }

        }
    }

    private void sendPluginMessage(String pluginMessage, Entry<String, ServerInfo> server) {
        if (SimpleChat.debug) {
            BungeeCord.getInstance().getLogger().info("[perUser] Sending Message to: " + server.getKey());
        }
        List<ProxiedPlayer> pPlayers = new ArrayList<ProxiedPlayer>(server.getValue().getPlayers());
        if (!pPlayers.isEmpty()) {
            ProxiedPlayer pPlayer = pPlayers.get(0);
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("SimpleChat");
            out.writeUTF(pluginMessage);
            if (SimpleChat.debug) {
                BungeeCord.getInstance().getLogger()
                        .info("[perUser] Sending Message to: " + pPlayers.get(0).getName() + " Message: " + out.toByteArray().toString());
            }
            pPlayer.getServer().sendData("BungeeCord", out.toByteArray());
        } else {
            BungeeCord.getInstance().getLogger().info("No Player found on Server: " + server.getKey());
        }
    }

    private void sendPluginMessage(String pluginMessage, Server server) {
        if (SimpleChat.debug) {
            BungeeCord.getInstance().getLogger().info("[perServer] Sending Message to: " + server.getInfo().getName());
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("SimpleChat");
        out.writeUTF(pluginMessage);
        server.sendData("BungeeCord", out.toByteArray());
    }

    private void sendAll(String msg) {
        List<ProxiedPlayer> pPlayers = new ArrayList<ProxiedPlayer>(BungeeCord.getInstance().getPlayers());
        if (!pPlayers.isEmpty()) {
            for (ProxiedPlayer proxiedPlayer : pPlayers) {
                if (SimpleChat.debug) {
                    BungeeCord.getInstance().getLogger().info("Sending Message to: " + proxiedPlayer.getName());
                }
                proxiedPlayer.sendMessage(msg);
            }
        }
    }
}
