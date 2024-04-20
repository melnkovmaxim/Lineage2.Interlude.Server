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
package ru.agecold.gameserver.network.clientpackets;

//import java.util.logging.Logger;
import ru.agecold.Config;
import ru.agecold.gameserver.datatables.MapRegionTable;
import ru.agecold.gameserver.instancemanager.CastleManager;
import ru.agecold.gameserver.instancemanager.ClanHallManager;
import ru.agecold.gameserver.model.L2SiegeClan;
import ru.agecold.util.Location;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.entity.Castle;
import ru.agecold.gameserver.model.entity.ClanHall;
import ru.agecold.gameserver.model.entity.TvTEvent;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.Revive;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.util.IllegalPlayerAction;
import ru.agecold.gameserver.util.Util;
import ru.agecold.util.Rnd;
import scripts.autoevents.basecapture.BaseCapture;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestartPoint extends L2GameClientPacket {

    protected int _requestedPointType;
    protected boolean _continuation;
    private static final int TO_CLANHALL = 1;
    private static final int TO_CASTLE = 2;
    private static final int FIXED_OR_FESTIVEL = 4;

    @Override
    protected void readImpl() {
        _requestedPointType = readD();
    }

    @Override
    protected void runImpl() {
        L2PcInstance player = getClient().getActiveChar();

        if (player == null) {
            return;
        }

        if (player.isFakeDeath()) {
            player.stopFakeDeath(null);
            player.broadcastPacket(new Revive(player));
            return;
        } else if (!player.isAlikeDead()) {
            return;
        }

        if (TvTEvent.isStarted()) {
            if (TvTEvent.isPlayerParticipant(player.getName())) {
                player.sendCritMessage("��� �����: ��������� �����������.");
                return;
            }
        }

        if (Config.EBC_ENABLE && BaseCapture.getEvent().isInBattle(player)) {
            player.sendCritMessage("-������ ����-: ��������� �����������.");
            return;
        }

        if (player.isFestivalParticipant()) {
            _requestedPointType = 4;
        }

        try {
            Location loc = null;
            Castle castle = null;
            switch (_requestedPointType) {
                case TO_CLANHALL: // to clanhall
                    if (player.getClan().getHasHideout() == 0) {
                        return;
                    }
                    loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.ClanHall);
                    if (ClanHallManager.getInstance().getClanHallByOwner(player.getClan()) != null && ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null) {
                        player.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
                    }
                    break;
                case TO_CASTLE: // to castle
                    Boolean isInDefense = false;
                    castle = CastleManager.getInstance().getCastle(player);
                    if (castle != null && castle.getSiege().getIsInProgress()) {
                        //siege in progress
                        if (castle.getSiege().checkIsDefender(player.getClan())) {
                            isInDefense = true;
                        }
                    }
                    if (player.getClan().getHasCastle() == 0 && !isInDefense) {
                        return;
                    }
                    loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.Castle);
                    break;
                case FIXED_OR_FESTIVEL: // Fixed or Player is a festival participant
                    if (player.isInFixedZone()) {
                        loc = player.getFixedLoc();
                    } else {
                        if (!player.isGM() && !player.isFestivalParticipant()) {
                            return;
                        }
                        loc = new Location(player.getX(), player.getY(), player.getZ()); // spawn them where they died
                    }
                    break;
                default:
                    loc = MapRegionTable.getInstance().getTeleToLocation(player, MapRegionTable.TeleportWhereType.Town);
            }
            //Teleport and revive
            player.setIsPendingRevive(true);
            player.teleToLocation(loc, true);
        } catch (Exception e) {
        }
    }
}