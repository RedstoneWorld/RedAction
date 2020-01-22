package de.redstoneworld.redaction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
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
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.material.Attachable;

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
        BlockData blockData = clickedBlock != null ? clickedBlock.getState(false).getBlockData() : null;
        ClickEventData data = new ClickEventData(
                click,
                clickedBlock != null
                        ? clickedBlock.getLocation()
                        : clickedEntity != null
                                ? clickedEntity.getLocation()
                                : null,
                blockData instanceof Directional
                        ? ((Directional) blockData).getFacing()
                        : clickedEntity instanceof Attachable
                                ? ((org.bukkit.material.Directional) clickedEntity).getFacing()
                                : null, clickedBlock != null ? clickedBlock.getType() : Material.AIR,
                blockData,
                clickedEntity != null ? clickedEntity.getType() : null,
                clickedEntity != null ? (clickedEntity instanceof Ageable && !((Ageable) clickedEntity).isAdult()) : null,
                playerInventory.getItemInMainHand().getType(),
                playerInventory.getItemInMainHand().getItemMeta() instanceof Damageable
                        ? ((Damageable) playerInventory.getItemInMainHand().getItemMeta()).getDamage() : -1,
                playerInventory.getItemInOffHand().getType(),
                playerInventory.getItemInOffHand().getItemMeta() instanceof Damageable
                        ? ((Damageable) playerInventory.getItemInOffHand().getItemMeta()).getDamage() : -1,
                player.isSneaking(),
                event.isCancelled()
        );
        List<Action> actions = plugin.getActions(data, true);
        if (handleActions(player, actions, data)) {
            event.setCancelled(true);
        }
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Action> notCancelledActions = plugin.getActions(data, false);
            player.getServer().getScheduler().runTask(plugin, () -> {
                handleActions(player, notCancelledActions, data);
            });
        });
    }

    private boolean handleActions(Player player, List<Action> actions, ClickEventData data) {
        boolean cancel = false;
        for (Action action : actions) {
            if (player.hasPermission("rwm.redaction.actions." + action.getName().toLowerCase())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("player", player.getName());
                replacements.put("click", action.getClick().toString());
                replacements.put("block", String.valueOf(data.getClickedMaterial()));
                String d = action.getClickedBlocks().get(data.getClickedMaterial()).getAsString(true);
                replacements.put("blockdata", d);
                replacements.put("states", d);
                replacements.put("entity", String.valueOf(action.getClickedEntity()));
                replacements.put("isbaby", String.valueOf(action.getIsClickedEntityBaby()));
                replacements.put("hand", action.getHandItems().isEmpty() ? "" : String.valueOf(player.getEquipment().getItemInMainHand().getType()));
                replacements.put("offhand", action.getOffhandItems().isEmpty() ? "" : String.valueOf(player.getEquipment().getItemInOffHand().getType()));
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

                if (data.getClickedMaterial() != Material.AIR) {
                    Location blockLocation = data.getClickedLocation();
                    replacements.put("blockx", String.valueOf(blockLocation.getBlockX()));
                    replacements.put("blocky", String.valueOf(blockLocation.getBlockY()));
                    replacements.put("blockz", String.valueOf(blockLocation.getBlockZ()));
                }
                if (data.getEntityType() != null) {
                    Location entityLocation = data.getClickedLocation();
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
                }
                if (data.getClickedDirection() != null) {
                    replacements.put("direction", data.getClickedDirection().toString());
                }

                plugin.execute(action, player, replacements);
                if (action.isCancel()) {
                    cancel = true;
                }
            }
        }
        return cancel;
    }
}
