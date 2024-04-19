package quests.q8086_RaidTeleports;

import javolution.util.FastMap;

import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;
import ru.agecold.util.Location;

public class q8086_RaidTeleports extends QuestJython {

	//босс:x,y,z;босс:x,y,z;босс:x,y,z
	private static String BOSS_TELEPORTS = "25447:-74857,87141,-5125;";

/////
    private final static FastMap<Integer, Location> teleports = new FastMap<Integer, Location>();
/////

    public q8086_RaidTeleports(int questId, String name, String descr) {
        super(questId, name, descr, 1);

		String[] teles = BOSS_TELEPORTS.split(";");
		for (String tele : teles) {
			if (tele == null 
				|| tele.isEmpty()) {
				continue;
			}

			String[] idxyz = tele.split(":");
			Integer id = Integer.parseInt(idxyz[0]);

			String[] xyz = idxyz[1].split(",");

			teleports.put(id, new Location(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2])));
		}

		////////////
        for (Integer i : teleports.keySet()) {
            this.addKillId(i);
        }
    }

    public static void main(String... arguments) {
        new q8086_RaidTeleports(8086, "q8086_RaidTeleports", "Raid Teleports");
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) {
		tryTeleport(killer, teleports.get(npc.getNpcId()));
        return null;
    }

	private void tryTeleport(L2PcInstance killer, Location loc) {
		if (loc == null) {
			return;
		}

		killer.teleToLocation(loc.x, loc.y, loc.z);
	}
}