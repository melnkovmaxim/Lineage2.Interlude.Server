/*
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
package ru.agecold.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.datatables.CustomServerData;
import ru.agecold.gameserver.datatables.TeleportLocationTable;
import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.instancemanager.GrandBossManager;
import ru.agecold.gameserver.instancemanager.SiegeManager;
import ru.agecold.gameserver.instancemanager.TownManager;
import ru.agecold.gameserver.instancemanager.ZoneManager;
import ru.agecold.gameserver.model.L2TeleportLocation;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.templates.L2NpcTemplate;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 *
 */
public final class L2TeleporterInstance extends L2FolkInstance {
    //private static Logger _log = Logger.getLogger(L2TeleporterInstance.class.getName());

    private static final int COND_ALL_FALSE = 0;
    private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    private static final int COND_OWNER = 2;
    private static final int COND_REGULAR = 3;

    /**
     * @param template
     */
    public L2TeleporterInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        ZoneManager.addDwZone(getX() - 55, getY() - 55, 110, 110);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command) {
        player.sendActionFailed();

        int condition = validateCondition(player);

        StringTokenizer st = new StringTokenizer(command, " ");
        String cmd = st.nextToken(); // Get actual command

        int npcId = getTemplate().npcId;
        if (cmd.equalsIgnoreCase("goto")) {
            switch (npcId) {
                case 31095: //
                case 31096: //
                case 31097: //
                case 31098: // Enter Necropolises
                case 31099: //
                case 31100: //
                case 31101: //
                case 31102: //

                case 31114: //
                case 31115: //
                case 31116: // Enter Catacombs
                case 31117: //
                case 31118: //
                case 31119: //
                    player.setIsIn7sDungeon(true);
                    break;
                case 31103: //
                case 31104: //
                case 31105: //
                case 31106: // Exit Necropolises
                case 31107: //
                case 31108: //
                case 31109: //
                case 31110: //

                case 31120: //
                case 31121: //
                case 31122: // Exit Catacombs
                case 31123: //
                case 31124: //
                case 31125: //
                    player.setIsIn7sDungeon(false);
                    break;
            }

            if (st.countTokens() <= 0) {
                return;
            }
            int whereTo = Integer.parseInt(st.nextToken());
            if (condition == COND_REGULAR) {
                doTeleport(player, whereTo);
                return;
            } else if (condition == COND_OWNER) {
                int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
                if (st.countTokens() >= 1) {
                    minPrivilegeLevel = Integer.parseInt(st.nextToken());
                }
                if (10 >= minPrivilegeLevel) // NOTE: Replace 10 with privilege level of player
                {
                    doTeleport(player, whereTo);
                } else {
                    player.sendMessage("You don't have the sufficient access level to teleport there.");
                }
                return;
            }
        }
        super.onBypassFeedback(player, command);
    }

    @Override
    public String getHtmlPath(int npcId, int val) {
        String pom = "";
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }

        return "data/html/teleporter/" + pom + ".htm";
    }

    @Override
    public void showChatWindow(L2PcInstance player) {
        String filename = "data/html/teleporter/castleteleporter-no.htm";

        int condition = validateCondition(player);
        if (condition == COND_REGULAR) {
            super.showChatWindow(player);
            return;
        } else if (condition > COND_ALL_FALSE) {
            if (condition == COND_BUSY_BECAUSE_OF_SIEGE) {
                filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
            } else if (condition == COND_OWNER) // Clan owns castle
            {
                filename = getHtmlPath(getNpcId(), 0); // Owner message window
            }
        }

        NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html = GrandBossManager.getHtmlRespawns(html);
        player.sendPacket(html);
    }

    private void doTeleport(L2PcInstance player, int val) {
        if (player.isAlikeDead()) {
            return;
        }

        L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
        if (list == null) {

            _log.warning("No teleport destination with id:" + val);
            player.sendPacket(Static.NO_PORT_THAT_IS_IN_SIGE);
            return;
        }
        //you cannot teleport to village that is in siege
        if (Config.CHECK_SIEGE_TELE && SiegeManager.getInstance().getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null) {
            player.sendPacket(Static.NO_PORT_THAT_IS_IN_SIGE);
            return;
        }
        if (Config.CHECK_SIEGE_TELE && TownManager.getInstance().townHasCastleInSiege(list.getLocX(), list.getLocY())) {
            player.sendPacket(Static.NO_PORT_THAT_IS_IN_SIGE);
            return;
        }
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) //karma
        {
            player.sendPacket(Static.NO_KARRMA_TELE);
            return;
        }
        if (list.getIsForNoble() && !player.isNoble()) {
            String filename = "data/html/teleporter/nobleteleporter-no.htm";
            NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
            html.setFile(filename);
            html.replace("%objectId%", String.valueOf(getObjectId()));
            html.replace("%npcname%", getName());
            player.sendPacket(html);
            return;
        }

        if (CustomServerData.getInstance().isSpecialTeleDenied(player, val)) {
            player.sendMessage("������� ����������� ������ ��� ������������.");
            return;
        }

        if (!list.getIsForNoble()) {
            if (!hasTicket(player, player.getLevel() < Config.ALT_GAME_FREE_TELEPORT, 57, list.getPrice())) {
                return;
            }

            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
        } else if (list.getTeleId() == 9983 && list.getTeleId() == 9984 && getNpcId() == 30483 && player.getLevel() > Config.CRUMA_TOWER_LEVEL_RESTRICT) {
            // Chars level XX can't enter in Cruma Tower. Retail: level 56 and above
            String filename = "data/html/teleporter/30483-biglvl.htm";
            NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
            html.setFile(filename);
            html.replace("%allowedmaxlvl%", "" + Config.CRUMA_TOWER_LEVEL_RESTRICT + "");
            player.sendPacket(html);
        } else if (list.getIsForNoble()) {
            if (!hasTicket(player, player.getLevel() < Config.ALT_GAME_FREE_TELEPORT, 6651, list.getPrice())) {
                return;
            }

            player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), true);
        }

        player.sendActionFailed();
    }

    private boolean hasTicket(L2PcInstance player, boolean free, int itemID, int price) {
        if (free || price == 0) {
            return true;
        }

        return player.destroyItemByItemId("Teleport", itemID, price, this, true);
    }

    private int validateCondition(L2PcInstance player) {
        if (CastleManager.getInstance().getCastleIndex(this) < 0) // Teleporter isn't on castle ground
        {
            return COND_REGULAR; // Regular access
        } else if (getCastle().getSiege().getIsInProgress()) // Teleporter is on castle ground and siege is in progress
        {
            return Config.CHECK_SIEGE_TELE ? COND_REGULAR : COND_BUSY_BECAUSE_OF_SIEGE;                 // Busy because of siege
        } else if (player.getClan() != null) // Teleporter is on castle ground and player is in a clan
        {
            if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
            {
                return COND_OWNER; // Owner
            }
        }

        return COND_ALL_FALSE;
    }

    @Override
    public boolean isL2Teleporter() {
        return true;
    }
}
