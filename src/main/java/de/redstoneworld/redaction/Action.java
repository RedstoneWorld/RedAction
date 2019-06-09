package de.redstoneworld.redaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class Action {
    private final String name;

    private final Material clickedBlock;
    private final BlockData blockData;
    private final BlockFace blockDirection;
    private final EntityType clickedEntity;
    private final Boolean isClickedEntityBaby;
    private final Material handItem;
    private final int handDamage;
    private final Material offhandItem;
    private final int offhandDamage;

    private final List<String> commands;
    private final List<String> commandPermissions;
    private final boolean commandsAsOperator;
    private final boolean commandsAsConsole;
    private final boolean outputShown;
    private final ClickType click;
    private final boolean cancel;
    private final Boolean sneaking;
    private final Boolean cancelled;

    /**
     * Create a new action from the config
     * @param name      The name of the action
     * @param config    The config defining what the action is going to do
     * @throws IllegalArgumentException If some of the parameters are invalid like the click or object value
     */
    public Action(String name, ConfigurationSection config) throws IllegalArgumentException {
        this.name = name;

        Material clickedBlock = null;
        if (config.contains("clicked-block", true)) {
            clickedBlock = Material.valueOf(config.getString("clicked-block", "NULL").toUpperCase());
        }
        BlockData blockData = Bukkit.createBlockData(clickedBlock, config.getString("states", config.getString("block-data", "")));
        EntityType clickedEntity = null;
        if (config.contains("clicked-entity", true)) {
            clickedEntity = EntityType.valueOf(config.getString("clicked-entity", "NULL").toUpperCase());
        }
        isClickedEntityBaby = (Boolean) config.get("entity-is-baby", null);
        Material handItem = null;
        if (config.contains("hand-item", true)) {
            handItem = Material.valueOf(config.getString("hand-item", "NULL").toUpperCase());
        }
        int handDamage = config.getInt("hand-damage", config.getInt("hand-data", -1));
        Material offhandItem = null;
        if (config.contains("offhand-item", true)) {
            offhandItem = Material.valueOf(config.getString("offhand-item", "NULL").toUpperCase());
        }
        int offhandDamage = config.getInt("offhand-damage", config.getInt("offhand-data", -1));

        // Legacy support
        if (config.contains("object", true)) {
            Material object = Material.valueOf(config.getString("object", "NULL").toUpperCase());
            String condition = config.getString("condition", null);
            if ("hand".equalsIgnoreCase(condition)) {
                handItem = object;
                handDamage = config.getInt("damage", -1);
            } else if ("offhand".equalsIgnoreCase(condition)) {
                offhandItem = object;
                offhandDamage = config.getInt("damage", -1);
            } else if ("block".equalsIgnoreCase(condition)) {
                clickedBlock = object;
                if (config.isSet("damage")) {
                    throw new IllegalArgumentException("Block damage values are no longer supported! Use the 'states' setting.");
                }
            }
        }

        this.clickedBlock = clickedBlock;
        this.blockData = blockData;
        this.clickedEntity = clickedEntity;
        this.handItem = handItem;
        this.handDamage = handDamage;
        this.offhandItem = offhandItem;
        this.offhandDamage = offhandDamage;
        this.commands = config.getStringList("commands");
        this.commandPermissions = config.getStringList("command-permissions");
        this.commandsAsOperator = config.getBoolean("command-as-operator", true);
        this.commandsAsConsole = config.getBoolean("command-as-console", false);
        this.outputShown = config.getBoolean("output", true);
        if (config.contains("click", true)) {
            this.click = ClickType.valueOf(config.getString("click").toUpperCase());
        } else {
            this.click = null;
        }
        if (config.contains("direction", true)) {
            this.blockDirection = BlockFace.valueOf(config.getString("direction").toUpperCase());
        } else {
            this.blockDirection = null;
        }
        this.cancel = config.getBoolean("cancel", false);
        this.sneaking = (Boolean) config.get("sneaking", null);
        this.cancelled = (Boolean) config.get("cancelled", null);
    }
}
