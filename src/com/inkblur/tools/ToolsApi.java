package com.inkblur.tools;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ToolsApi extends JavaPlugin {
	private final HashMap<String,Class<?extends Tool>> registeredTools
	= new HashMap<String,Class<?extends Tool>>();
	private final HashMap<Material,String> itemTools
	= new HashMap<Material,String>();
	private final HashMap<String,Material> itemToolsReverse
	= new HashMap<String,Material>();
	private static ToolsApi instance;
	private SessionManager sessionManager;
	@Override
	public void onEnable() {
		sessionManager = new SessionManager(this);
		register(sessionManager);
		instance = this;
		registerItem(TestTool.class, Material.STICK, "testtool");
	}

	@Override
	public void onDisable() {
		sessionManager = null;
		registeredTools.clear();
		instance = null;
	}
	public String lookupItemTool(Material mat){
		return itemTools.get(mat);
	}
	public Material lookupItemToolMaterial(String label){
		return itemToolsReverse.get(label);
	}
	public Tool getNewTool(String toolName, PlayerSession session){
		Class<? extends Tool> toolCL = registeredTools.get(toolName);
		if(toolCL != null){
			Tool tool;
			try {
				tool = toolCL.newInstance();
				tool.setup(session,itemToolsReverse.get(toolName));
				return tool;
			} catch (InstantiationException e){
				e.printStackTrace();
			} catch (IllegalAccessException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void registerTool(Class<? extends Tool> toolClass, String label) {
		instance.l("Registering: "+toolClass.getName()+".class with /"+label);
		Bukkit.getServer().getPluginCommand(label).setExecutor(instance);
		instance.registeredTools.put(label, toolClass);
	}
	public static void registerItem(Class<? extends Tool> toolClass, Material mat, String label) {
		instance.l("Registering: "+toolClass.getName()+".class on Material."+mat.toString()+" with /"+label);
		Bukkit.getServer().getPluginCommand(label).setExecutor(instance);
		instance.registeredTools.put(label, toolClass);
		instance.itemTools.put(mat, label);
		instance.itemToolsReverse.put(label, mat);
		instance.l(instance.itemToolsReverse.get(label).toString());
	}
	public static void updateHUD(String playerName){
		instance.sessionManager.updateHUD(playerName);
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(sender instanceof Player){
			sessionManager.onCommand(sender.getName(), label, args);
		}
		return false;
	}
	private void register(Listener listener) {
		getServer().getPluginManager().registerEvents(listener, this);
	}

	public void l(Object o) {
		getLogger().info((String) o);
	}
}