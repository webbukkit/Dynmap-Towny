package org.dynmap.towny.mapupdate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.dynmap.towny.events.BuildTownFlagsEvent;
import org.dynmap.towny.settings.Settings;

import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.TownyFormatter;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.utils.TownRuinUtil;
import com.palmergames.util.StringMgmt;

public class TownInfoWindow {

    public static String formatInfoWindow(Town town) {
		String v = "<div class=\"regioninfo\">" + Settings.getTownInfoWindow() + "</div>";
		v = v.replace("%regionname%", town.getName());
        v = v.replace("%playerowners%", town.hasMayor()?town.getMayor().getName():"");
        String[] residents = town.getResidents().stream().map(obj -> obj.getName()).collect(Collectors.toList()).toArray(new String[0]);
		if (residents.length > 34) {
			String[] entire = residents;
			residents = new String[35 + 1];
			System.arraycopy(entire, 0, residents, 0, 35);
			residents[35] = "and more...";
		}
		
        String res = String.join(", ", residents);
        v = v.replace("%playermembers%", res);
        String mgrs = "";
        for(Resident r : town.getRank("assistant")) {
            if(mgrs.length()>0) mgrs += ", ";
            mgrs += r.getName();
        }
        v = v.replace("%playermanagers%", res);

        String dispNames = "";
        for (Resident r: town.getResidents()) {
            if(dispNames.length()>0) dispNames += ", ";
            dispNames += r.isOnline() ? r.getPlayer().getDisplayName() : r.getFormattedName();
        }
        v = v.replace("%residentdisplaynames%", dispNames);

        v = v.replace("%residentcount%", town.getResidents().size() + "");
        v = v.replace("%founded%", town.getRegistered() != 0 ? TownyFormatter.registeredFormat.format(town.getRegistered()) : "Not set");
        v = v.replace("%board%", town.getBoard());
        v = v.replace("%towntrusted%", town.getTrustedResidents().isEmpty() ? "None"
                : StringMgmt.join(town.getTrustedResidents().stream().map(trustedRes-> trustedRes.getName()).collect(Collectors.toList()), ", "));

        if (TownySettings.isUsingEconomy() && TownyEconomyHandler.isActive()) {
	        if (town.isTaxPercentage()) {
	            v = v.replace("%tax%", town.getTaxes() + "%");
	        } else {
	            v = v.replace("%tax%", TownyEconomyHandler.getFormattedBalance(town.getTaxes()));
	        }
	
	       	v = v.replace("%bank%", TownyEconomyHandler.getFormattedBalance(town.getAccount().getCachedBalance()));
            v = v.replace("%upkeep%", TownyEconomyHandler.getFormattedBalance(TownySettings.getTownUpkeepCost(town)));
        }
        String nation = town.hasNation() ? town.getNationOrNull().getName() : Settings.noNationSlug();

        v = v.replace("%nation%", nation);

		String natStatus = "";
        if (town.isCapital()) {
            natStatus = "Capital of " + nation;
        } else if (town.hasNation()) {
            natStatus = "Member of " + nation;
        }

        v = v.replace("%nationstatus%", natStatus);

        v = v.replace("%public%", town.isPublic() ? "true" : "false");
        v = v.replace("%peaceful%", town.isNeutral() ? "true" : "false");
        
        
        /* Build flags */
        List<String> flags = new ArrayList<>();
        flags.add("Has Upkeep: " + town.hasUpkeep());
        flags.add("pvp: " + town.isPVP());
        flags.add("mobs: " + town.hasMobs());
        flags.add("explosion: " + town.isExplosion());
        flags.add("fire: " + town.isFire());
        flags.add("nation: " + nation);

        if (TownySettings.getTownRuinsEnabled()) {
        	String ruinedString = "ruined: " + town.isRuined(); 
            if (town.isRuined())
            	ruinedString += " (Time left: " + (TownySettings.getTownRuinsMaxDurationHours() - TownRuinUtil.getTimeSinceRuining(town)) + " hours)";

           	flags.add(ruinedString);
        }

        BuildTownFlagsEvent buildTownFlagsEvent = new BuildTownFlagsEvent(town, flags);
        Bukkit.getPluginManager().callEvent(buildTownFlagsEvent);

        v = v.replace("%flags%", String.join("<br/>", buildTownFlagsEvent.getFlags()));

        return v;
    }
}
