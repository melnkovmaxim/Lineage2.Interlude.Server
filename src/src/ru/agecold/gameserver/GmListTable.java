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
package ru.agecold.gameserver;

import javolution.util.FastList;
import javolution.util.FastMap;
import java.util.logging.Logger;

import ru.agecold.Config;
import ru.agecold.gameserver.cache.Static;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.CreatureSay;
import ru.agecold.gameserver.network.serverpackets.L2GameServerPacket;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.util.log.AbstractLogger;

/**
 * This class stores references to all online game masters. (access level > 100)
 * 
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable {

    private static Logger _log = AbstractLogger.getLogger(GmListTable.class.getName());
    private static GmListTable _instance;
    /** Set(L2PcInstance>) containing all the GM in game */
    private FastMap<L2PcInstance, Boolean> _gmList;

    public static GmListTable getInstance() {
        if (_instance == null) {
            _instance = new GmListTable();
        }
        return _instance;
    }

    public FastList<L2PcInstance> getAllGms(boolean includeHidden) {
        FastList<L2PcInstance> tmpGmList = new FastList<L2PcInstance>();

        for (FastMap.Entry<L2PcInstance, Boolean> n = _gmList.head(), end = _gmList.tail(); (n = n.getNext()) != end;) {
            if (includeHidden || !n.getValue()) {
                tmpGmList.add(n.getKey());
            }
        }

        return tmpGmList;
    }

    public FastList<String> getAllGmNames(boolean includeHidden) {
        FastList<String> tmpGmList = new FastList<String>();

        for (FastMap.Entry<L2PcInstance, Boolean> n = _gmList.head(), end = _gmList.tail(); (n = n.getNext()) != end;) {
            if (!n.getValue()) {
                tmpGmList.add(n.getKey().getName());
            } else if (includeHidden) {
                tmpGmList.add(n.getKey().getName() + " (invis)");
            }
        }

        return tmpGmList;
    }

    private GmListTable() {
        _gmList = new FastMap<L2PcInstance, Boolean>().shared("GmListTable._gmList");
    }

    /**
     * Add a L2PcInstance player to the Set _gmList
     */
    public void addGm(L2PcInstance player, boolean hidden) {
        //if (Config.DEBUG) _log.fine("added gm: "+player.getName());
        _gmList.put(player, hidden);
    }

    public void deleteGm(L2PcInstance player) {
        //if (Config.DEBUG) _log.fine("deleted gm: "+player.getName());

        _gmList.remove(player);
    }

    /**
     * GM will be displayed on clients gmlist
     * @param player
     */
    public void showGm(L2PcInstance player) {
        FastMap.Entry<L2PcInstance, Boolean> gm = _gmList.getEntry(player);
        if (gm != null) {
            gm.setValue(false);
        }
    }

    /**
     * GM will no longer be displayed on clients gmlist
     * @param player
     */
    public void hideGm(L2PcInstance player) {
        FastMap.Entry<L2PcInstance, Boolean> gm = _gmList.getEntry(player);
        if (gm != null) {
            gm.setValue(true);
        }
    }

    public boolean isGmOnline(boolean includeHidden) {
        for (FastMap.Entry<L2PcInstance, Boolean> n = _gmList.head(), end = _gmList.tail(); (n = n.getNext()) != end;) {
            if (includeHidden || !n.getValue()) {
                return true;
            }
        }

        return false;
    }

    public void sendListToPlayer(L2PcInstance player) {
        if (!isGmOnline(player.isGM())) {
            player.sendPacket(Static.NO_GM_PROVIDING_SERVICE_NOW);
        } else {
            player.sendPacket(SystemMessage.id(SystemMessageId.GM_LIST));

            for (String name : getAllGmNames(player.isGM())) {
                player.sendPacket(SystemMessage.id(SystemMessageId.GM_S1).addString(name));
            }
        }
    }

    public static void broadcastToGMs(L2GameServerPacket packet) {
        for (L2PcInstance gm : getInstance().getAllGms(true)) {
            gm.sendPacket(packet);
        }
    }

    public static void broadcastMessageToGMs(String message) {
        for (L2PcInstance gm : getInstance().getAllGms(true)) {
            gm.sendPacket(SystemMessage.sendString(message));
        }
    }

    public static void broadcastChat(String name, String text) {
        CreatureSay pkt = new CreatureSay(0, 9, name, text);
        for (L2PcInstance gm : getInstance().getAllGms(true)) {
            if (gm != null) {
                gm.sendPacket(pkt);
            }
        }
    }
}