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
    private final Condition condition;
    private final Material object;
    private final List<String> commands;
    private final boolean outputShown;
    private final ClickType click;
    private final BlockFace direction;
    private final int damage;
    private final boolean cancelled;
    private final Boolean sneaking;

    /**
     * Create a new action from the config
     * @param name      The name of the action
     * @param config    The config defining what the action is going to do
     * @throws IllegalArgumentException If some of the parameters are invalid like the click or object value
     */
    public Action(String name, ConfigurationSection config) throws IllegalArgumentException {
        this.name = name;
        this.condition = Condition.valueOf(config.getString("condition", "UNKNOWN").toUpperCase());
        this.object = Material.valueOf(config.getString("object", "UNKNOWN").toUpperCase());
        this.commands = config.getStringList("commands");
        this.outputShown = config.getBoolean("output", true);
        if (config.contains("click", true)) {
            this.click = ClickType.valueOf(config.getString("click").toUpperCase());
        } else {
            this.click = null;
        }
        if (config.contains("direction", true)) {
            this.direction = BlockFace.valueOf(config.getString("direction").toUpperCase());
        } else {
            this.direction = null;
        }
        this.damage = config.getInt("damage", -1);
        this.cancelled = config.getBoolean("cancel", false);
        this.sneaking = (Boolean) config.get("sneaking", null);
    }
}
