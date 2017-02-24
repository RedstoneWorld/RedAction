package de.redstoneworld.redaction;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class RedAction extends JavaPlugin {

    private Map<Condition, Map<Material, List<String>>> actionTrigger;
    private Map<String, Action> actions;

    @Override
    public void onEnable() {
        loadConfig();
        getCommand("redaction").setExecutor(this);
        getServer().getPluginManager().registerEvents(new ActionListener(this), this);
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();

        actionTrigger = new HashMap<>();
        actions = new HashMap<>();

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
        actions.put(action.getName().toLowerCase(), action);
        actionTrigger.putIfAbsent(action.getCondition(), new HashMap<>());
        actionTrigger.get(action.getCondition()).putIfAbsent(action.getObject(), new ArrayList<>());
        actionTrigger.get(action.getCondition()).get(action.getObject()).add(action.getName().toLowerCase());
        try {
            getServer().getPluginManager().addPermission(new Permission("rwm.redaction.actions." + action.getName().toLowerCase(), PermissionDefault.FALSE));
        } catch (IllegalArgumentException ignored) {}
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

    public List<Action> getActions(Condition condition, ClickType click, Material type, short durability, BlockFace direction) {
        List<Action> actionList = new ArrayList<>();

        if (actionTrigger.containsKey(condition) && actionTrigger.get(condition).containsKey(type)) {
            for (String actionName : actionTrigger.get(condition).get(type)) {
                Action action = actions.get(actionName);
                if (action != null
                        && (action.getClick() == null || action.getClick() == click)
                        && (action.getDamage() < 0 || action.getDamage() == durability)
                        && (direction == null || action.getDirection() == null || action.getDirection() == direction)) {
                    actionList.add(action);
                }
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
        boolean wasOp = player.isOp();
        PermissionAttachment perm = player.addAttachment(this, "*", true);
        try {
            if (!wasOp) {
                player.setOp(true);
            }
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
        }
    }
}
