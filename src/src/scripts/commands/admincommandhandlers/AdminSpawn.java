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
package scripts.commands.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import ru.agecold.Config;
import ru.agecold.gameserver.GmListTable;
import ru.agecold.gameserver.datatables.*;
import ru.agecold.gameserver.idfactory.IdFactory;
import ru.agecold.gameserver.instancemanager.DayNightSpawnManager;
import ru.agecold.gameserver.instancemanager.RaidBossSpawnManager;
import ru.agecold.gameserver.model.L2Object;
import ru.agecold.gameserver.model.L2Spawn;
import ru.agecold.gameserver.model.L2World;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.network.SystemMessageId;
import ru.agecold.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.agecold.gameserver.network.serverpackets.SystemMessage;
import ru.agecold.gameserver.templates.L2NpcTemplate;
import scripts.commands.IAdminCommandHandler;

/**
 * This class handles following admin commands: - show_spawns = shows menu -
 * spawn_index lvl = shows menu for monsters with respective level -
 * spawn_monster id = spawns monster id on target
 *
 * @version $Revision: 1.2.2.5.2.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSpawn implements IAdminCommandHandler {

    private static final String[] ADMIN_COMMANDS = {"admin_show_spawns", "admin_spawn", "admin_spawn_monster", "admin_spawn_index",
        "admin_unspawnall", "admin_respawnall", "admin_spawn_reload", "admin_npc_index", "admin_spawn_once",
        "admin_show_npcs", "admin_teleport_reload", "admin_spawnnight", "admin_spawnday", "admin_botspawn"};
    public static final Logger _log = Logger.getLogger(AdminSpawn.class.getName());
    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
    private static final int REQUIRED_LEVEL2 = Config.GM_TELEPORT_OTHER;

    @Override
    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN) {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) {
                return false;
            }
        }

        if (command.equals("admin_show_spawns")) {
            AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
        } else if (command.startsWith("admin_spawn_index")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                st.nextToken();
                int level = Integer.parseInt(st.nextToken());
                int from = 0;
                try {
                    from = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException nsee) {
                }
                showMonsters(activeChar, level, from);
            } catch (Exception e) {
                AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
            }
        } else if (command.equals("admin_show_npcs")) {
            AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
        } else if (command.startsWith("admin_npc_index")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                st.nextToken();
                String letter = st.nextToken();
                int from = 0;
                try {
                    from = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException nsee) {
                }
                showNpcs(activeChar, letter, from);
            } catch (Exception e) {
                AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
            }
        } else if (command.startsWith("admin_spawn") || command.startsWith("admin_spawn_monster")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                String cmd = st.nextToken();
                String id = st.nextToken();
                int respawnTime = 0;
                int mobCount = 1;
                if (st.hasMoreTokens()) {
                    mobCount = Integer.parseInt(st.nextToken());
                }
                if (st.hasMoreTokens()) {
                    respawnTime = Integer.parseInt(st.nextToken());
                }
                if (cmd.equalsIgnoreCase("admin_spawn_once")) {
                    spawnMonster(activeChar, id, respawnTime, mobCount, false);
                } else {
                    spawnMonster(activeChar, id, respawnTime, mobCount, true);
                }
            } catch (Exception e) {	// Case of wrong or missing monster data
                System.err.println("#" + e);
                AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
            }
        } else if (command.startsWith("admin_unspawnall")) {
            for (L2PcInstance player : L2World.getInstance().getAllPlayers()) {
                player.sendPacket(SystemMessage.id(SystemMessageId.NPC_SERVER_NOT_OPERATING));
            }
            RaidBossSpawnManager.getInstance().cleanUp();
            DayNightSpawnManager.getInstance().cleanUp();
            L2World.getInstance().deleteVisibleNpcSpawns();
            GmListTable.broadcastMessageToGMs("NPC Unspawn completed!");
        } else if (command.startsWith("admin_spawnday")) {
            DayNightSpawnManager.getInstance().spawnDayCreatures();
        } else if (command.startsWith("admin_spawnnight")) {
            DayNightSpawnManager.getInstance().spawnNightCreatures();
        } else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload")) {
            // make sure all spawns are deleted
            RaidBossSpawnManager.getInstance().cleanUp();
            DayNightSpawnManager.getInstance().cleanUp();
            L2World.getInstance().deleteVisibleNpcSpawns();
            // now respawn all
            NpcTable.getInstance().reloadAllNpc();
            SpawnTable.getInstance().reloadAll();
            RaidBossSpawnManager.getInstance().reloadBosses();
            GmListTable.broadcastMessageToGMs("NPC Respawn completed!");
        } else if (command.startsWith("admin_teleport_reload")) {
            TeleportLocationTable.getInstance().reloadAll();
            GmListTable.broadcastMessageToGMs("Teleport List Table reloaded.");
        } else if (command.startsWith("admin_botspawn")) {
            String[] cmd = command.split(" ");
            String name = cmd[1];
            int classId = Integer.parseInt(cmd[2]);

            L2PcInstance fantom = L2PcInstance.restoreFake(IdFactory.getInstance().getNextId(), classId, false);
            fantom.setName(name);
            fantom.setTitle("");
            fantom.setXYZInvisible(activeChar.getX(), activeChar.getY(), activeChar.getZ());
            fantom.setFakeLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ());

            switch (classId) {
                case 92:
                case 102:
                case 109:
                    FakePlayersTablePlus.getInstance().wearArcher(fantom);
                    break;
                default:
                    FakePlayersTablePlus.getInstance().wearFantom(fantom);
                    break;
            }
        }
        return true;
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }

    private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, int mobCount, boolean permanent) {
        L2Object target = activeChar.getTarget();
        if (target == null) {
            target = activeChar;
        }
        if (target != activeChar && activeChar.getAccessLevel() < REQUIRED_LEVEL2) {
            return;
        }

        int monsterTemplate = 0;
        L2NpcTemplate template1;
        if (monsterId.matches("[0-9]*")) {
            //First parameter was an ID number
            monsterTemplate = Integer.parseInt(monsterId);
            template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
        } else {
            //First parameter wasn't just numbers so go by name not ID
            monsterId = monsterId.replace('_', ' ');
            template1 = NpcTable.getInstance().getTemplateByName(monsterId);
        }

        /*
         * if (SpawnTable.getInstance().isPcNpc(Integer.parseInt(monsterId))) {
         * activeChar.sendAdmResultMessage("##1#");
         * CustomServerData.getInstance().spawnPcNpc(monsterTemplate,
         * target.getX(), target.getY(), target.getZ(),
         * activeChar.getHeading()); return;
        }
         */

        if (monsterTemplate == 13006 || monsterTemplate == 13007) {
            //
            try {
                L2Spawn spawn = new L2Spawn(template1);
                spawn.setLocx(target.getX());
                spawn.setLocy(target.getY());
                spawn.setLocz(target.getZ());
                spawn.setAmount(mobCount);
                spawn.setHeading(activeChar.getHeading());
                spawn.setRespawnDelay(respawnTime);
                SpawnTable.getInstance().addNewSpawn(spawn, permanent);
                spawn.init();
                activeChar.sendAdmResultMessage("Created " + template1.name + " on " + target.getObjectId());

            } catch (Exception e) {
                activeChar.sendPacket(SystemMessage.id(SystemMessageId.TARGET_CANT_FOUND));
            }
            return;
        }

        try {
            L2Spawn spawn = new L2Spawn(template1);
            spawn.setLocx(target.getX());
            spawn.setLocy(target.getY());
            spawn.setLocz(target.getZ());
            spawn.setAmount(mobCount);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid())) {
                activeChar.sendAdmResultMessage("You cannot spawn another instance of " + template1.name + ".");
            } else {
                if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null) {
                    RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.getStatsSet().getDouble("baseHpMax"), template1.getStatsSet().getDouble("baseMpMax"), permanent);
                } else {
                    SpawnTable.getInstance().addNewSpawn(spawn, permanent);
                }
                if (SpawnTable.getInstance().isPcNpc(Integer.parseInt(monsterId))) {
                    CustomServerData.getInstance().spawnPcNpc(monsterTemplate, target.getX(), target.getY(), target.getZ(), activeChar.getHeading(), template1);
                } else {
                    spawn.init();
                }
                if (!permanent) {
                    spawn.stopRespawn();
                }
                activeChar.sendAdmResultMessage("Created " + template1.name + " on " + target.getObjectId());
            }
        } catch (Exception e) {
            activeChar.sendPacket(SystemMessage.id(SystemMessageId.TARGET_CANT_FOUND));
        }
    }

    private void showMonsters(L2PcInstance activeChar, int level, int from) {
        TextBuilder tb = new TextBuilder();

        L2NpcTemplate[] mobs = NpcTable.getInstance().getAllMonstersOfLevel(level);

        // Start
        tb.append("<html><title>Spawn Monster:</title><body><p> Level " + level + ":<br>Total Npc's : " + mobs.length + "<br>");
        String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index " + level + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
        String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";

        // Loop
        boolean ended = true;
        for (int i = from; i < mobs.length; i++) {
            String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";

            if ((tb.length() + txt.length() + end2.length()) > 8192) {
                end1 = end1.replace("$from$", "" + i);
                ended = false;
                break;
            }

            tb.append(txt);
        }

        // End
        if (ended) {
            tb.append(end2);
        } else {
            tb.append(end1);
        }

        activeChar.sendPacket(NpcHtmlMessage.id(5, tb.toString()));
    }

    private void showNpcs(L2PcInstance activeChar, String starting, int from) {
        TextBuilder tb = new TextBuilder();
        L2NpcTemplate[] mobs = NpcTable.getInstance().getAllNpcStartingWith(starting);
        // Start
        tb.append("<html><title>Spawn Monster:</title><body><p> There are " + mobs.length + " Npcs whose name starts with " + starting + ":<br>");
        String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index " + starting + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
        String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
        // Loop
        boolean ended = true;
        for (int i = from; i < mobs.length; i++) {
            String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";

            if ((tb.length() + txt.length() + end2.length()) > 8192) {
                end1 = end1.replace("$from$", "" + i);
                ended = false;
                break;
            }
            tb.append(txt);
        }
        // End
        if (ended) {
            tb.append(end2);
        } else {
            tb.append(end1);
        }
        activeChar.sendPacket(NpcHtmlMessage.id(5, tb.toString()));
    }
}
