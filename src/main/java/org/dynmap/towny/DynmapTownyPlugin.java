package org.dynmap.towny;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.dynmap.markers.Marker;

import com.palmergames.bukkit.towny.NotRegisteredException;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class DynmapTownyPlugin extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    private static final String LOG_PREFIX = "[Dynmap-Towny] ";
    private static final String DEF_INFOWINDOW = "<div class=\"infowindow\"><span style=\"font-size:120%;\">%regionname%</span><br /> Owner <span style=\"font-weight:bold;\">%playerowners%</span><br />Flags<br /><span style=\"font-weight:bold;\">%flags%</span></div>";
    DynmapAPI api;
    MarkerAPI markerapi;
    Towny towny;
    TownyUniverse tuniv;
    
    FileConfiguration cfg;
    MarkerSet set;
    long updperiod;
    boolean use3d;
    String infowindow;
    AreaStyle defstyle;
    Map<String, AreaStyle> cusstyle;
    Set<String> visible;
    Set<String> hidden;
    
    private static class AreaStyle {
        String strokecolor;
        double strokeopacity;
        int strokeweight;
        String fillcolor;
        double fillopacity;

        AreaStyle(FileConfiguration cfg, String path, AreaStyle def) {
            strokecolor = cfg.getString(path+".strokeColor", def.strokecolor);
            strokeopacity = cfg.getDouble(path+".strokeOpacity", def.strokeopacity);
            strokeweight = cfg.getInt(path+".strokeWeight", def.strokeweight);
            fillcolor = cfg.getString(path+".fillColor", def.fillcolor);
            fillopacity = cfg.getDouble(path+".fillOpacity", def.fillopacity);
        }

        AreaStyle(FileConfiguration cfg, String path) {
            strokecolor = cfg.getString(path+".strokeColor", "#FF0000");
            strokeopacity = cfg.getDouble(path+".strokeOpacity", 0.8);
            strokeweight = cfg.getInt(path+".strokeWeight", 3);
            fillcolor = cfg.getString(path+".fillColor", "#FF0000");
            fillopacity = cfg.getDouble(path+".fillOpacity", 0.35);
        }
    }
    
    public static void info(String msg) {
        log.log(Level.INFO, LOG_PREFIX + msg);
    }
    public static void severe(String msg) {
        log.log(Level.SEVERE, LOG_PREFIX + msg);
    }

    private class TownyUpdate implements Runnable {
        public void run() {
            updateTowns();
        }
    }
    
    private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();

    private String formatInfoWindow(Town town) {
        String v = "<div class=\"regioninfo\">"+infowindow+"</div>";
        v = v.replaceAll("%regionname%", town.getName());
        v = v.replaceAll("%playerowners%", town.hasMayor()?town.getMayor().getName():"");
        String res = "";
        for(Resident r : town.getResidents()) {
        	if(res.length()>0) res += ",";
        	res += r.getName();
        }
        v = v.replaceAll("%playermembers%", res);
        String nation = "";
		try {
			if(town.hasNation())
				nation = town.getNation().getName();
		} catch (NotRegisteredException e) {
		}
        v = v.replaceAll("%nation%", nation);
        /* Build flags */
        String flgs = "hasUpkeep: " + town.hasUpkeep();
        flgs += "<br/>pvp: " + town.isPVP();
        flgs += "<br/>mobs: " + town.hasMobs();
        flgs += "<br/>public: " + town.isPublic();
        flgs += "<br/>explosion: " + town.isBANG();
        flgs += "<br/>fire: " + town.isFire();
        v = v.replaceAll("%flags%", flgs);
        return v;
    }
    
    private boolean isVisible(String id) {
        if((visible != null) && (visible.size() > 0)) {
            if(visible.contains(id) == false) {
                return false;
            }
        }
        if((hidden != null) && (hidden.size() > 0)) {
            if(hidden.contains(id))
                return false;
        }
        return true;
    }
    
    private void addStyle(String resid, AreaMarker m) {
        AreaStyle as = cusstyle.get(resid);
        if(as == null)
            as = defstyle;
        int sc = 0xFF0000;
        int fc = 0xFF0000;
        try {
            sc = Integer.parseInt(as.strokecolor.substring(1), 16);
            fc = Integer.parseInt(as.fillcolor.substring(1), 16);
        } catch (NumberFormatException nfx) {
        }
        m.setLineStyle(as.strokeweight, as.strokeopacity, sc);
        m.setFillStyle(as.fillopacity, fc);
    }
    
    /* Handle specific town */
    private void handleTown(Town town, Map<String, AreaMarker> newmap) {
        String name = town.getName();
        double[] x = null;
        double[] z = null;
        
        /* Build popup */
        String desc = formatInfoWindow(town);
        
        /* Handle areas */
        if(isVisible(name)) {
        	List<TownBlock> blocks = town.getTownBlocks();
        	
        	for(TownBlock b : blocks) {
        		b.
        	}
            String id = region.getId();
            String tn = region.getTypeName();
            BlockVector l0 = region.getMinimumPoint();
            BlockVector l1 = region.getMaximumPoint();

            if(tn.equalsIgnoreCase("cuboid")) { /* Cubiod region? */
                /* Make outline */
                x = new double[4];
                z = new double[4];
                x[0] = l0.getX(); z[0] = l0.getZ();
                x[1] = l0.getX(); z[1] = l1.getZ()+1.0;
                x[2] = l1.getX() + 1.0; z[2] = l1.getZ()+1.0;
                x[3] = l1.getX() + 1.0; z[3] = l0.getZ();
            }
            else if(tn.equalsIgnoreCase("polygon")) {
                ProtectedPolygonalRegion ppr = (ProtectedPolygonalRegion)region;
                List<BlockVector2D> points = ppr.getPoints();
                x = new double[points.size()];
                z = new double[points.size()];
                for(int i = 0; i < points.size(); i++) {
                    BlockVector2D pt = points.get(i);
                    x[i] = pt.getX(); z[i] = pt.getZ();
                }
            }
            else {  /* Unsupported type */
                return;
            }
            AreaMarker m = resareas.remove(id); /* Existing area? */
            if(m == null) {
                m = set.createAreaMarker(id, name, false, world.getName(), x, z, false);
                if(m == null)
                    return;
            }
            else {
                m.setCornerLocations(x, z); /* Replace corner locations */
                m.setLabel(name);   /* Update label */
            }
            if(use3d) { /* If 3D? */
                m.setRangeY(l1.getY()+1.0, l0.getY());
            }
            m.setDescription(desc); /* Set popup */
            
            /* Set line and fill properties */
            addStyle(id, m);

            /* Add to map */
            newmap.put(id, m);
        }
    }
    
    /* Update Towny information */
    private void updateTowns() {
        Map<String,AreaMarker> newmap = new HashMap<String,AreaMarker>(); /* Build new map */
 
        /* Loop through towns */
        List<Town> towns = tuniv.getTowns();
        for(Town t : towns) {
    		handleTown(t, newmap);
        }
        /* Now, review old map - anything left is gone */
        for(AreaMarker oldm : resareas.values()) {
            oldm.deleteMarker();
        }
        /* And replace with new map */
        resareas = newmap;
        
        getServer().getScheduler().scheduleSyncDelayedTask(this, new TownyUpdate(), updperiod);
        
    }
    
    public void onEnable() {
        Plugin p = this.getServer().getPluginManager().getPlugin("dynmap"); /* Find dynmap */
        if(p == null) {
            severe("Error loading dynmap API!");
            return;
        }
        api = (DynmapAPI)p; /* Get API */
        /* Now, get markers API */
        markerapi = api.getMarkerAPI();
        if(markerapi == null) {
            severe("Error loading dynmap marker API!");
            return;
        }
        /* Find Towny */
        p = this.getServer().getPluginManager().getPlugin("Towny");
        if(p == null) {
            severe("Error loading Towny");
            return;
        }
        /* Connect to towny API */
        towny = (Towny)p;
        tuniv = towny.getTownyUniverse();
        
        /* Load configuration */
        FileConfiguration cfg = getConfig();
        cfg.options().copyDefaults(true);   /* Load defaults, if needed */
        this.saveConfig();  /* Save updates, if needed */
        
        /* Now, add marker set for mobs (make it transient) */
        set = markerapi.createMarkerSet("towny.markerset", cfg.getString("layer.name", "Towny"), null, false);
        if(set == null) {
            severe("Error creating marker set");
            return;
        }
        set.setLayerPriority(cfg.getInt("layer.layerprio", 10));
        set.setHideByDefault(cfg.getBoolean("layer.hidebydefault", false));
        use3d = cfg.getBoolean("use3dregions", false);
        infowindow = cfg.getString("infowindow", DEF_INFOWINDOW);

        /* Get style information */
        defstyle = new AreaStyle(cfg, "regionstyle");
        cusstyle = new HashMap<String, AreaStyle>();
        ConfigurationSection sect = cfg.getConfigurationSection("custstyle");
        if(sect != null) {
            Set<String> ids = sect.getKeys(false);
            
            for(String id : ids) {
                cusstyle.put(id, new AreaStyle(cfg, "custstyle." + id, defstyle));
            }
        }
        List vis = cfg.getList("visibleregions");
        if(vis != null) {
            visible = new HashSet<String>(vis);
        }
        List hid = cfg.getList("hiddenregions");
        if(hid != null) {
            hidden = new HashSet<String>(hid);
        }

        /* Set up update job - based on periond */
        int per = cfg.getInt("update.period", 300);
        if(per < 15) per = 15;
        updperiod = (long)(per*20);
        
        getServer().getScheduler().scheduleSyncDelayedTask(this, new TownyUpdate(), 40);   /* First time is 2 seconds */
        
        info("version " + this.getDescription().getVersion() + " is enabled");
    }

    public void onDisable() {
        if(set != null) {
            set.deleteMarkerSet();
            set = null;
        }
        resareas.clear();
    }

}
