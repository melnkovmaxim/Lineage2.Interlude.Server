package quests.q8081_RaidAnnounce;

import javolution.util.FastMap;

import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.jython.QuestJython;

public class q8081_RaidAnnounce extends QuestJython {

    private static FastMap<Integer, String> bosses = new FastMap<Integer, String>().shared("q8081_RaidAnnounce.bosses");

    public q8081_RaidAnnounce(int questId, String name, String descr) {
        super(questId, name, descr, 1);

		/**
			Шаблон:
				bosses.put(моб_ид, "текст_анонса");

			Пример:
				bosses.put(20039, "Игрок %name% убил босса %npcname%!");

			Автозамена:
				%name% - ник игрока
				%npcname% - имя моба
		*/
		bosses.put(50031, "Игрок %name% убил босса %npcname%!");

		//
        for (Integer boss_id : bosses.keySet()) {
			if (boss_id == null){
				continue;
			}

            this.addKillId(boss_id);
        }
    }

    public static void main(String... arguments) {
        new q8081_RaidAnnounce(8081, "q8081_RaidAnnounce", "Raid Announce");
    }

    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) {
        if (!bosses.containsKey(npc.getNpcId())) {
            return null;
        }

        announceDeath(npc, killer, bosses.get(npc.getNpcId()));
        return null;
    }

	private void announceDeath(L2NpcInstance npc, L2PcInstance killer, String announce) {
		if (announce == null || announce.isEmpty()){
			return;
		}
        Announcements.getInstance().announceToAll(replaceNames(killer.getName(), npc.getName(), announce));
	}

	private String replaceNames(String name, String npcname, String announce) {
		announce = announce.replaceAll("%name%", name);
		announce = announce.replaceAll("%npcname%", npcname);
		return announce;
	}
}