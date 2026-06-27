package com.timebrackets.timebracket;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;

import java.util.Arrays;

public class TimeBracketPlugin extends JavaPlugin implements Listener {

    private NamespacedKey bracketKey;
    private NamespacedKey guiKey;

    @Override
    public void onEnable() {
        bracketKey = new NamespacedKey(this, "time_bracket");
        guiKey = new NamespacedKey(this, "gui_slot");
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Time Bracket plugin enabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            giveBracket(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isTimeBracket(item)) return;
        event.setCancelled(true);
        openTravelGUI(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "✦ Time Bracket ✦")) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;
        String slot = meta.getPersistentDataContainer().get(guiKey, PersistentDataType.STRING);
        if (slot == null) return;
        player.closeInventory();
        switch (slot) {
            case "timerift" -> {
                teleportToWorld(player, "TimeRift");
                player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Returning to the Time Rift...");
            }
            case "prehistoric" -> player.sendMessage(ChatColor.YELLOW + "✦ Prehistoric Earth is not yet unlocked.");
            case "modern" -> player.sendMessage(ChatColor.YELLOW + "✦ Modern Earth is not yet unlocked.");
        }
    }

    private void openTravelGUI(Player player) {
        Inventory gui = getServer().createInventory(null, 27,
            ChatColor.DARK_PURPLE + "✦ Time Bracket ✦");
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) gui.setItem(i, filler);

        ItemStack riftItem = new ItemStack(Material.END_PORTAL_FRAME);
        ItemMeta riftMeta = riftItem.getItemMeta();
        riftMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ The Time Rift");
        riftMeta.setLore(Arrays.asList(ChatColor.GRAY + "Your personal home.", ChatColor.GRAY + "A space outside of time."));
        riftMeta.getPersistentDataContainer().set(guiKey, PersistentDataType.STRING, "timerift");
        riftItem.setItemMeta(riftMeta);
        gui.setItem(11, riftItem);

        ItemStack prehistoricItem = new ItemStack(Material.BARRIER);
        ItemMeta prehistoricMeta = prehistoricItem.getItemMeta();
        prehistoricMeta.setDisplayName(ChatColor.RED + "✦ Prehistoric Earth");
        prehistoricMeta.setLore(Arrays.asList(ChatColor.GRAY + "65 million years ago.", ChatColor.RED + "Complete the Time Rift to unlock."));
        prehistoricMeta.getPersistentDataContainer().set(guiKey, PersistentDataType.STRING, "prehistoric");
        prehistoricItem.setItemMeta(prehistoricMeta);
        gui.setItem(13, prehistoricItem);

        ItemStack modernItem = new ItemStack(Material.BARRIER);
        ItemMeta modernMeta = modernItem.getItemMeta();
        modernMeta.setDisplayName(ChatColor.RED + "✦ Modern Earth");
        modernMeta.setLore(Arrays.asList(ChatColor.GRAY + "The present day.", ChatColor.RED + "Complete Prehistoric Earth to unlock."));
        modernMeta.getPersistentDataContainer().set(guiKey, PersistentDataType.STRING, "modern");
        modernItem.setItemMeta(modernMeta);
        gui.setItem(15, modernItem);

        player.openInventory(gui);
    }

    private void teleportToWorld(Player player, String worldName) {
        var world = getServer().getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "That world doesn't exist yet.");
            return;
        }
        player.teleport(world.getSpawnLocation());
    }

    public ItemStack createBracket() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "✦ Time Bracket");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "A mysterious device capable of",
            ChatColor.GRAY + "travelling through time.",
            "",
            ChatColor.DARK_PURPLE + "Right-click to open."
        ));
        meta.getPersistentDataContainer().set(bracketKey, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isTimeBracket(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(bracketKey, PersistentDataType.BYTE);
    }

    public void giveBracket(Player player) {
        player.getInventory().setItem(8, createBracket());
        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ You have been given a Time Bracket.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("givebracket")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /givebracket <player>");
                return true;
            }
            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            giveBracket(target);
            sender.sendMessage(ChatColor.GREEN + "Given Time Bracket to " + target.getName());
            return true;
        }
        return false;
    }
}
