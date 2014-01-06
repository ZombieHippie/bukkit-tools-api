package com.inkblur.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class TestTool extends Tool {
	@Override
	public boolean onRightClick(Block clicked) {
		if(clicked != null){
			player.sendMessage("You clicked on a "+clicked.getType().toString());
		} else {
			
		}
		return true;
	}
	@Override
	public void onSwitch(int i){
		
	}
	@Override
	public boolean onLeftClick(Block clicked) {
		
		return false;
	}
	@Override
	public String[] getHUD() {
		String[] arr = {"Api test"};
		return arr;
	}
	@Override
	public String getName() {
		return "Test Tool";
	}
	@Override
	public void onCommand(String[] args) {
		if(args.length == 2){
			if(args[0] == "m"){
				Material m = Material.matchMaterial(args[1]);
				if(m != null){
					player.setItemInHand(new ItemStack(m,1));
				} else {
					player.sendMessage(ChatColor.RED+args[1]+" is not a valid material");
				}
			}
		}
	}
	
}
