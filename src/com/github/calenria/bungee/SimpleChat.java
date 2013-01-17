package com.github.calenria.bungee;

import java.util.Map.Entry;
import java.util.Set;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Logger;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.plugin.JavaPlugin;
import net.md_5.bungee.plugin.PluginMessageEvent;
import net.md_5.bungee.plugin.PluginMessageEvent.Destination;

public class SimpleChat extends JavaPlugin {
    private Logger log;

    @Override
    public void onEnable() {
        log = Logger.$();

        // Register our global plugin channel
        BungeeCord.instance.registerPluginChannel("SimpleChat");

        log.info("[SimpleChat] Enabled v0.0.1");
    }

    @Override
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getTag().equals("SimpleChat"))
            return;

        // Disallow clients from spoofing the SGBungee packet
        if (event.getDestination() != Destination.CLIENT) {
            event.setCancelled(true);
            return;
        }
        //
        // String[] data = event.getData().split("@#@");
        // String displayName = ChatColor.translateAlternateColorCodes('&', data[0]);
        // String format = ChatColor.translateAlternateColorCodes('&', data[1]);
        // String message = ChatColor.translateAlternateColorCodes('&', data[2]);

        // PacketFAPluginMessage pMessage = new PacketFAPluginMessage("SimpleChat", event.getData().getBytes());

        String message = event.getData();
        if (message.startsWith("@#@")) {
            String[] data = event.getData().substring(3).split("@#@");
            String to = "";
            if (data[1] != null && data[1].length() >= 0) {
                to = data[1];
            }

            UserConnection toConn = null;
            UserConnection fromConn = event.getConnection();
            boolean found = false;

            if (BungeeCord.instance.connections.containsKey(to)) {
                toConn = BungeeCord.instance.connections.get(to);
                found = true;
            } else {
                Set<Entry<String, UserConnection>> conns = BungeeCord.instance.connections.entrySet();
                for (Entry<String, UserConnection> entry : conns) {
                    if (entry.getKey().toLowerCase().startsWith(to.toLowerCase())) {
                        toConn = entry.getValue();
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                event.getConnection().sendMessage("Spieler nicht gefunden.");
            } else {
                toConn.sendPluginMessage("SimpleChat", event.getData().getBytes());
                fromConn.sendMessage(data[2].replace("<to>", toConn.tabListName).replace("<msg>", data[3]));
            }

            event.setCancelled(true);
            return;
        }

        String srcServer = event.getConnection().getServer();

        Set<Entry<String, UserConnection>> connSet = BungeeCord.instance.connections.entrySet();

        for (Entry<String, UserConnection> entry : connSet) {
            UserConnection con = entry.getValue();
            if (!con.getServer().equals(srcServer)) {
                con.sendPluginMessage("SimpleChat", event.getData().getBytes());
                break;
            }
        }

        // event.getPlayer().getDisplayName() + "@#@" + event.getFormat() + "" + event.getMessage();

        // // Split the message to get server/gate
        // String[] data = event.getData().split("@#@");
        // String server = data[0];
        // String gate = data[1];
        //
        // // Switch server, and setup tag to kill old connection
        // event.getConnection().connect(server);
        // event.setTag("KillCon");
        //
        // // Send a message to Stargate on the new server to teleport the player
        // event.getConnection().sendPluginMessage("SimpleChat", gate.getBytes());
    }
}
