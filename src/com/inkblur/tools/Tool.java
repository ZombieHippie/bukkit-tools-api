package com.inkblur.tools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class Tool {
	public abstract String[] getHUD();
	public abstract boolean onRightClick(Block clicked);
	public abstract boolean onLeftClick(Block clicked);
	public abstract void onCommand(String[] args);
	public abstract void onSwitch(int index);
	public abstract String getName();
	protected Player player;
	private PlayerSession ps;
	private Material material;
	public void setup(PlayerSession ps, Material isItemTool){
		this.player = ps.player;
		this.ps = ps;
		this.material = isItemTool;
	}
	public boolean isItemTool(){
		return material != null;
	}
	public String[] getGlobalHUD(){
		return null;
	}
	public Material getMaterial(){
		return material;
	}
	public void updateHUD(){
		ps.updateHUD();
	}
	protected void execute(Runnable task){
		Bukkit.getScheduler().runTaskAsynchronously(ps.plugin, task);
	}
}
