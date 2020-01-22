package de.redstoneworld.redaction;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;

public class ClickEventData {
    private final ClickType click;
    private final Location clickedLocation;
    private final Material clickedMaterial;
    private final BlockData clickedData;
    private final BlockFace clickedDirection;
    private final EntityType entityType;
    private final Boolean baby;
    private final Material handItem;
    private final int handDamage;
    private final Material offhandItem;
    private final int offhandDamage;
    private final boolean sneaking;
    private final boolean cancelled;

    public ClickEventData(ClickType click, Location clickedLocation, BlockFace clickedDirection, Material clickedMaterial, BlockData clickedData, EntityType entityType, Boolean baby, Material handItem, int handDamage, Material offhandItem, int offhandDamage, boolean sneaking, boolean cancelled) {
        this.click = click;
        this.clickedLocation = clickedLocation;
        this.clickedMaterial = clickedMaterial;
        this.clickedData = clickedData;
        this.clickedDirection = clickedDirection;
        this.entityType = entityType;
        this.baby = baby;
        this.handItem = handItem;
        this.handDamage = handDamage;
        this.offhandItem = offhandItem;
        this.offhandDamage = offhandDamage;
        this.sneaking = sneaking;
        this.cancelled = cancelled;
    }

    public ClickType getClick() {
        return click;
    }

    public Location getClickedLocation() {
        return clickedLocation;
    }

    public Material getClickedMaterial() {
        return clickedMaterial;
    }

    public BlockData getClickedData() {
        return clickedData;
    }

    public BlockFace getClickedDirection() {
        return clickedDirection;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Boolean isBaby() {
        return baby;
    }

    public Material getHandItem() {
        return handItem;
    }

    public int getHandDamage() {
        return handDamage;
    }

    public Material getOffhandItem() {
        return offhandItem;
    }

    public int getOffhandDamage() {
        return offhandDamage;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
