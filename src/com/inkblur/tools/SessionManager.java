package com.inkblur.tools;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SessionManager implements Listener {
	private final HashMap<String,PlayerSession> sessions = new HashMap<String,PlayerSession>();
	private final ToolsApi plugin;
	public SessionManager(ToolsApi plugin){
		this.plugin = plugin;
		for(Player p : Bukkit.getOnlinePlayers()){
			addUser(p);
		}
	}
	private void addUser(Player p){
		sessions.put(p.getName(), new PlayerSession(p, plugin));
	}
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		sessions.remove(event.getPlayer().getName());
	}
	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		addUser(event.getPlayer());
	}
	@EventHandler
	public void onToggleSneak(PlayerToggleSneakEvent event) {
		sessions.get(event.getPlayer().getName()).onSneak(event);
	}
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		sessions.get(event.getPlayer().getName()).onInteract(event);
	}
	@EventHandler
	public void onSwitch(PlayerItemHeldEvent event) {
		if(event.getNewSlot() == event.getPreviousSlot()){
			// Fired when the player inventory is loaded (I think)
			//sessions.get(event.getPlayer().getName()).refresh();
		} else {			
			sessions.get(event.getPlayer().getName()).onSwitch(event.getNewSlot());
		}
	}
	@EventHandler
	public void onInvEvent(InventoryClickEvent event) {
		if(event.getInventory().getHolder() instanceof Player
				&& event.getSlotType() == SlotType.QUICKBAR){
			sessions.get(event.getWhoClicked().getName())
			.onInventoryChange(event.getSlot()-9,event.getCursor().getType());
		}
	}
	@EventHandler
	public void onDropEvent(PlayerDropItemEvent event) {
		sessions.get(event.getPlayer().getName()).refresh();
	}
	@EventHandler
	public void onPickupEvent(PlayerPickupItemEvent event) {
		sessions.get(event.getPlayer().getName()).refresh();
	}
	public void onCommand(String name, String label, String[] args) {
		sessions.get(name).onCommand(label, args);
	}
	void updateHUD(String playerName) {
		try {
			sessions.get(playerName).updateHUD();
		} catch(NullPointerException e){
			plugin.getLogger().log(Level.SEVERE, playerName+" does not have a PlayerSession registered, is this player online?");
			e.printStackTrace();
		}
	}
}
