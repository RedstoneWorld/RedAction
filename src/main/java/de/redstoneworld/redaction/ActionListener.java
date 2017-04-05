package de.redstoneworld.redaction;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Directional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionListener implements Listener {
    private final RedAction plugin;

    public ActionListener(RedAction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            // Only react on one click event
            return;
        }
        ClickType click = ClickType.fromAction(event.getAction());
        if (click == null) {
            // Not a click but other interaction
            return;
        }

        PlayerInventory playerInventory = event.getPlayer().getInventory();
        List<Action> actions = plugin.getActions(
                click,
                event.getClickedBlock() != null ? event.getClickedBlock().getType() : Material.AIR,
                event.getClickedBlock() != null ? event.getClickedBlock().getState().getData().getData() : -1,
                event.getClickedBlock() != null && event.getClickedBlock().getState().getData() instanceof Directional ? ((Directional) event.getClickedBlock().getState().getData()).getFacing() : (BlockFace) null,
                playerInventory.getItemInMainHand() != null ? playerInventory.getItemInMainHand().getType() : Material.AIR,
                playerInventory.getItemInMainHand() != null ?  playerInventory.getItemInMainHand().getData().getData() : -1,
                playerInventory.getItemInOffHand() != null ? playerInventory.getItemInOffHand().getType() : Material.AIR,
                playerInventory.getItemInOffHand() != null ?  playerInventory.getItemInOffHand().getData().getData() : -1,
                event.getPlayer().isSneaking(),
                event.isCancelled()
        );

        for (Action action : actions) {
            if (event.getPlayer().hasPermission("rwm.redaction.actions." + action.getName().toLowerCase())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("click", action.getClick().toString());
                replacements.put("block", String.valueOf(action.getClickedBlock()));
                replacements.put("hand", String.valueOf(action.getHandItem()));
                replacements.put("offhand", String.valueOf(action.getOffhandItem()));
                replacements.put("world", event.getPlayer().getWorld().getName());
                replacements.put("x", String.valueOf(event.getPlayer().getLocation().getBlockX()));
                replacements.put("y", String.valueOf(event.getPlayer().getLocation().getBlockY()));
                replacements.put("z", String.valueOf(event.getPlayer().getLocation().getBlockZ()));
                replacements.put("yaw", String.valueOf(Math.floor(event.getPlayer().getLocation().getYaw())));
                replacements.put("pitch", String.valueOf(Math.floor(event.getPlayer().getLocation().getPitch())));
                replacements.put("exactx", String.valueOf(event.getPlayer().getLocation().getX()));
                replacements.put("exacty", String.valueOf(event.getPlayer().getLocation().getY()));
                replacements.put("exactz", String.valueOf(event.getPlayer().getLocation().getZ()));
                replacements.put("exactyaw", String.valueOf(event.getPlayer().getLocation().getYaw()));
                replacements.put("exactpitch", String.valueOf(event.getPlayer().getLocation().getPitch()));

                if (event.getClickedBlock() != null) {
                    replacements.put("blockx", String.valueOf(event.getClickedBlock().getLocation().getBlockX()));
                    replacements.put("blocky", String.valueOf(event.getClickedBlock().getLocation().getBlockY()));
                    replacements.put("blockz", String.valueOf(event.getClickedBlock().getLocation().getBlockZ()));
                    if (event.getClickedBlock().getState().getData() instanceof Directional) {
                        replacements.put("direction", ((Directional) event.getClickedBlock().getState().getData()).getFacing().toString());
                    }
                }

                plugin.execute(action, event.getPlayer(), replacements);
                if (action.isCancel()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
