package de.redstoneworld.redaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
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
        ClickType click = ClickType.fromAction(event.getAction());
        if (click == null) {
            // Not a click but other interaction
            return;
        }

        List<Action> actions = new ArrayList<>();
        if (event.getHand() == EquipmentSlot.HAND) {
            if (event.getItem() != null) {
                actions.addAll(plugin.getActions(
                        Condition.HAND,
                        click,
                        event.getItem().getType(),
                        event.getItem().getDurability(),
                        null
                ));
            }
            if (event.getClickedBlock() != null) {
                actions.addAll(plugin.getActions(
                        Condition.BLOCK,
                        click,
                        event.getClickedBlock().getType(),
                        event.getClickedBlock().getState().getData().getData(),
                        event.getClickedBlock().getState() instanceof Directional ? ((Directional) event.getClickedBlock().getState()).getFacing() : null
                ));
            }
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            actions.addAll(plugin.getActions(
                    Condition.OFFHAND,
                    click,
                    event.getItem().getType(),
                    event.getItem().getDurability(),
                    null
            ));
        }

        for (Action action : actions) {
            if (event.getPlayer().hasMetadata("rwm.redaction.actions." + action.getName().toLowerCase())) {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("click", action.getClick().toString());
                replacements.put("object", action.getObject().toString());
                replacements.put("world", event.getPlayer().getWorld().getName());
                replacements.put("x", String.valueOf(event.getPlayer().getLocation().getBlockX()));
                replacements.put("y", String.valueOf(event.getPlayer().getLocation().getBlockY()));
                replacements.put("z", String.valueOf(event.getPlayer().getLocation().getBlockZ()));
                replacements.put("exactx", String.valueOf(event.getPlayer().getLocation().getX()));
                replacements.put("exacty", String.valueOf(event.getPlayer().getLocation().getY()));
                replacements.put("exactz", String.valueOf(event.getPlayer().getLocation().getZ()));

                if (event.getClickedBlock() != null) {
                    replacements.put("blockx", String.valueOf(event.getClickedBlock().getLocation().getBlockX()));
                    replacements.put("blocky", String.valueOf(event.getClickedBlock().getLocation().getBlockY()));
                    replacements.put("blockz", String.valueOf(event.getClickedBlock().getLocation().getBlockZ()));
                    if (event.getClickedBlock().getState() instanceof Directional) {
                        replacements.put("direction", ((Directional) event.getClickedBlock().getState()).getFacing().toString());
                    }
                }

                plugin.execute(action, event.getPlayer(), replacements);
                if (action.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
