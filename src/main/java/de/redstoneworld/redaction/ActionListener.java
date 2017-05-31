package de.redstoneworld.redaction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

    @EventHandler(priority = EventPriority.HIGHEST)
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            // Only react on one click event
            return;
        }

        handleEvent(event, event.getPlayer(), ClickType.RIGHT, null, event.getRightClicked());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
                clickedBlock != null && clickedBlock.getState().getData() instanceof Directional
                        ? ((Directional) clickedBlock.getState().getData()).getFacing()
                        : clickedEntity != null && clickedEntity instanceof Directional
                                ? ((Directional) clickedEntity).getFacing()
                                : null,
                clickedEntity != null ? clickedEntity.getType() : null,
                clickedEntity != null ? (clickedEntity instanceof Ageable && !((Ageable) clickedEntity).isAdult()) : null,
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
                replacements.put("isbaby", String.valueOf(action.getIsClickedEntityBaby()));
                replacements.put("hand", String.valueOf(action.getHandItem()));
                replacements.put("offhand", String.valueOf(action.getOffhandItem()));
                replacements.put("world", player.getWorld().getName());
                Location playerLocation = player.getLocation();
                Location playerEyeLocation = player.getEyeLocation();
                replacements.put("x", String.valueOf(playerLocation.getBlockX()));
                replacements.put("y", String.valueOf(playerLocation.getBlockY()));
                replacements.put("z", String.valueOf(playerLocation.getBlockZ()));
                replacements.put("yaw", String.valueOf(Math.floor(playerEyeLocation.getYaw())));
                replacements.put("pitch", String.valueOf(Math.floor(playerEyeLocation.getPitch())));
                replacements.put("exactx", String.valueOf(playerLocation.getX()));
                replacements.put("exacty", String.valueOf(playerLocation.getY()));
                replacements.put("exactz", String.valueOf(playerLocation.getZ()));
                replacements.put("exactyaw", String.valueOf(playerEyeLocation.getYaw()));
                replacements.put("exactpitch", String.valueOf(playerEyeLocation.getPitch()));

                if (clickedBlock != null) {
                    Location blockLocation = clickedBlock.getLocation();
                    replacements.put("blockx", String.valueOf(blockLocation.getBlockX()));
                    replacements.put("blocky", String.valueOf(blockLocation.getBlockY()));
                    replacements.put("blockz", String.valueOf(blockLocation.getBlockZ()));
                    if (clickedBlock.getState().getData() instanceof Directional) {
                        replacements.put("direction", ((Directional) clickedBlock.getState().getData()).getFacing().toString());
                    }
                }
                if (clickedEntity != null) {
                    Location entityLocation = clickedEntity.getLocation();
                    replacements.put("entityx", String.valueOf(entityLocation.getBlockX()));
                    replacements.put("entityy", String.valueOf(entityLocation.getBlockY()));
                    replacements.put("entityz", String.valueOf(entityLocation.getBlockZ()));
                    replacements.put("entityyaw", String.valueOf(Math.floor(entityLocation.getYaw())));
                    replacements.put("entitypitch", String.valueOf(Math.floor(entityLocation.getPitch())));
                    replacements.put("entityexactx", String.valueOf(entityLocation.getX()));
                    replacements.put("entityexacty", String.valueOf(entityLocation.getY()));
                    replacements.put("entityexactz", String.valueOf(entityLocation.getZ()));
                    replacements.put("entityexactyaw", String.valueOf(entityLocation.getYaw()));
                    replacements.put("entityexactpitch", String.valueOf(entityLocation.getPitch()));
                    if (clickedEntity instanceof Directional) {
                        replacements.put("direction", ((Directional) clickedEntity).getFacing().toString());
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
