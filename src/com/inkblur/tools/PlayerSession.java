package com.inkblur.tools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerSession {
	Tool[] inventory = { null, null, null, null, null, null, null, null, null };
	final ToolsApi plugin;
	final Player player;
	private Tool currentTool = null;
	int currentIndex = 0;
	boolean init = false;

	public PlayerSession(Player player, ToolsApi plugin) {
		this.player = player;
		this.plugin = plugin;
		this.currentIndex = player.getInventory().getHeldItemSlot();
	}

	public Tool getTool(int index) {
		if (!init) {
			Scoreboard newSB = Bukkit.getServer().getScoreboardManager()
					.getNewScoreboard();
			player.setScoreboard(newSB);
			init = true;
		}
		try {
			return inventory[index];
		} catch (IndexOutOfBoundsException err) {
			return null;
		}
	}

	public void setTool(Tool tool) {
		if (tool.isItemTool()) {
			plugin.l(tool.getName());
			ItemStack its = new ItemStack(tool.getMaterial(), 1);
			player.getInventory().setItem(currentIndex, its);
		}
		inventory[currentIndex] = tool;
		currentTool = tool;
		tool.onSwitch(currentIndex);
		this.updateHUD();
	}

	@SuppressWarnings("deprecation")
	public void onInteract(PlayerInteractEvent event) {
		if (currentTool != null) {
			boolean shouldCancel;
			Block clicked = event.getClickedBlock();
			switch (event.getAction()) {
			case LEFT_CLICK_AIR:
				clicked = player.getTargetBlock(null, 100);
			case LEFT_CLICK_BLOCK:
				shouldCancel = currentTool.onLeftClick(clicked);
				break;
			case RIGHT_CLICK_AIR:
				clicked = player.getTargetBlock(null, 100);
			case RIGHT_CLICK_BLOCK:
				shouldCancel = currentTool.onRightClick(clicked);
				break;
			default:
				return;
			}
			if (shouldCancel) {
				event.setCancelled(true);
			}
		}
	}

	public void clear() {
		setTool(null);
	}

	public void onSneak(PlayerToggleSneakEvent event) {

	}

	public void onSwitch(int i) {
		if (player.isSneaking()) {
			Tool old = inventory[currentIndex];
			if (old != null)
				if (old.isItemTool()) {
					ItemStack oldIts = player.getInventory().getItem(
							currentIndex);
					player.getInventory().setItem(currentIndex,
							player.getInventory().getItem(i));
					player.getInventory().setItem(i, oldIts);
				}
			inventory[currentIndex] = inventory[i];
			inventory[i] = old;
		} else {
			currentTool = getTool(i);
			if (currentTool != null) {
				currentTool.onSwitch(i);
			} else {
				ItemStack its = player.getInventory().getItem(i);
				if (its != null && !its.getType().isBlock()) {
					String itemToolName = plugin.lookupItemTool(player
							.getInventory().getItem(i).getType());
					if (itemToolName != null) {
						Tool itemTool = plugin.getNewTool(itemToolName, this);
						inventory[i] = itemTool;
					}
				}
			}
		}
		currentIndex = i;
		updateHUD();
	}

	public void onCommand(String label, String[] args) {
		if (label.equalsIgnoreCase("t") || label.equalsIgnoreCase("tool")) {
			if (currentTool != null) {
				currentTool.onCommand(args);
			} else {
				player.sendMessage(ChatColor.DARK_GRAY + "Unknown tool");
			}
		} else {
			Tool tool = plugin.getNewTool(label, this);
			if (tool != null) {
				setTool(tool);
			}
		}
	}

	private OfflinePlayer getInfo(String str) {
		if (str.length() > 16) {
			str = str.substring(0, 15);
		}
		return Bukkit.getOfflinePlayer(str);
	}

	void updateHUD() {
		Scoreboard sb = player.getScoreboard();
		if (sb.getObjective(DisplaySlot.SIDEBAR) != null)
			sb.getObjective(DisplaySlot.SIDEBAR).unregister();

		Objective obj = sb.registerNewObjective("tools-api", "dummy");
		obj.setDisplayName(ChatColor.GREEN + plugin.getName());
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		int index = 0;
		/*
		 * for (Tool tool : inventory) { message = ChatColor.GRAY.toString() +
		 * (index + 1) + (currentIndex == index ? "#" : " "); if (tool != null)
		 * { message += ChatColor.AQUA + tool.getName(); } final Score score =
		 * obj.getScore(getInfo(message)); score.setScore(-10 - index++); }//
		 */
		String message = "";
		for (Tool tool : inventory) {
			if (tool != null) {
				String[] global = tool.getGlobalHUD();
				if(global != null){
					for(String str : global){
						final Score score = obj.getScore(getInfo(str));
						score.setScore(-10 - index++);
					}
				}
			}
		}// */
		index = 9;
		if (currentTool != null) {
			for (String str : currentTool.getHUD()) {
				final Score score = obj.getScore(getInfo(" " + str));
				score.setScore(index--);
			}
		}
		for (; index >= 0; index--) {
			final Score score = obj.getScore(Bukkit
					.getOfflinePlayer(ChatColor.DARK_GRAY.toString()
							+ (10 - index) + " "));
			score.setScore(index);
		}
	}

	public void onInventoryChange(int slot, Material newMat) {
		Tool clicked = inventory[slot];
		if (clicked != null && clicked.isItemTool()) {
			inventory[slot] = null;
		}
		if (!newMat.isBlock()) {
			String itemToolName = plugin.lookupItemTool(newMat);
			if (itemToolName != null) {
				Tool itemTool = plugin.getNewTool(itemToolName, this);
				inventory[slot] = itemTool;
			}
		}
		updateHUD();
	}

	public void refresh() {
		for (int iit = 0; iit < 9; iit++) {
			ItemStack its = player.getInventory().getItem(iit);
			if (its == null) {
				if (inventory[iit] != null)
					if (inventory[iit].isItemTool())
						inventory[iit] = null;
			} else if (!its.getType().isBlock()) {
				Tool tool = inventory[iit];
				String lookup = plugin.lookupItemTool(its.getType());
				if (lookup != null)
					if (tool == null || !tool.getName().equals(lookup)) {
						inventory[iit] = plugin.getNewTool(lookup, this);
					}
			}
		}
		updateHUD();
	}
}
