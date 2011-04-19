package com.mjmr89.TradeCraft;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;


public class TradeCraftBlockListener extends BlockListener{
	
	private TradeCraft plugin;
	
	TradeCraftBlockListener(TradeCraft plugin){
		this.plugin = plugin;
	}
	
	public void debug(String str){
		plugin.getServer().broadcastMessage(str);
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent e) {
		if ( !this.plugin.isEnabled() ) {
			return;
		}
		
		Player player = e.getPlayer();
		Block block = e.getBlock();
        ArrayList<TradeCraftShop> shops = plugin.getShopsFromBlock(player, block);
                
        if (shops.size() == 0) {
            return;
        }

        // Go through all shops in the list and check whether the player can destroy them all first.
        // Only if that is the case proceed to destroy.
        for ( TradeCraftShop shop : shops ) {
        	if (!shop.playerCanDestroy(player) && !plugin.permissions.canDestroyShops(player) ||
        			shop.shopCanBeWithdrawnFrom() ) {
        		// cannot destroy this shop, so cancel destruction, use distinct error messages 
        		if ( shop.shopCanBeWithdrawnFrom() ) {
                    plugin.sendMessage(player, "All items and currency must be withdrawn before you can destroy this sign or chest!");
        		} else {
        			if ( block.getType() == Material.WALL_SIGN ) {
        				plugin.sendMessage(player, "You can't destroy this sign!");
        			} else if ( block.getType() == Material.CHEST ) {
        				plugin.sendMessage(player, "You can't destroy this chest!");
        			} else {
        				plugin.sendMessage(player, "You can't destroy this block because there are signs attached to it!");
        			}
        		}
        		stopDestruction(block,e);
                return;
        	}
        }
        // player can destroy all shops, so proceed
        for ( TradeCraftShop shop : shops ) {
        	plugin.data.deleteShop(shop);
        }
    }
	
	public void stopDestruction(Block b, BlockBreakEvent e){
		if(b.getState() instanceof Sign){
			Sign sign = (Sign)b.getState();
			String[] lines = sign.getLines();
			e.setCancelled(true);
			for(int i = 0;i<4;i++){
				sign.setLine(i, lines[i]);
			}
			
			sign.update(true);
			return;
		} else {
			e.setCancelled(true);			
		}
		
	}
	
	public void onSignChange(SignChangeEvent e) {
		if ( !this.plugin.isEnabled() ) {
			return;
		}
		
		Player player = e.getPlayer();
		Sign sign = (Sign) e.getBlock().getState();

		int x = sign.getX();
		int y = sign.getY();
		int z = sign.getZ();

		plugin.trace(player, "You created a sign at %d, %d, %d.", x, y, z);

		// check for the special item line, ie. [dirt] pass on this event if there's no such item text
		String itemName = plugin.getItemName(e.getLines());
        if (itemName == null) {
            return;
        }

		// require a chest to be below the sign
		Block blockBelowSign = player.getWorld().getBlockAt(x, y - 1, z);
		if (blockBelowSign.getType() != Material.CHEST) {
			player.sendMessage("There is no chest beneath the sign.");
			e.setCancelled(true);
			return;
		}

		// handle check for infinite chest TODO NO, WRONG..
		// Differentiate between normal player or op/player with permission to place infinite shop right away.
		// A normal player will always try to make a player shop. An admin could be placing an infinite chest.
		// In the latter case, test whether there are and rate lines on the sign. If not, it will be an infinite chest.
		// If it's still meant to be a player chest, then put an owner line -namehere- on the last line, limited to maxline-2 characters.
		// Convert the rate lines from '30:1' to "Buy 30 for 1" / "Sell 30 for 1"
		
		// TODO require the item name to be on the first line. Though this will prevent additional text to make chest look a bit more
		//  user friendly. But that's where the message on clicking can be helpful.
		
		// check whether this is an infinite chest by looking for exchange rates
		TradeCraftExchangeRate buyRate = plugin.getExchangeRate(sign, 1);
		// If this is a normal player, or an admin, but at least a buy rate is set, then this will be a player owned shop.
		// TODO later check needed for repair shop
		if ( buyRate.amount != 0 ) {
			if ( !plugin.properties.getPlayerOwnedShopsEnabled() ) {
				plugin.sendMessage(player, "Player owned shops are disabled!");
	        	e.setCancelled(true);
	        	return;
			} else if ( !plugin.permissions.canMakePlayerShops(player) ) {
				plugin.sendMessage(player, "You are not allowed to build a player owned shop!");
	        	e.setCancelled(true);
	        	return;
			} else {
				// this will be a player owned shop
				// make the buy rate line nicer
				sign.setLine(1, "Buy "+ buyRate.amount +" for "+ buyRate.value);
				
				// check for sell rate and convert that line too if it is set
				TradeCraftExchangeRate sellRate = plugin.getExchangeRate(sign, 2);
				if ( sellRate.amount != 0 ) {
					sign.setLine(2, "Sell "+ sellRate.amount +" for "+ sellRate.value);
				}
				// set the player name on the last line, cut to up to 13 characters (15 - 2), surrounded by - -
				sign.setLine(3, "-"+ player.getName().substring(0, 13) +"-");
				
				// now create and store the player shop
				TradeCraftPlayerOwnedShop playershop = new TradeCraftPlayerOwnedShop()
			}
		} else {
			if ( !plugin.properties.getInfiniteShopsEnabled() ) {
				plugin.sendMessage(player, "Infinite shops are disabled!");
        		e.setCancelled(true);
        		return;
			} else if ( !plugin.permissions.canMakeInfShops(player) ){
	            plugin.sendMessage(player, "You can't create infinite shops!");
	            e.setCancelled(true);
	            return;
	        } else {
	        	// this will be an infinite shop, nothing will be stored, so just accept it
	        	return;
	        }
		}
		
        String ownerName = plugin.getOwnerName(e.getLine(3));
        if (ownerName == null) {
        	if ( plugin.properties.getInfiniteShopsEnabled() ) {
	            if ( plugin.permissions.canMakeInfShops(player) ){
	            	// all ok, infinite chests are not stored
	            	return;
	            } else {
		            plugin.sendMessage(player, "You can't create infinite shops!");
		            e.setCancelled(true);
		            
		            return;
	            }
        	} else {
        		plugin.sendMessage(player, "Infinite shops are disabled!");
        		e.setCancelled(true);
            
        		return;
        	}
        }

        if ( plugin.properties.getPlayerOwnedShopsEnabled() ) {
	        if ( plugin.permissions.canMakePlayerShops(player)) {
	        	// create the actual shop here now, so we can store the creator's name
	        	// just a requirement whether you need to have the full name on the sign, though the actual check is with the full name later
	            if ( this.plugin.properties.getStrictPlayerShopOwnerNameRequired() ) {
		        	if (player.getName().equalsIgnoreCase(ownerName)) {
		        		plugin.data.setOwnerOfSign(player.getName(), sign);
		        		return;
		        	}
		        } else {
		        	if (player.getName().startsWith(ownerName)) {
		        		plugin.data.setOwnerOfSign(player.getName(), sign);
		        		return;
		        	}
		        }
	        } else {
	        	plugin.sendMessage(player, "You can't create signs with other player's names on them!");
	        	e.setCancelled(true);
	        	
	        	return;
	        }
        } else {
        	plugin.sendMessage(player, "Player owned shops are disabled!");
        	e.setCancelled(true);
        	
        	return;
        }
        
    	plugin.sendMessage(player, "You are not allowed to build a shop!");
    	e.setCancelled(true);
        
        
        return;
    }

}
