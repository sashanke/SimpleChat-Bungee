package com.github.calenria.scbungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class SimpleChat extends Plugin {
    public static ResourceBundle messages = null;
    public static String pluginPath = "./plugins/SimpleChat-Bungee/";
    public File messagesFile = new File(pluginPath + "messages.properties");
    public static Boolean hideStream = true;
    public static Boolean debug = false;

    @Override
    public void onEnable() {
        BungeeCord.getInstance().registerChannel("SimpleChat");
        BungeeCord.getInstance().getPluginManager().registerListener(this, new SimpleChatListener());
        messages = readProperties();
        hideStream = Boolean.parseBoolean(messages.getString("hideStream"));
        try {
            debug = Boolean.parseBoolean(messages.getString("debug"));
        } catch (MissingResourceException e) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(this.messagesFile, true));
                out.write("debug=false");
                out.newLine();
                out.close();
            } catch (Exception ex) {
                BungeeCord.getInstance().getLogger().warning(ex.getLocalizedMessage());
            }
        }

    }

    private PropertyResourceBundle readProperties() {
        PropertyResourceBundle bundle = null;

        File pluginDir = new File(pluginPath);
        if (!this.messagesFile.exists()) {
            try {
                if (!pluginDir.exists()) {
                    pluginDir.mkdirs();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(this.messagesFile));
                out.write("hideStream=true");
                out.newLine();
                out.write("login=&6%s hat das Spiel betreten");
                out.newLine();
                out.write("logout=&6%s hat das Spiel verlassen");
                out.newLine();
                out.write("debug=false");
                out.newLine();
                out.close();
            } catch (Exception e) {
                BungeeCord.getInstance().getLogger().warning(e.getLocalizedMessage());
            }
        }
        try {
            bundle = new PropertyResourceBundle(new FileInputStream(this.messagesFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bundle;
    }
}
