package de.redstoneworld.redaction;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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

        handleEvent(event, event.getPlayer(), click, event.getClickedBlock(), null);
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            // Only react on one click event
            return;
        }

        handleEvent(event, event.getPlayer(), ClickType.RIGHT, null, event.getRightClicked());
    }

    @EventHandler
    public void onPlayerDamageInteract(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            // Only react on players
            return;
        }

        handleEvent(event, (Player) event.getDamager(), ClickType.LEFT, null, event.getEntity());
    }

    private void handleEvent(Cancellable event, Player player, ClickType click, Block clickedBlock, Entity clickedEntity) {

        PlayerInventory playerInventory = player.getInventory();
        List<Action> actions = plugin.getActions(
                click,
                clickedBlock != null ? clickedBlock.getType() : Material.AIR,
                clickedBlock != null ? clickedBlock.getState().getData().getData() : -1,
                clickedBlock != null && clickedBlock.getState().getData() instanceof Directional ? ((Directional) clickedBlock.getState().getData()).getFacing() : null,
                clickedEntity != null ? clickedEntity.getType() : null,
                clickedEntity != null && (clickedEntity instanceof Ageable && !((Ageable) clickedEntity).isAdult()),
                playerInventory.getItemInMainHand() != null ? playerInventory.getItemInMainHand().getType() : Material.AIR,
                playerInventory.getItemInMainHand() != null ?  playerInventory.getItemInMainHand().getData().getData() : -1,
                playerInventory.getItemInOffHand() != null ? playerInventory.getItemInOffHand().getType() : Material.AIR,
                playerInventory.getItemInOffHand() != null ?  playerInventory.getItemInOffHand().getData().getData() : -1,
                player.isSneaking(),
                event.isCancelled()
        );

        for (Action action : actions) {
            if (player.hasPermission("rwm.redaction.actions." + action.getName().toLowerCase())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("player", player.getName());
                replacements.put("click", action.getClick().toString());
                replacements.put("block", String.valueOf(action.getClickedBlock()));
                replacements.put("blockdata", String.valueOf(action.getBlockData()));
                replacements.put("entity", String.valueOf(action.getClickedEntity()));
                replacements.put("isbaby", String.valueOf(action.isClickedEntityBaby()));
                replacements.put("hand", String.valueOf(action.getHandItem()));
                replacements.put("offhand", String.valueOf(action.getOffhandItem()));
                replacements.put("world", player.getWorld().getName());
                replacements.put("x", String.valueOf(player.getLocation().getBlockX()));
                replacements.put("y", String.valueOf(player.getLocation().getBlockY()));
                replacements.put("z", String.valueOf(player.getLocation().getBlockZ()));
                replacements.put("yaw", String.valueOf(Math.floor(player.getEyeLocation().getYaw())));
                replacements.put("pitch", String.valueOf(Math.floor(player.getEyeLocation().getPitch())));
                replacements.put("exactx", String.valueOf(player.getLocation().getX()));
                replacements.put("exacty", String.valueOf(player.getLocation().getY()));
                replacements.put("exactz", String.valueOf(player.getLocation().getZ()));
                replacements.put("exactyaw", String.valueOf(player.getEyeLocation().getYaw()));
                replacements.put("exactpitch", String.valueOf(player.getEyeLocation().getPitch()));

                if (clickedBlock != null) {
                    replacements.put("blockx", String.valueOf(clickedBlock.getLocation().getBlockX()));
                    replacements.put("blocky", String.valueOf(clickedBlock.getLocation().getBlockY()));
                    replacements.put("blockz", String.valueOf(clickedBlock.getLocation().getBlockZ()));
                    if (clickedBlock.getState().getData() instanceof Directional) {
                        replacements.put("direction", ((Directional) clickedBlock.getState().getData()).getFacing().toString());
                    }
                }

                plugin.execute(action, player, replacements);
                if (action.isCancel()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
