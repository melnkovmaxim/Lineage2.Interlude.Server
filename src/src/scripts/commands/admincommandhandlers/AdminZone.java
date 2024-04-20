/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package scripts.commands.admincommandhandlers;

import java.util.StringTokenizer;

import ru.agecold.Config;
import ru.agecold.gameserver.GmListTable;
import ru.agecold.gameserver.datatables.MapRegionTable;
import scripts.commands.IAdminCommandHandler;
import ru.agecold.gameserver.model.L2Character;
import ru.agecold.util.Location;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;

public class AdminZone implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = Config.GM_TEST;
    private static final String[] ADMIN_COMMANDS =
    {
        "admin_zone_check", "admin_zone_reload"
    };

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, ru.agecold.gameserver.model.L2PcInstance)
     */
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (activeChar == null) return false;

        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (activeChar.getAccessLevel() < REQUIRED_LEVEL) return false;

        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken(); // Get actual command

        //String val = "";
        //if (st.countTokens() >= 1) {val = st.nextToken();}

        if (actualCommand.equalsIgnoreCase("admin_zone_check"))
        {
            if (activeChar.isInsideZone(L2Character.ZONE_PVP))
            	activeChar.sendAdmResultMessage("This is a PvP zone.");
            else
            	activeChar.sendAdmResultMessage("This is NOT a PvP zone.");

            if (activeChar.isInsideZone(L2Character.ZONE_NOLANDING))
            	activeChar.sendAdmResultMessage("This is a no landing zone.");
            else
            	activeChar.sendAdmResultMessage("This is NOT a no landing zone.");

            activeChar.sendAdmResultMessage("MapRegion: x:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionTable.getInstance().getMapRegionX(activeChar.getY()));

            activeChar.sendAdmResultMessage("Closest Town: " + MapRegionTable.getInstance().getClosestTownName(activeChar));

            Location loc;

            loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
            activeChar.sendAdmResultMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

            loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
            activeChar.sendAdmResultMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

            loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
            activeChar.sendAdmResultMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

            loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);
            activeChar.sendAdmResultMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
        } else if (actualCommand.equalsIgnoreCase("admin_zone_reload"))
        {
        	//TODO: ZONETODO ZoneManager.getInstance().reload();
        	GmListTable.broadcastMessageToGMs("Zones can not be reloaded in this version.");
        }
        return true;
    }

    /* (non-Javadoc)
     * @see ru.agecold.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

}
