package de.redstoneworld.redaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class Action {
    private final String name;

    private final Material clickedBlock;
    private final int blockData;
    private final BlockFace blockDirection;
    private final Material handItem;
    private final int handData;
    private final Material offhandItem;
    private final int offhandData;

    private final List<String> commands;
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
            clickedBlock = Material.valueOf(config.getString("clicked-block", "UNKNOWN").toUpperCase());
        }
        int blockData = config.getInt("block-data", -1);
        Material handItem = null;
        if (config.contains("hand-item", true)) {
            handItem = Material.valueOf(config.getString("hand-item", "UNKNOWN").toUpperCase());
        }
        int handData = config.getInt("hand-data", -1);
        Material offhandItem = null;
        if (config.contains("offhand-item", true)) {
            offhandItem = Material.valueOf(config.getString("offhand-item", "UNKNOWN").toUpperCase());
        }
        int offhandData = config.getInt("offhand-data", -1);

        // Legacy support
        if (config.contains("object", true)) {
            Material object = Material.valueOf(config.getString("object", "UNKNOWN").toUpperCase());
            String condition = config.getString("condition", null);
            if ("hand".equalsIgnoreCase(condition)) {
                handItem = object;
                handData = config.getInt("damage", -1);
            } else if ("offhand".equalsIgnoreCase(condition)) {
                offhandItem = object;
                offhandData = config.getInt("damage", -1);
            } else if ("block".equalsIgnoreCase(condition)) {
                clickedBlock = object;
                blockData = config.getInt("damage", -1);
            }
        }

        this.clickedBlock = clickedBlock;
        this.blockData = blockData;
        this.handItem = handItem;
        this.handData = handData;
        this.offhandItem = offhandItem;
        this.offhandData = offhandData;
        this.commands = config.getStringList("commands");
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
