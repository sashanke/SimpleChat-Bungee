package com.github.calenria.scbungee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;

import com.google.common.eventbus.Subscribe;

public class SimpleChatListener implements Listener {

    private static Logger log = Logger.$();

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (SimpleChat.hideStream) {
            return;
        }
        log.info("Player Login: " + event.getConnection().getName());
        sendAll(ChatColor.translateAlternateColorCodes('&', String.format(SimpleChat.messages.getString("login"), event.getConnection().getName())));
    }

    @Subscribe
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (SimpleChat.hideStream) {
            return;
        }
        log.info("Player Disconnect: " + event.getPlayer().getName());
        sendAll(ChatColor.translateAlternateColorCodes('&', String.format(SimpleChat.messages.getString("logout"), event.getPlayer().getName())));
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        String pluginMessage = new String(event.getData());
        if (SimpleChat.debug) {
            log.info("Recived plugin message: " + pluginMessage);
        }
        if (!event.getTag().equals("SimpleChat"))
            return;

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
                    fromPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Fehler beim Ermitteln des Chat Partners. Offline oder Verschrieben? (" + to + ")"));
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

    private void sendPluginMessage(String pluginMessage, Entry<String, ServerInfo> server) {
        if (SimpleChat.debug) {
            log.info("Sending Message to: " + server.getKey());
        }
        List<ProxiedPlayer> pPlayers = new ArrayList<ProxiedPlayer>(server.getValue().getPlayers());
        if (!pPlayers.isEmpty()) {
            ProxiedPlayer pPlayer = pPlayers.get(0);
            pPlayer.sendData("SimpleChat", pluginMessage.getBytes());
        }
    }

    private void sendPluginMessage(String pluginMessage, Server server) {
        if (SimpleChat.debug) {
            log.info("Sending Message to: " + server.getInfo().getName());
        }
        server.sendData("SimpleChat", pluginMessage.getBytes());
    }

    private void sendAll(String msg) {
        List<ProxiedPlayer> pPlayers = new ArrayList<ProxiedPlayer>(BungeeCord.getInstance().getPlayers());
        if (!pPlayers.isEmpty()) {
            for (ProxiedPlayer proxiedPlayer : pPlayers) {
                if (SimpleChat.debug) {
                    log.info("Sending Message to: " + proxiedPlayer.getName());
                }
                proxiedPlayer.sendMessage(msg);
            }
        }
    }
}
