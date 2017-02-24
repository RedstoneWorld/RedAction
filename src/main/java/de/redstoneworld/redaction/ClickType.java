package de.redstoneworld.redaction;

import org.bukkit.event.block.Action;

/**
 * Created by Max on 24.02.2017.
 */
public enum ClickType {
    LEFT,
    RIGHT;

    public static ClickType fromAction(Action action) {
        switch (action) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                return ClickType.LEFT;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                return ClickType.RIGHT;
            default:
                return null;
        }
    }
}
