/* This program is free software; you can redistribute it and/or modify
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
package ru.agecold.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import ru.agecold.Config;
import ru.agecold.L2DatabaseFactory;
import ru.agecold.gameserver.datatables.CustomServerData;
import ru.agecold.gameserver.datatables.NpcTable;
import ru.agecold.gameserver.model.L2Clan;
import ru.agecold.gameserver.model.L2Spawn;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.entity.Castle;
import ru.agecold.gameserver.templates.L2NpcTemplate;
import ru.agecold.mysql.Close;
import ru.agecold.mysql.Connect;
import ru.agecold.util.log.AbstractLogger;

public class SiegeGuardManager {

    private static Logger _log = AbstractLogger.getLogger(SiegeGuardManager.class.getName());
    // =========================================================
    // Data Field
    private Castle _castle;
    private List<L2Spawn> _siegeGuardSpawn = new FastList<L2Spawn>();

    // =========================================================
    // Constructor
    public SiegeGuardManager(Castle castle) {
        _castle = castle;
    }

    // =========================================================
    // Method - Public
    /**
     * Add guard.<BR><BR>
     */
    public void addSiegeGuard(L2PcInstance activeChar, int npcId) {
        if (activeChar == null) {
            return;
        }
        addSiegeGuard(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
    }

    /**
     * Add guard.<BR><BR>
     */
    public void addSiegeGuard(int x, int y, int z, int heading, int npcId) {
        saveSiegeGuard(x, y, z, heading, npcId, 0);
    }

    /**
     * Hire merc.<BR><BR>
     */
    public void hireMerc(L2PcInstance activeChar, int npcId) {
        if (activeChar == null) {
            return;
        }
        hireMerc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
    }

    /**
     * Hire merc.<BR><BR>
     */
    public void hireMerc(int x, int y, int z, int heading, int npcId) {
        saveSiegeGuard(x, y, z, heading, npcId, 1);
    }

    /**
     * Remove a single mercenary, identified by the npcId and location.
     * Presumably, this is used when a castle lord picks up a previously dropped
     * ticket
     */
    public void removeMerc(int npcId, int x, int y, int z) {
        Connect con = null;
        PreparedStatement st = null;
        try {
            con = L2DatabaseFactory.get();
            st = con.prepareStatement("DELETE FROM castle_siege_guards WHERE npcId = ? And x = ? AND y = ? AND z = ? AND isHired = 1");
            st.setInt(1, npcId);
            st.setInt(2, x);
            st.setInt(3, y);
            st.setInt(4, z);
            st.execute();
        } catch (Exception e1) {
            _log.warning("Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e1);
        } finally {
            Close.CS(con, st);
        }
    }

    /**
     * Remove mercs.<BR><BR>
     */
    public void removeMercs() {
        Connect con = null;
        PreparedStatement st = null;
        try {
            con = L2DatabaseFactory.get();
            st = con.prepareStatement("DELETE FROM castle_siege_guards WHERE castleId = ? AND isHired = 1");
            st.setInt(1, getCastle().getCastleId());
            st.execute();
        } catch (Exception e1) {
            _log.warning("Error deleting hired siege guard for castle " + getCastle().getName() + ":" + e1);
        } finally {
            Close.CS(con, st);
        }
    }

    /**
     * Spawn guards.<BR><BR>
     */
    public void spawnSiegeGuard(L2Clan owner) {
        loadSiegeGuard(owner);
        for (L2Spawn spawn : getSiegeGuardSpawn()) {
            if (spawn != null) {
                spawn.init();
            }
        }
    }

    /**
     * Unspawn guards.<BR><BR>
     */
    public void unspawnSiegeGuard() {
        for (L2Spawn spawn : getSiegeGuardSpawn()) {
            if (spawn == null) {
                continue;
            }

            spawn.stopRespawn();
            spawn.getLastSpawn().doDie(spawn.getLastSpawn());
        }

        getSiegeGuardSpawn().clear();
    }

    // =========================================================
    // Method - Private
    /**
     * Load guards.<BR><BR>
     */
    private void loadSiegeGuard(L2Clan owner) {
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            con = L2DatabaseFactory.get();
            con.setTransactionIsolation(1);
            st = con.prepareStatement("SELECT * FROM castle_siege_guards WHERE castleId = ? AND isHired = ?");
            st.setInt(1, getCastle().getCastleId());
            /*if (getCastle().getOwnerId() > 0) // If castle is owned by a clan, then don't spawn default guards
             {
             st.setInt(2, 1);
             } else {
             st.setInt(2, 0);
             }*/
            st.setInt(2, 0);
            rs = st.executeQuery();
            rs.setFetchSize(50);

            L2Spawn spawn1;
            L2NpcTemplate template1 = null;

            while (rs.next()) {
                if (owner == null) {
                    template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
                }

                if (template1 != null) {
                    spawn1 = new L2Spawn(template1);
                    spawn1.setId(rs.getInt("id"));
                    spawn1.setAmount(1);
                    spawn1.setLocx(rs.getInt("x"));
                    spawn1.setLocy(rs.getInt("y"));
                    spawn1.setLocz(rs.getInt("z"));
                    spawn1.setHeading(rs.getInt("heading"));
                    spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
                    spawn1.setLocation(0);

                    _siegeGuardSpawn.add(spawn1);
                } else {
                    _log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
                }
            }
        } catch (Exception e1) {
            _log.warning("Error loading siege guard for castle " + getCastle().getName() + ":" + e1);
        } finally {
            Close.CSR(con, st, rs);
        }
    }

    /**
     * Save guards.<BR><BR>
     */
    private void saveSiegeGuard(int x, int y, int z, int heading, int npcId, int isHire) {
        Connect con = null;
        PreparedStatement st = null;
        try {
            con = L2DatabaseFactory.get();
            st = con.prepareStatement("INSERT INTO castle_siege_guards (castleId, npcId, x, y, z, heading, respawnDelay, isHired) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            st.setInt(1, getCastle().getCastleId());
            st.setInt(2, npcId);
            st.setInt(3, x);
            st.setInt(4, y);
            st.setInt(5, z);
            st.setInt(6, heading);
            if (isHire == 1) {
                st.setInt(7, 0);
            } else {
                st.setInt(7, 600);
            }
            st.setInt(8, isHire);
            st.execute();
        } catch (Exception e1) {
            _log.warning("Error adding siege guard for castle " + getCastle().getName() + ":" + e1);
        } finally {
            Close.CS(con, st);
        }
    }

    // =========================================================
    // Proeprty
    public final Castle getCastle() {
        return _castle;
    }

    public final List<L2Spawn> getSiegeGuardSpawn() {
        return _siegeGuardSpawn;
    }
}
