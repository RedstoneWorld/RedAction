package de.redstoneworld.redaction;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class RedAction extends JavaPlugin {

    private List<Action> actions;
    private boolean debug;

    @Override
    public void onEnable() {
        loadConfig();
        getCommand("redaction").setExecutor(this);
        getServer().getPluginManager().registerEvents(new ActionListener(this), this);
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();

        debug = getConfig().getBoolean("debug");

        actions = new ArrayList<>();

        ConfigurationSection actionSection = getConfig().getConfigurationSection("actions");
        for (String actionName : actionSection.getKeys(false)) {
            try {
                registerAction(new Action(actionName, actionSection.getConfigurationSection(actionName)));
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.WARNING, "Action " + actionName + " has an invalid config! " + e.getMessage());
            }
        }
    }

    private void registerAction(Action action) {
        actions.add(action);
        try {
            getServer().getPluginManager().addPermission(new Permission("rwm.redaction.actions." + action.getName().toLowerCase(), PermissionDefault.FALSE));
        } catch (IllegalArgumentException ignored) {}
        getLogger().log(Level.INFO, "Registered " + action);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            if ("reload".equalsIgnoreCase(args[0]) && sender.hasPermission("rwm.redaction.command.reload")) {
                loadConfig();
                sender.sendMessage(ChatColor.YELLOW + "Config reloaded!");
                return true;
            }
        }
        return false;
    }

    public List<Action> getActions(ClickType click, Material clickedBlock, byte blockData, BlockFace blockDirection, EntityType entityType, boolean baby, Material handItem, byte handData, Material offhandItem, byte offhandData, boolean sneaking, boolean cancelled) {
        List<Action> actionList = new ArrayList<>();

        for (Action action : actions) {
            if (action != null
                    && (action.getClick() == null || action.getClick() == click)
                    && (action.getClickedBlock() == null || action.getClickedBlock() == clickedBlock)
                    && (action.getClickedEntity() == null || action.getClickedEntity() == entityType)
                    && (action.getClickedEntity() == null || entityType == null || action.isClickedEntityBaby() == baby)
                    && (action.getHandItem() == null || action.getHandItem() == handItem)
                    && (action.getOffhandItem() == null || action.getOffhandItem() == offhandItem)
                    && (blockData == -1 || action.getBlockData() < 0 || action.getBlockData() == blockData)
                    && (handData == -1 || action.getBlockData() < 0 || action.getBlockData() == blockData)
                    && (blockData == -1 || action.getBlockData() < 0 || action.getBlockData() == blockData)
                    && (blockDirection == null || action.getBlockDirection() == null || action.getBlockDirection() == blockDirection)
                    && (action.getSneaking() == null || action.getSneaking().booleanValue() == sneaking)
                    && (action.getCancelled() == null || action.getCancelled().booleanValue() == cancelled)) {
                actionList.add(action);
            }
        }

        return actionList;
    }

    /**
     * Make a player execute this action
     * @param action        The action to execute
     * @param player        The player who executes it
     * @param replacements  A map of variables mapped to their replacements
     */
    public void execute(Action action, Player player, Map<String, String> replacements) {
        logDebug(player.getName() + " executes " + action);
        boolean wasOp = player.isOp();
        PermissionAttachment perm = player.addAttachment(this, "*", true);
        String sendCommandFeedback = player.getWorld().getGameRuleValue("sendCommandFeedback");
        try {
            if (!wasOp) {
                player.setOp(true);
            }
            player.getWorld().setGameRuleValue("sendCommandFeedback", String.valueOf(action.isOutputShown()));
            for (String command : action.getCommands()) {
                String replacedCommand = command;
                for (Map.Entry<String, String> replacement : replacements.entrySet()) {
                    replacedCommand = replacedCommand.replace("%" + replacement.getKey() + "%", replacement.getValue());
                }
                player.getServer().dispatchCommand(player, replacedCommand);
            }
        } finally {
            if (!wasOp) {
                player.setOp(false);
            }
            player.removeAttachment(perm);
            player.getWorld().setGameRuleValue("sendCommandFeedback", sendCommandFeedback);
        }
    }

    private void logDebug(String message) {
        if (debug) {
            getLogger().log(Level.INFO, "Debug: " + message);
        }
    }
}
