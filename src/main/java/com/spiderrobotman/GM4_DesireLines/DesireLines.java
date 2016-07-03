package com.spiderrobotman.GM4_DesireLines;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Project: GM4_DesireLines
 * Author: SpiderRobotMan
 * Date: Jul 03 2016
 * Website: http://www.spiderrobotman.com
 */

@SuppressWarnings("deprecation")
public class DesireLines extends JavaPlugin implements Listener {

    private int WALKING_PROBABILITY;
    private int SPRINTING_PROBABILITY;
    private int SNEAK_PROBABILITY;

    private Map<ItemStack, ItemStack> UNDER_REPLACE = new HashMap<>();
    private Map<ItemStack, ItemStack> IN_REPLACE = new HashMap<>();

    @Override
    public void onEnable() {
        //Register this Class as a listener
        Bukkit.getPluginManager().registerEvents(this, this);

        //Save default config if not already there
        saveDefaultConfig();

        //Import config
        WALKING_PROBABILITY = getConfig().getInt("walking-probability", 5);
        SPRINTING_PROBABILITY = getConfig().getInt("sprint-probability", 10);
        SNEAK_PROBABILITY = getConfig().getInt("sneak-probability", 0);
        UNDER_REPLACE = getItemMapFromList(getConfig().getStringList("under_replacements"));
        IN_REPLACE = getItemMapFromList(getConfig().getStringList("in_replacements"));

        //Log module enabled message
        getLogger().log(Level.INFO, ChatColor.GREEN + "[DesireLines] Module enabled!");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        //Prevent checking same block twice
        if(e.getFrom().getX() == e.getTo().getX() && e.getFrom().getZ() == e.getTo().getZ()) return;

        //Set probability for different speeds
        int prob = WALKING_PROBABILITY;
        if(e.getPlayer().isSneaking()) prob = SNEAK_PROBABILITY;
        if(e.getPlayer().isSprinting()) prob = SPRINTING_PROBABILITY;

        //Generate random number and see if under set prob percentage
        if(Math.random()*100 < prob) {

            //Break blocks below player
            Block below = e.getPlayer().getLocation().subtract(0, 0.1, 0).getBlock();
            if(below != null && below.getType() != Material.AIR) {
                ItemStack newBlock = UNDER_REPLACE.get(itemFromBlock(below));
                if(newBlock != null) {
                    setBlockAt(below, newBlock);

                    //Break blocks player is in if below broke
                    Block in = e.getPlayer().getLocation().getBlock();
                    if(in != null && in.getType() != Material.AIR) {
                        ItemStack newBlockIn = IN_REPLACE.get(itemFromBlock(in));
                        if(newBlockIn != null) {
                            setBlockAt(in, newBlockIn);
                        }
                    }
                }
            }
        }
    }

    private Map<ItemStack, ItemStack> getItemMapFromList(List<String> list) {
        //Initialize output map
        Map<ItemStack, ItemStack> out = new HashMap<>();

        //Loop through input strings
        for (String s : list) {
            //Split at ">" and make sure valid
            String[] split = s.split(">");
            if (split.length != 2) continue;

            //Try converting string values to ItemStacks and gracefully catch errors.
            try {
                Material key;
                Material value;

                //Convert key  to a Material
                String[] split1 = split[0].split(":");
                key = Material.valueOf(split1[0].toUpperCase());
                short dmg1 = split1.length == 2 ? Short.parseShort(split1[1]) : 0;

                //Convert value to a Material
                String[] split2 = split[1].split(":");
                value = Material.valueOf(split2[0].toUpperCase());
                short dmg2 = split2.length == 2 ? Short.parseShort(split2[1]) : 0;

                //Push new ItemStack objects to the final map
                out.put(new ItemStack(key, 1, dmg1), new ItemStack(value, 1, dmg2));
            } catch (Exception e) {
                getLogger().warning(ChatColor.RED + "[DesireLines] Invalid material name, skipping - " + s);
            }
        }
        return out;
    }

    private ItemStack itemFromBlock(Block b) {
        //Return ItemStack using given blocks type and data value
        return new ItemStack(b.getType(), 1, b.getData());
    }

    private void setBlockAt(Block b, ItemStack i) {
        if(i.getType() == Material.AIR) {
            b.breakNaturally();
        } else {
            b.setType(i.getType());
            b.setData((byte) i.getDurability());
            b.getState().update();
        }
        if (i.getType().isBlock() && i.getType().isSolid()) b.getWorld().spigot().playEffect(b.getLocation().add(0.5, 0.1, 0.5), Effect.TILE_BREAK, b.getTypeId(), b.getData(), 0, 0, 0, 0, 10, 16);
    }
}
