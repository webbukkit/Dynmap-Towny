package org.dynmap.towny.events;

import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when the marker icon for a town is chosen.
 */
public class TownSetMarkerIconEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Town town;
    private MarkerIcon icon;

    public TownSetMarkerIconEvent(Town town, MarkerIcon icon) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
        this.icon = icon;
    }

    public Town getTown() {
        return town;
    }

    public void setIcon(MarkerIcon icon) {
        this.icon = icon;
    }

    public MarkerIcon getIcon() {
        return icon;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
