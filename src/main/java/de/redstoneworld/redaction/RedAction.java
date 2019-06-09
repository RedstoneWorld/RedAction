package de.redstoneworld.redaction;

import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
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

    public List<Action> getActions(ClickType click, Material clickedBlock, BlockData blockData, BlockFace blockDirection, EntityType entityType, Boolean baby, Material handItem, int handDamage, Material offhandItem, int offhandDamage, boolean sneaking, boolean cancelled) {
        List<Action> actionList = new ArrayList<>();

        for (Action action : actions) {
            if (action != null
                    && (action.getClick() == null || action.getClick() == click)
                    && (action.getClickedBlock() == null || action.getClickedBlock() == clickedBlock)
                    && (action.getClickedEntity() == null || action.getClickedEntity() == entityType)
                    && (action.getClickedEntity() == null || entityType == null || action.getIsClickedEntityBaby() == null || action.getIsClickedEntityBaby() == baby)
                    && (action.getHandItem() == null || action.getHandItem() == handItem)
                    && (action.getOffhandItem() == null || action.getOffhandItem() == offhandItem)
                    && (handDamage == -1 || action.getHandDamage() < 0 || action.getHandDamage() == handDamage)
                    && (offhandDamage == -1 || action.getOffhandDamage() < 0 || action.getOffhandDamage() == offhandDamage)
                    && (action.getBlockDirection() == null || action.getBlockDirection() == blockDirection)
                    && (action.getSneaking() == null || action.getSneaking() == sneaking)
                    && (action.getCancelled() == null || action.getCancelled() == cancelled)
                    && (blockData == null || action.getBlockData() == null || blockData.matches(action.getBlockData()))) {
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
        PermissionAttachment perm = player.addAttachment(this);
        for (String commandPerm : action.getCommandPermissions()) {
            perm.setPermission(replaceReplacements(commandPerm, replacements), !commandPerm.startsWith("-") && !commandPerm.startsWith("!"));
        }
        Boolean sendCommandFeedback = player.getWorld().getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK);
        try {
            if (action.isCommandsAsOperator() && !wasOp) {
                player.setOp(true);
            }
            player.getWorld().setGameRule(GameRule.SEND_COMMAND_FEEDBACK, action.isOutputShown());
            for (String command : action.getCommands()) {
                player.getServer().dispatchCommand(action.isCommandsAsConsole() ? getServer().getConsoleSender() : player,
                        replaceReplacements(command, replacements));
            }
        } finally {
            if (action.isCommandsAsOperator() && !wasOp) {
                player.setOp(false);
            }
            player.removeAttachment(perm);
            player.getWorld().setGameRule(GameRule.SEND_COMMAND_FEEDBACK, sendCommandFeedback != null ? sendCommandFeedback : true);
        }
    }

    private String replaceReplacements(String string, Map<String, String> replacements) {
        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            string = string.replace("%" + replacement.getKey() + "%", replacement.getValue());
        }
        return string;
    }

    private void logDebug(String message) {
        if (debug) {
            getLogger().log(Level.INFO, "Debug: " + message);
        }
    }
}
