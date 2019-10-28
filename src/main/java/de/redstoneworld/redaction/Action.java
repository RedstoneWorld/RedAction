package de.redstoneworld.redaction;

import com.destroystokyo.paper.MaterialTags;
import de.redstoneworld.redaction.utils.ObjectIndex.Index;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
@ToString
public class Action {
    private final String name;

    @Index
    private final ClickType click;
    @Index
    private final Boolean sneaking;
    @Index
    private final Boolean cancelled;
    @Index
    private final EntityType clickedEntity;
    @Index
    private final Boolean isClickedEntityBaby;
    @Index
    private final Map<Material, BlockData> clickedBlocks = new EnumMap<>(Material.class);
    @Index
    private final BlockFace blockDirection;
    @Index
    private final Set<Material> handItems = EnumSet.noneOf(Material.class);
    @Index
    private final int handDamage;
    @Index
    private final Set<Material> offhandItems = EnumSet.noneOf(Material.class);
    @Index
    private final int offhandDamage;

    private final List<String> commands;
    private final List<String> commandPermissions;
    private final boolean commandsAsOperator;
    private final boolean commandsAsConsole;
    private final boolean outputShown;
    private final boolean cancel;

    /**
     * Create a new action from the config
     * @param name      The name of the action
     * @param config    The config defining what the action is going to do
     * @throws IllegalArgumentException If some of the parameters are invalid like the click or object value
     */
    public Action(String name, ConfigurationSection config) throws IllegalArgumentException {
        this.name = name;

        String blockData = config.getString("states", config.getString("block-data", ""));
        if (config.contains("clicked-block", true)) {
            for (Material material : getMaterials(config.getString("clicked-block", "NULL"))) {
                clickedBlocks.put(material, material.createBlockData(blockData));
            }
        }
        EntityType clickedEntity = null;
        if (config.contains("clicked-entity", true)) {
            clickedEntity = EntityType.valueOf(config.getString("clicked-entity", "NULL").toUpperCase());
        }
        isClickedEntityBaby = (Boolean) config.get("entity-is-baby", null);
        if (config.contains("hand-item", true)) {
            handItems.addAll(getMaterials(config.getString("hand-item", "NULL")));
        }
        int handDamage = config.getInt("hand-damage", config.getInt("hand-data", -1));
        if (config.contains("offhand-item", true)) {
            offhandItems.addAll(getMaterials(config.getString("offhand-item", "NULL")));
        }
        int offhandDamage = config.getInt("offhand-damage", config.getInt("offhand-data", -1));

        // Legacy support
        if (config.contains("object", true)) {
            Material object = Material.valueOf(config.getString("object", "NULL").toUpperCase());
            String condition = config.getString("condition", null);
            if ("hand".equalsIgnoreCase(condition)) {
                handItems.add(object);
                handDamage = config.getInt("damage", -1);
            } else if ("offhand".equalsIgnoreCase(condition)) {
                offhandItems.add(object);
                offhandDamage = config.getInt("damage", -1);
            } else if ("block".equalsIgnoreCase(condition)) {
                clickedBlocks.put(object, object.createBlockData());
                if (config.isSet("damage")) {
                    throw new IllegalArgumentException("Block damage values are no longer supported! Use the 'states' setting.");
                }
            }
        }
        this.clickedEntity = clickedEntity;
        this.handDamage = handDamage;
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

    private static Collection<? extends Material> getMaterials(String blockStr) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        for (String s : blockStr.toUpperCase().split(",")) {
            if (s.startsWith("tag=")) {
                String nameSpace = NamespacedKey.MINECRAFT;
                String tagName = s.substring(4);
                String[] parts = tagName.split(":");
                if (parts.length == 2) {
                    nameSpace = parts[0].toLowerCase();
                    tagName = parts[1].toLowerCase();
                }
                Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, new NamespacedKey(nameSpace, tagName), Material.class);
                if (tag == null) {
                    tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, new NamespacedKey(nameSpace, tagName), Material.class);
                }
                if (tag == null) {
                    try {
                        Field field = MaterialTags.class.getField(tagName);
                        tag = (Tag<Material>) field.get(null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }
                }

                if (tag != null) {
                    materials.addAll(tag.getValues());
                }
            } else if (s.startsWith("r=") || s.contains("*")) {
                Pattern p = Pattern.compile(s.startsWith("r=") ? s.substring(2) : s.replace("*", "(.*)"));
                for (Material material : Material.values()) {
                    if (p.matcher(material.name()).matches()) {
                        materials.add(material);
                    }
                }
            } else {
                materials.add(Material.valueOf(s));
            }
        }
        return materials;
    }
}
