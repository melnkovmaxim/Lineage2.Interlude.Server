package quests.q8085_RaidDoors;

import javolution.util.FastMap;

import ru.agecold.gameserver.datatables.DoorTable;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;

public class q8085_RaidDoors extends QuestJython {

	//босс-дверь;босс-дверь;босс-дверь;босс-дверь
	private static String BOSS_DOORS = "25407-19240005;";

/////
    private final static FastMap<Integer, Integer> doors = new FastMap<Integer, Integer>();
/////

    public q8085_RaidDoors(int questId, String name, String descr) {
        super(questId, name, descr, 1);

		String[] data = BOSS_DOORS.split(";");
		for (String door : data) {
			if (door == null 
				|| door.isEmpty()) {
				continue;
			}

			String[] id = door.split("-");
			try{
				doors.put(Integer.parseInt(id[0]), Integer.parseInt(id[1]));
			} catch (NumberFormatException nfe) {
				//
            }
		}

		////////////
        for (Integer i : doors.keySet()) {
            this.addKillId(i);
        }
    }

    public static void main(String... arguments) {
        new q8085_RaidDoors(8085, "q8085_RaidDoors", "Raid Doors");
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) {
		tryOpenDoor(doors.get(npc.getNpcId()));
        return null;
    }

	private void tryOpenDoor(Integer door) {
		if (door == null) {
			return;
		}

		try {
            DoorTable.getInstance().getDoor(door).openMe();
		} catch (Exception e) {
			//
        }
	}
}