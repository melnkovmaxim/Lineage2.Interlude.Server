package ai;

import ru.agecold.gameserver.model.actor.instance.L2PcInstance;
import ru.agecold.gameserver.model.actor.instance.L2NpcInstance;
import ru.agecold.gameserver.model.quest.jython.QuestJython;

public class ClanRepBoss extends QuestJython
{
	private static int[] info = {200022, 3000, 41001, 2000, 41002, 2000, 41003, 2000, 41004, 2000, 41005, 2000, 200035, 3000, 200036, 1000, 15162, 500, 50031, 3000, 200049, 100, 25163, 500, 45114, 100, 15106, 100, 200049, 100, 80105, 100, 41101, 3000, 44447, 500, 44448, 500, 44449, 500, 44450, 500, 44451, 1500};

	public ClanRepBoss(int questId, String name, String descr)
	{
		super(questId, name, descr, 1);
		
		for (int i = 0; i < info.length; i += 2) {
            addKillId(info[i]);
        }
	}
	
    @Override
    public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet) {
        for (int i = 0; i < info.length; i += 2) {
            if (npc.getNpcId() == info[i]) {
            killer.getClan().setReputationScore(killer.getClan().getReputationScore() + info[i + 1], true);
            }
        }
        return super.onKill(npc, killer, isPet);
    }

	public static void main(String[] args)
	{
		new ClanRepBoss(-1, "ClanRepBoss", "ai");
	}
}