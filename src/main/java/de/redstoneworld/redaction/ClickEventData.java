package de.redstoneworld.redaction;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;

@Data
public class ClickEventData {
    private final ClickType click;
    private final Location clickedLocation;
    private final Material clickedMaterial;
    private final BlockData clickedData;
    private final BlockFace clickedDirection;
    private final EntityType entityType;
    private final Boolean isBaby;
    private final Material handItem;
    private final int handDamage;
    private final Material offhandItem;
    private final int offhandDamage;
    private final boolean sneaking;
    private final boolean cancelled;
}
