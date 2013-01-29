package com.github.calenria.scbungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

public class SimpleChat extends Plugin {

    @Override
    public void onEnable() {
        BungeeCord.getInstance().registerChannel("SimpleChat");
        BungeeCord.getInstance().getPluginManager().registerListener(new SimpleChatListener());
    }

}
